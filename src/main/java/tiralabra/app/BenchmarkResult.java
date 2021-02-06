/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app;

/**
 * Results of a benchmark.
 */
public class BenchmarkResult {
  /**
   * Name of the algorithm benchmarked.
   */
  private String algorithm;

  /**
   * Name of the benchmark template from which this benchmark was created.
   */
  private String name;

  /**
   * Average time taken to initialize the algorithm in microseconds.
   */
  private double init_average;
  
  /**
   * Average time taken to execute the search algorithm itself in microseconds.
   */
  private double exec_average;

  /**
   * Create a new instance containing the results of a single benchmark run.
   *
   * @param algo - Name of the algorithm.
   * @param name - Name of the benchmark template.
   * @param init_average - Average initialization time.
   * @param exec_average - Average execution time.
   */
  public BenchmarkResult(String algo, String name, double init_average, double exec_average) {
    this.algorithm = algo;
    this.name = name;
    this.init_average = init_average;
    this.exec_average = exec_average;
  }

  /**
   * Get name of the algorithm used in this benchmark.
   */
  public String getAlgorithm() {
    return algorithm;
  }

  /**
   * Get name of the benchmark template used to create this benchmark.
   */
  public String getName() {
    return name;
  }

  /**
   * Get the average initialization time in microseconds.
   */
  public double getAverageInitTime() {
    return init_average;
  }

  /**
   * Get the average execution time in microseconds.
   */
  public double getAverageExecTime() {
    return exec_average;
  }
}
