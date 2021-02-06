/**
 * @author : dogamak
 * @created : 2021-01-30
**/

package tiralabra.app;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
  }

  private static StringMatcherBuilder rabinKarpBuilderFactory() {
    return new RabinKarpBuilder();
  }

  private static StringMatcherBuilder knuthMorrisPrattBuilderFactory() {
    return new SingleStringMatcherAdapter(new KnuthMorrisPrattBuilder());
  }

  private String getFlagLongVariant(String shortFlag) {
    if (shortFlag.equals("i")) {
      return "input";
    } else if (shortFlag.equals("p")) {
      return "pattern";
    } else {
      return null;
    }
  }

  private void parseArguments(String[] args) {
    boolean expectFlags = true;
    int positionalIndex = 0;

    for (int i = 0; i < args.length; i++) {
      String arg = args[i];

      if (arg.equals("--")) {
        expectFlags = false;
        continue;
      }

      boolean longFlag = expectFlags && arg.startsWith("--");
      boolean shortFlag = expectFlags && !longFlag && arg.startsWith("-") && arg.length() > 1;

      if (longFlag || shortFlag) {
        String flag = arg;
        String value = null;

        for (int j = 0; j < flag.length(); j++) {
          if (flag.charAt(j) == '=') {
            value = flag.substring(j+1);
            flag = flag.substring(0, j);
          }
        }

        if (longFlag) {
          flag = flag.substring(2);
        }

        if (shortFlag) {
          flag = getFlagLongVariant(flag.substring(1));

          if (flag == null) {
            printUsage();
          }
        }

        if (value == null) {
          for (int j = 0; j < FLAGS_TAKE_VALUE.length; j++) {
            if (FLAGS_TAKE_VALUE[j].equals(flag)) {
              value = args[i+1];
              i++;
              break;
            }
          }
        }

        handleFlag(flag, value);
      } else {
        handlePositionalArgument(arg, positionalIndex);
        positionalIndex += 1;
      }
    }
  }

  private void handlePositionalArgument(String arg, int index) {
    if (index == 0) {
      addPattern(arg);
    } else {
      addInput(arg);
    }
  }

  private void handleFlag(String name, String value) {
    if (name.equals("pattern")) {
      addPattern(value);
    } else if (name.equals("input")) {
      addInput(value);
    } else {
      StringMatcherBuilderFactory factory = matcherBuilderFactories.get(name);

      if (factory != null) {
        finishMatcher();
        selectMatcher(factory);
      } else {
        printUsage();
      }
    }
  }

  private void printUsage() {
    System.err.println("Usage: java -jar tiralabra.jar [--rabin-karp] [--pattern=<PATTERN>...]");
    System.err.println("                               [--input=<FILE>...] [<PATTERN>] [<FILE>...]");
    System.err.println();
    System.err.println(" --rabin-karp | Use the Rabin-Karp algorithm for the subsequent patterns");
    System.err.println("    <PATTERN> | Substring to be searched from the input streams");
    System.err.println("       <FILE> | Path to a file or - for standard input.");
    System.exit(1);
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

        if (inputByte == -1) {
          break;
        }

        for (int i = 0; i < input.matchers.size(); i++) {
          StringMatcher matcher = input.matchers.get(i);

          matcher.pushByte((byte) inputByte);

          Match match = matcher.pollMatch();

          if (match != null) {
            System.out.println("Match at offset " + match.getOffset() + " on input " + input.name + ": " + new String(match.getSubstring(), "UTF-8"));
          }
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
