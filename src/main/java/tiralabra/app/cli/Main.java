/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.app.cli;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Iterator;
import java.util.concurrent.locks.ReentrantLock;
import java.util.function.Consumer;

import tiralabra.algorithms.AhoCorasick.AhoCorasick;
import tiralabra.algorithms.BoyerMoore.BoyerMoore;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPratt;
import tiralabra.algorithms.RabinKarp.RabinKarp;
import tiralabra.algorithms.StringMatcherBuilderFactory;
import tiralabra.utils.HashMap;
import tiralabra.utils.ArrayList;

import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.algorithms.SingleStringMatcherAdapter;
import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcher.Match;
import tiralabra.algorithms.RabinKarp.RabinKarpBuilder;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPrattBuilder;
import tiralabra.utils.Queue;
import tiralabra.utils.RingBuffer;

/**
 * Information about a match and it's location in an input stream.
 */
class MatchContext {
  /**
   * Contents of the line which contains the match (or the matches start position).
   */
  byte[] line;

  /**
   * Number of the line on which the match is.
   */
  int lineNumber;

  /**
   * Number of characters from the start of the line to the start of the match.
   */
  int column;

  /**
   * Name of the input source.
   */
  String source;

  /**
   * Name of search algorithm which was used to find this match.
   */
  String algorithm;

  /**
   * The actual match object.
   */
  Match match;


  /**
   * Create a match context object from a match object.
   *
   * You need to populate the other fields manually.
   *
   * @param match - Match object received from a {@link StringMatcher}.
   */
  MatchContext(Match match) {
    this.match = match;
  }
}

/**
 * Thread which contains multiple string matcher instances and runs them against a single input stream.
 */
class InputSource extends Thread {
  /**
   * The input source against which the string matchers are executed.
   */
  private InputStream stream;

  /**
   * Name of this input source.
   */
  private String name;

  /**
   * List of {@link StringMatcher StringMatchers} which are executed against this input stream.
   */
  private ArrayList<StringMatcher> matchers = new ArrayList<>();

  /**
   * Callback which is called whenever we have found a match and have collected enough context for it.
   */
  private Consumer<MatchContext> matchCallback;

  /**
   * Buffer for holding onto input bytes after they have been fed to the string matchers.
   */
  private RingBuffer buffer = new RingBuffer(1024);

  /**
   * Current position in the input stream, counting bytes from the beginning.
   */
  private int input_offset;

  /**
   * Offset of start of the {@link #buffer} from the beginning of the input stream.
   */
  private int buffer_start_offset;

  /**
   * Length of the longest searched pattern.
   */
  private int longest_pattern_length = 0;

  /**
   * Number of the current line. (1-based)
   */
  private int line_counter = 1;

  /**
   * List of offsets of newlines in the buffer.
   */
  private Queue<Integer> newline_offsets = new Queue<>(8);

  /**
   * List of matches we have received from the matchers, but do not yet have enough context for.
   */
  private ArrayList<Match> pending_matches = new ArrayList<>(8);

  /**
   * Create a new input source thread.
   *
   * @param stream - The input stream.
   * @param name - Human readable name of this input source.
   * @param matchCallback - Callback called whenever a match is found.
   */
  InputSource(InputStream stream, String name, Consumer<MatchContext> matchCallback) {
    this.stream = stream;
    this.name = name;
    this.matchCallback = matchCallback;
  }

  /**
   * Add a matcher instance to the list of matchers executed against this input source.
   */
  public void addMatcher(StringMatcher matcher) {
    matchers.add(matcher);

    Iterator<byte[]> it = matcher.getPatterns();

    while (it.hasNext()) {
      byte[] pattern = it.next();

      if (pattern.length > longest_pattern_length) {
        longest_pattern_length = pattern.length;
      }
    }
  }

