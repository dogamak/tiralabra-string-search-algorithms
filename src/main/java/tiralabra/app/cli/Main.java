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

import tiralabra.algorithms.BoyerMoore.BoyerMoore;
import tiralabra.utils.HashMap;
import tiralabra.utils.ArrayList;

import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.algorithms.SingleStringMatcherAdapter;
import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcher.Match;
import tiralabra.algorithms.RabinKarp.RabinKarpBuilder;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPrattBuilder;

class InputSource {
  InputStream stream;
  String name;
  ArrayList<StringMatcher> matchers = new ArrayList<>();

  InputSource(InputStream stream, String name) {
    this.stream = stream;
    this.name = name;
  }
}

public class Main {
  private static interface StringMatcherBuilderFactory {
    public StringMatcherBuilder create();
  }

  private static String[] FLAGS_TAKE_VALUE = new String[] { "input", "pattern" };
  
  private HashMap<String, StringMatcherBuilderFactory> matcherBuilderFactories = new HashMap<>();
  private StringMatcherBuilderFactory selectedFactory = Main::rabinKarpBuilderFactory;
  private StringMatcherBuilder matcherBuilder = null;
  private ArrayList<StringMatcherBuilder> matcherBuilders = new ArrayList<>();
  private ArrayList<InputSource> inputs = new ArrayList<>();

  Main () {
    matcherBuilderFactories.insert("rabin-karp", Main::rabinKarpBuilderFactory);
    matcherBuilderFactories.insert("knuth-morris-pratt", Main::knuthMorrisPrattBuilderFactory);
    matcherBuilderFactories.insert("boyer-moore", () -> BoyerMoore.getBuilder().adapt());
  }

  private static StringMatcherBuilder rabinKarpBuilderFactory() {
    return new RabinKarpBuilder();
  }

  private static StringMatcherBuilder knuthMorrisPrattBuilderFactory() {
    return new SingleStringMatcherAdapter(new KnuthMorrisPrattBuilder());
  }

  private void parseArguments(String[] args) {
      ArgumentParser parser = new ArgumentParser();

      parser.addFlagHandlerValue("i", "input", (flag, value) -> addInput(value));
      parser.addFlagHandlerValue("p", "pattern", (flag, value) -> addPattern(value));

      parser.addFlagHandler("rabin-karp", this::handleAlgorithmFlag);
      parser.addFlagHandler("knuth-morris-pratt", this::handleAlgorithmFlag);
      parser.addFlagHandler("boyer-moore", this::handleAlgorithmFlag);

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

  private void handleAlgorithmFlag(ArgumentParser.FlagOptions flag, String value) {
    StringMatcherBuilderFactory factory = matcherBuilderFactories.get(flag.getLongName());

    if (factory != null) {
      finishMatcher();
      selectMatcher(factory);
    } else {
      printUsage();
    }
  }

  private boolean handlePositionalArgument(int index, String arg) {
    if (index == 0) {
      addPattern(arg);
    } else {
      addInput(arg);
    }

    return true;
  }

  private void printUsage() {
    System.err.println("Usage: java -jar tiralabra.jar [--rabin-karp] [--knuth-morris-pratt]");
    System.err.println("                               [--boyer-moore]");
    System.err.println("                               [--pattern=<PATTERN>...] [--input=<FILE>...]");
    System.err.println("                               [<PATTERN>] [<FILE>...]");
    System.err.println();
    System.err.println("          --rabin-karp | Use the Rabin-Karp algorithm for the subsequent patterns");
    System.err.println("  --knuth-morris-pratt | Use the Knuth-Morris-Pratt algorithm for the subsequent patterns");
    System.err.println("         --boyer-moore | Use the Boyer-Moore algorithm for the subsequent patterns");
    System.err.println(" -i, --input=<PATTERN> | Substring to be searched from the input streams");
    System.err.println("  -p, --pattern=<FILE> | Path to a file or - for standard input.");
  }

  private void addPattern(String pattern) {
    if (matcherBuilder == null) {
      matcherBuilder = selectedFactory.create();
    }

    matcherBuilder.addPattern(pattern.getBytes());
  }

  private void addInput(String input) {
    if (input.equals("-")) {
      inputs.add(new InputSource(System.in, "stdin"));
    } else {
      try {
        FileInputStream stream = new FileInputStream(new File(input));
        inputs.add(new InputSource(stream, input));
      } catch (FileNotFoundException fnfe) {
        System.err.println("File not found: " + input);
        System.exit(1);
      }
    }
  }

  private void selectMatcher(StringMatcherBuilderFactory factory) {
    selectedFactory = factory;
  }

  private void finishMatcher() {
    if (matcherBuilder != null) {
      matcherBuilders.add(matcherBuilder);
    }
  }

  private void runMatchers() {
    for (int i = 0; i < inputs.size(); i++) {
      InputSource input = inputs.get(i);

      for (int j = 0; j < matcherBuilders.size(); j++) {
        StringMatcher matcher = matcherBuilders.get(j).buildMatcher();
        input.matchers.add(matcher);
      }
    }

    int inputIndex = 0;

    try {
      while (true) {
        inputIndex = (inputIndex + 1) % inputs.size();
        InputSource input = inputs.get(inputIndex);
        int inputByte = input.stream.read();

        for (int i = 0; i < input.matchers.size(); i++) {
          StringMatcher matcher = input.matchers.get(i);

          if (inputByte != -1) {
            matcher.pushByte((byte) inputByte);
          } else {
            matcher.finish();
          }

          Match match = matcher.pollMatch();

          if (match != null) {
            System.out.println("Match at offset " + match.getOffset() + " on input " + input.name + ": " + new String(match.getSubstring(), "UTF-8"));
          }
        }

        if (inputByte == -1) {
          break;
        }
      }
    } catch (IOException ioe) {
      System.err.println("IO error: " + ioe);
    }
  }

  private void run(String[] args) {
    parseArguments(args);
    finishMatcher();

    if (inputs.size() == 0) {
      addInput("-");
    }

    runMatchers();
  }

  public static void main(String[] args) {
    new Main().run(args);
  }
}
