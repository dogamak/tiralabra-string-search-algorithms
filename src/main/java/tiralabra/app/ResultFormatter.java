/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app;

import tiralabra.utils.ArrayList;
import java.util.stream.Stream;

/**
 * Formatter for formatting and outputting the results of executed benchmarks.
 */
public abstract class ResultFormatter {
  /**
   * List of the algorithms used in this run of benchmarks.
   */
  private ArrayList<String> algorithms = new ArrayList<>();

  /**
   * List of benchmarks executed in this set.
   */
  private ArrayList<String> benchmarks = new ArrayList<>();

  /**
   * Add an algorithm to the set of algorithms benchmarked.
   */
  public void defineAlgorithm(String algorithm) {
    algorithms.add(algorithm);
  }

  /**
   * Add a benchmark to the set of benchmarks executed.
   */
  public void defineBenchmark(String benchmark) {
    benchmarks.add(benchmark);
  }

  /**
   * Get list of the algorithms benchmarked in this run.
   */
  public Stream<String> getAlgorithms() {
    return algorithms.getStream();
  }

  /**
   * Get list of the benchmarks in this run.
   */
  public Stream<String> getBenchmarks() {
    return benchmarks.getStream();
  }

  /**
   * Executed before begining the execution of the benchmarks, but after defining all
   * of the algorithms and benchmarks with the {@link #defineBenchmark} and {@link #defineAlgorithm}
   * methods.
   */
  public void begin() {}

  /**
   * Called after each benchmark has been executed using a single algorithm.
   */
  public abstract void format(BenchmarkResult results);

  /**
   * Called after all benchmarks have been executed.
   */
  public void end() {}
}