  /**
   * Called for each byte received from the input stream.
   *
   * @param b - Byte received from the input stream.
   */
  private void handleInputByte(byte b) {
    input_offset++;
    buffer.pushByte(b);

    // Push the byte to the string matchers and handle any found matches.

    for (StringMatcher matcher : matchers) {
      while (!matcher.pushByte(b)) {
        matcher.process();
      }

      if (b == (byte) '\n')
        matcher.process();

      handleMatches(matcher);
    }

    // If the byte is a line feed character, we know that by this point we must
    // have all necessary information in the buffer for any pending matches.

    if (b == (byte) '\n') {
      newline_offsets.push(input_offset);

      for (Match match : pending_matches) {
        MatchContext ctx = new MatchContext(match);
        ctx.source = this.name;
        calculateLineDetails(match, ctx);
        matchCallback.accept(ctx);
      }

      pending_matches.clear();
    }

    // Move the beginning of the buffer forward, single line at a time, until
    // we reach a line which may have yet-to-be-found matches.

    while (true) {
      Integer next_newline_offset = newline_offsets.peek();

      if (next_newline_offset == null)
        break;

      int advance_len = next_newline_offset - buffer_start_offset;

      if (buffer.size() - advance_len > longest_pattern_length) {
        newline_offsets.remove();
        buffer.advance(advance_len);
        buffer_start_offset += advance_len;
        line_counter++;
      } else {
        break;
      }
    }
  }

  /**
   * Check if a matcher has found any new matches.
   *
   * Check if we have received the full line for the match, and if so, call the {@link #matchCallback}.
   * If not, put the match to the {@link #pending_matches} list.
   *
   * @param matcher - String matcher to check for new matches.
   */
  private void handleMatches(StringMatcher matcher) {
    Match match;

    while ((match = matcher.pollMatch()) != null) {
      MatchContext ctx = new MatchContext(match);

      ctx.source = this.name;

      if (!calculateLineDetails(match, ctx)) {
        pending_matches.add(match);
      } else {
        matchCallback.accept(ctx);
      }
    }
  }

  /**
   * Determine if we have the full line containing a matches start offset in our buffer.
   * If not, return {@code false}. If we do have it, calculate the start and end offsets
   * of that line, and populate the {@link MatchContext#line}, {@link MatchContext#lineNumber}
   * and {@link MatchContext#column} fields.
   *
   * @param match - Match object received from the string matcher.
   * @param ctx - Context object which should be populated with the context information.
   *
   * @return {@code true} if line detauls could be calculated, {@code false} if more information is needed.
   */
  private boolean calculateLineDetails(Match match, MatchContext ctx) {
    int match_offset = match.getOffset();
    int line_number = line_counter;

    int prev_newline_offset = buffer_start_offset;
    int next_newline_offset = -1;

    for (int newline_offset : newline_offsets) {
      if (newline_offset > match_offset) {
        next_newline_offset = newline_offset;
        break;
      }

      line_number++;
      prev_newline_offset = newline_offset;
    }

    if (next_newline_offset == -1) {
      return false;
    }

    byte[] line = new byte[next_newline_offset - prev_newline_offset - 1];

    for (int i = prev_newline_offset; i < next_newline_offset - 1; i++) {
      line[i - prev_newline_offset] = buffer.get(i - buffer_start_offset);
    }

    ctx.lineNumber = line_number;
    ctx.line = line;
    ctx.column = match_offset - prev_newline_offset;

    return true;
  }

  /**
   * This method is the entry point of this thread. It continuously reads bytes from the input stream
   * and passes them to the {@link #handleInputByte(byte)} method.
   *
   * When the end of the input stream is reached, this method makes sure that all matches still pending for
   * context are "published".
   */
  @Override
  public void run() {
    while (true) {
      int inputByte;

      try {
        inputByte = stream.read();
      } catch (IOException ioe) {
        break;
      }

      if (inputByte == -1) {
        break;
      }

      handleInputByte((byte) inputByte);
    }

    newline_offsets.push(input_offset);

    for (StringMatcher matcher : matchers) {
      matcher.finish();
      handleMatches(matcher);
    }
  }
}

