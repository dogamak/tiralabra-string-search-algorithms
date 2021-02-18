/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app;

import tiralabra.utils.HashMap;
import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcherBuilderFactory;
import tiralabra.algorithms.RabinKarp.RabinKarp;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPratt;
import java.io.IOException;
import java.io.File;

/**
 * A data type containing the name of a search algorithm and a factory instance
 * for creating builders for that algorithm.
 */
class Algorithm {
  /**
   * Unique and human readable name of the algorithm.
   */
  public String name;

  /**
   * Factory for creating builders for the algorithm.
   */
  public StringMatcherBuilderFactory factory;

  /**
   * Create a new instance.
   *
   * @param name - Name of the algorithm.
   * @param factory - Factory instance for creating builders for this algorithm.
   */
  public Algorithm(String name, StringMatcherBuilderFactory factory) {
    this.name = name;
    this.factory = factory;
  }
}

/**
 * A class for creating a set of benchmarks from templates and executing them.
 */
public class BenchmarkRunner {
  /**
   * List of the implemented and benchmarked search algorithms.
   */
  private Algorithm[] algorithms;

  /**
   * The number of iterations for each benchmark.
   *
   * TODO: Allow for benchmark templates to specify custom iteration counts.
   */
  private static int CYCLE_COUNT = 1000;

  /**
   * A formatter for outputting the benchmark results.
   */
  private static ResultFormatter formatter = new BrowserResultFormatter(new File("results.html"));

  BenchmarkRunner() {
    algorithms = new Algorithm[] {
      new Algorithm("rabin-karp", RabinKarp::getBuilder),
      new Algorithm("knuth-morris-pratt", KnuthMorrisPratt::getBuilder),
    };
  }
  
  /**
   * Initialize the class and call the {@link #run} method.
   */
  public static void main(String[] args) {
    new BenchmarkRunner().run(args);
  }

  /**
   * Create benchmarks from the specified benchmark template files and execute them
   * for each of the supported search-algorithms.
   */
  private void run(String[] benchmarkFiles) {
    ArrayList<BenchmarkTemplate> templates = new ArrayList<>(benchmarkFiles.length);

    for (String benchmarkFile : benchmarkFiles) {
      try {
        BenchmarkTemplate template = BenchmarkTemplate.fromFile(new File(benchmarkFile));

        templates.add(template);

        formatter.defineBenchmark(template.getName());
      } catch (IOException ioe) {
        System.err.println("Unable to load benchmark '" + benchmarkFile + "': " + ioe);
      }
    }

    for (Algorithm algo : algorithms) {
      formatter.defineAlgorithm(algo.name);
    }

    try {
      formatter.begin();

      for (int i = 0; i < templates.size(); i++) {
        BenchmarkTemplate template = templates.get(i);

        for (Algorithm algorithm : algorithms) {
          runBenchmark(template, algorithm);
        }
      }

      formatter.end();
    } catch (IOException ioe) {
      System.err.println("Unable to write benchmark results: " + ioe);
    }
  }

  /**
   * Runs a benchmark using the given template and algorithm.
   */
  private void runBenchmark(BenchmarkTemplate benchmark, Algorithm algorithm) throws IOException {
    long init_time = 0;
    long exec_time = 0;

    for (int i = 0; i < CYCLE_COUNT; i++) {
      long init_start = System.nanoTime();
      Benchmark initialized = benchmark.initialize(algorithm.factory);
      long init_end = System.nanoTime();

      long exec_start = System.nanoTime();
      initialized.execute();
      long exec_end = System.nanoTime();

      init_time += init_end - init_start;
      exec_time += exec_end - exec_start;
    }

    double init_per_cycle = (double) init_time / (double) CYCLE_COUNT / 1000000;
    double exec_per_cycle = (double) exec_time / (double) CYCLE_COUNT / 1000000;

    BenchmarkResult result = new BenchmarkResult(algorithm.name, benchmark.getName(), init_per_cycle, exec_per_cycle);

    /*results
      .entry(benchmark.getName())
      .orInsert(new HashMap<>())
      .getValue()
      .insert(algorithm.name, new double[] { init_per_cycle, exec_per_cycle });*/

    formatter.format(result);
  }
}


