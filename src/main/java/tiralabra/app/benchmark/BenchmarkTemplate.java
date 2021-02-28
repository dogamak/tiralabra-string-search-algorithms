/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app.benchmark;

import tiralabra.algorithms.StringMatcher;
import tiralabra.algorithms.StringMatcherBuilder;
import tiralabra.algorithms.StringMatcherBuilderFactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.File;
import java.io.IOException;

/**
 * A benchmark template from which benchmarks can be created for
 * different algorithm implementations. 
 */
public class BenchmarkTemplate {
  /**
   * Name of this benchmark template.
   */
  private String name;

  /**
   * List of patterns to be searched from the input streams.
   */
  private byte[][] patterns;

  /**
   * Input streams for the benchmark.
   */
  private byte[][] streams;

  /**
   * Create a benchmark template from the values parsed from a file.
   *
   * @param name - Name of this template
   * @param patterns - List of byte patterns to search from the streams
   * @param streams - List of input streams from which to search
   */
  BenchmarkTemplate(String name, byte[][] patterns, byte[][] streams) {
    this.name = name;
    this.patterns = patterns;
    this.streams = streams;
  }

  /**
   * Get the name of this template.
   */
  public String getName() {
    return name;
  }

  /**
   * Read an parse a benchmark template from a file.
   *
   * <h3>Contents of a benchmark template file</h3>
   *
   * Each valid benchmark template file contains the following items:
   *
   * <ul>
   *   <li>The first line contains the name of the template.
   *   <li>The second line contains two numbers: the first is the number of patterns in this template and the second the number of input streams.
   *   <li>From the third line onwards, there is a single pattern per line. 
   *   <li>After the patterns, there are the input streams, one stream per line.
   * </ul>
   *
   * @param file - File containing the benchmark template.
   *
   * @return A benchmark template read and parsed from the file.
   */
  public static BenchmarkTemplate fromFile(File file) throws IOException {
    BufferedReader br = new BufferedReader(new FileReader(file));

    String name = br.readLine();

    String line = br.readLine();
    String[] counts = line.trim().split("\\s+");
    int pattern_count = Integer.valueOf(counts[0]);
    int stream_count = Integer.valueOf(counts[1]);

    byte[][] patterns = new byte[pattern_count][];
    byte[][] streams = new byte[stream_count][];

    for (int i = 0; i < pattern_count; i++) {
      patterns[i] = br.readLine().getBytes();
    }

    for (int i = 0; i < stream_count; i++) {
      streams[i] = br.readLine().getBytes();
    }

    return new BenchmarkTemplate(name, patterns, streams);
  }

  /**
   * Create a benchmark from this template by initializing an algorithm. 
   */
  public Benchmark initialize(StringMatcherBuilderFactory factory) {
    StringMatcher[] matchers = new StringMatcher[streams.length];

    for (int i = 0; i < streams.length; i++) {
      StringMatcherBuilder builder = factory.createBuilder();

      for (int j = 0; j < patterns.length; j++) {
        builder.addPattern(patterns[j]);
      }

      matchers[i] = builder.buildMatcher();
    }

    return new Benchmark(matchers, streams);
  }
}