/**
 * A {@code grep}-like command-line utility for searching through files.
 */
public class Main {
  /**
   * Map from algorithm names to factories, which produce builders for the algorithms.
   */
  private HashMap<String, StringMatcherBuilderFactory> matcherBuilderFactories = new HashMap<>();

  /**
   * Builder factory for the currently selected search algorithm.
   */
  private StringMatcherBuilderFactory selectedFactory = RabinKarp::getBuilder;

  /**
   * Builder for the currently selected search algorithm.
   */
  private StringMatcherBuilder matcherBuilder = null;

  /**
   * List of all configured builders.
   *
   * Contains a builder for each search pattern after all arguments have been parsed.
   */
  private ArrayList<StringMatcherBuilder> matcherBuilders = new ArrayList<>();

  /**
   * List of input sources.
   */
  private ArrayList<InputSource> inputs = new ArrayList<>();

  Main () {
    matcherBuilderFactories.insert("rabin-karp", RabinKarp::getBuilder);
    matcherBuilderFactories.insert("knuth-morris-pratt", KnuthMorrisPratt::getBuilder);
    matcherBuilderFactories.insert("boyer-moore", () -> BoyerMoore.getBuilder().adapt());
    matcherBuilderFactories.insert("aho-corasick", AhoCorasick::getBuilder);
  }

  /**
   * Parses command-line arguments and creates {@link InputSource} and
   * {@link StringMatcherBuilder} instances based on them.
   *
   * @param args - The command-line arguments.
   */
  private void parseArguments(String[] args) {
    ArgumentParser parser = new ArgumentParser();

    parser.addFlagHandlerValue("i", "input", (flag, value) -> addInput(value));
    parser.addFlagHandlerValue("p", "pattern", (flag, value) -> addPattern(value));

    parser.addFlagHandler("rabin-karp", this::handleAlgorithmFlag);
    parser.addFlagHandler("knuth-morris-pratt", this::handleAlgorithmFlag);
    parser.addFlagHandler("boyer-moore", this::handleAlgorithmFlag);
    parser.addFlagHandler("aho-corasick", this::handleAlgorithmFlag);

    parser.addPositionalArgumentHandler(this::handlePositionalArgument);

    try {
      parser.parse(args);
    } catch (ArgumentParser.UnknownArgumentException e) {
        printUsage();
        System.exit(1);
    } catch (ArgumentParser.InvalidUsageException e) {
        System.err.println(e.toString());
        System.exit(1);
    }
  }

  /**
   * Common handler for all flags, which specify the search algorithm to be used.
   *
   * @param flag - Information about the flag in question.
   * @param value - Value for the flag, in this case always {@code null}, because these flags do not take values.
   */
  private void handleAlgorithmFlag(ArgumentParser.FlagOptions flag, String value) {
    StringMatcherBuilderFactory factory = matcherBuilderFactories.get(flag.getLongName());

    if (factory != null) {
      finishMatcher();
      selectedFactory = factory;
    } else {
      printUsage();
    }
  }

  /**
   * Handler which is called for each positional command-line argument.
   *
   * Interprets the first positinal argument as a search pattern and the rest as input sources.
   *
   * @param index - Number of the positional argument.
   * @param arg - Value of the argument.
   *
   * @return Whether or not the argument was handled by this handler.
   */
  private boolean handlePositionalArgument(int index, String arg) {
    if (index == 0) {
      addPattern(arg);
    } else {
      addInput(arg);
    }

    return true;
  }

