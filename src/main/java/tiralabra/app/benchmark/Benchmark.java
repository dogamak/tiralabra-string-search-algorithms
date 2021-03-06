/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app.benchmark;

import tiralabra.algorithms.StringMatcher;

/**
 * An instance of a benchmark created from a {@link BenchmarkTemplate} using a specific string matching algorithm.
 */
public class Benchmark {
  /**
   * List of matchers benchmarked.
   *
   * Usually one for each of the implemented searching algorithms.
   */
  private StringMatcher[] matchers;

  /**
   * Input streams against which the matchers are benchmarked against.
   */
  private byte[][] streams;

  private int input_multiplier;

  /**
   * Create an {@link Benchmark} instance.
   *
   * @param matchers - Array containing already initialized instances of the string
   *                   matchers to be benchmarked.
   * @param streams  - Input streams used for the benchmarking.
   */
  public Benchmark(StringMatcher[] matchers, byte[][] streams) {
    this.matchers = matchers;
    this.streams = streams;
  }

  /**
   * Execute the benchmark.
   */
  public void execute() {
    for (int i = 0; i < matchers.length; i++) {
      for (int j = 0; j < input_multiplier; j++) {
        int consumed = 0;

        while (consumed < streams[i].length) {
          consumed += matchers[i].pushBytes(streams[i], consumed, streams[i].length - consumed);
          matchers[i].process();
        }

        matchers[i].pushByte((byte) 0);
      }

      matchers[i].finish();
    }
  }

  public void setInputMultiplier(int input_multiplier) {
    this.input_multiplier = input_multiplier;
  }
}