  /**
   * Prints the usage instructions for this utility.
   */
  private void printUsage() {
    System.err.println("Usage: java -jar tiralabra.jar [--rabin-karp] [--knuth-morris-pratt]");
    System.err.println("                               [--boyer-moore]");
    System.err.println("                               [--pattern=<PATTERN>...] [--input=<FILE>...]");
    System.err.println("                               [<PATTERN>] [<FILE>...]");
    System.err.println();
    System.err.println("  --knuth-morris-pratt | Use the Knuth-Morris-Pratt algorithm for the subsequent patterns");
    System.err.println("        --aho-corasick | Use the Aho-Corasick algorithm for the subsequent patterns");
    System.err.println("         --boyer-moore | Use the Boyer-Moore algorithm for the subsequent patterns");
    System.err.println("          --rabin-karp | Use the Rabin-Karp algorithm for the subsequent patterns");
    System.err.println(" -i, --input=<PATTERN> | Substring to be searched from the input streams");
    System.err.println("  -p, --pattern=<FILE> | Path to a file or - for standard input.");
  }

  /**
   * Add a pattern to the matcher under construction.
   *
   * @param pattern - Pattern as a string.
   */
  private void addPattern(String pattern) {
    if (matcherBuilder == null) {
      matcherBuilder = selectedFactory.createBuilder();
    }

    matcherBuilder.addPattern(pattern.getBytes());
  }

  /**
   * Adds an input source to the list of input sources.
   *
   * Input source {@code -} is a special case and means the standard input.
   * All other values are interpreted as file paths.
   *
   * @param input The string {@code -} or a file path.
   */
  private void addInput(String input) {
    if (input.equals("-")) {
      inputs.add(new InputSource(System.in, "stdin", this::handleMatch));
    } else {
      try {
        FileInputStream stream = new FileInputStream(new File(input));
        inputs.add(new InputSource(stream, input, this::handleMatch));
      } catch (FileNotFoundException fnfe) {
        System.err.println("File not found: " + input);
        System.exit(1);
      }
    }
  }

  /**
   * Adds the currently active matcher builder to the list of configured matcher builders.
   */
  private void finishMatcher() {
    if (matcherBuilder != null) {
      matcherBuilders.add(matcherBuilder);
    }
  }

  /**
   * Initializes the matchers for each of the input sources.
   * Launches an thread for each input source and waits for them to finish.
   */
  private void runMatchers() {
    for (int i = 0; i < inputs.size(); i++) {
      InputSource input = inputs.get(i);

      for (int j = 0; j < matcherBuilders.size(); j++) {
        StringMatcher matcher = matcherBuilders.get(j).buildMatcher();
        input.addMatcher(matcher);
      }

      input.start();
    }

    try {
      for (InputSource input : inputs) {
        input.join();
      }
    } catch (InterruptedException ie) {
      return;
    }
  }

  /**
   * Lock preventing multiple {@link InputSource} threads from printing their matches at the same time.
   */
  private ReentrantLock handleMatchMutex = new ReentrantLock();

  /**
   * Callback executed by the {@link InputSource} threads whenever they find a match.
   *
   * @param ctx
   */
  private void handleMatch(MatchContext ctx) {
    try {
      handleMatchMutex.lock();

      System.out.format(
        "Match on line %d column %d of input '%s':\n    %s\n",
        ctx.lineNumber, ctx.column, ctx.source, new String(ctx.line)
      );

      for (int i = 0; i < ctx.column + 4; i++)
        System.out.print(' ');

      for (int i = 0; i < ctx.match.getSubstring().length; i++)
        System.out.print('^');

      System.out.print("\n\n");
    } finally {
      handleMatchMutex.unlock();
    }
  }

  /**
   * Parse command-line arguments, configure matchers and execute them.
   *
   * @param args - Command-line arguments
   */
  private void run(String[] args) {
    parseArguments(args);
    finishMatcher();

    if (inputs.size() == 0) {
      addInput("-");
    }

    runMatchers();
  }

  /**
   * Entrypoint of this application.
   *
   * @param args Command-line arguments
   */
  public static void main(String[] args) {
    new Main().run(args);
  }
}
