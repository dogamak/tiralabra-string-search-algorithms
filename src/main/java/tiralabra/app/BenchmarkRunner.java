/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app;

import tiralabra.algorithms.BoyerMoore.BoyerMoore;
import tiralabra.algorithms.NaiveSearch.NaiveSearch;
import tiralabra.algorithms.RabinKarp.BitShiftHash;
import tiralabra.algorithms.RabinKarp.SimpleModuloHash;
import tiralabra.utils.ArrayList;
import tiralabra.algorithms.StringMatcherBuilderFactory;
import tiralabra.algorithms.RabinKarp.RabinKarp;
import tiralabra.algorithms.KnuthMorrisPratt.KnuthMorrisPratt;
import java.io.IOException;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

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
      new Algorithm("Rabin-Karp (Bit Shift)", () -> RabinKarp.getBuilder().setHashFunction(BitShiftHash::new)),
      new Algorithm("Rabin-Karp (Simple Modulo)", () -> RabinKarp.getBuilder().setHashFunction(SimpleModuloHash::new)),
      new Algorithm("Knuth-Morris-Pratt", KnuthMorrisPratt::getBuilder),
      new Algorithm("Boyer-Moore", BoyerMoore::getBuilder),
      new Algorithm("Naïve Search", NaiveSearch::getBuilder),
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
  private void run(String[] benchmarkPaths) {
    ArrayList<BenchmarkTemplate> templates = new ArrayList<>(benchmarkPaths.length);
    ArrayList<File> benchmarkFiles = new ArrayList<>();

    for (String path : benchmarkPaths) {
      File file = new File(path);

      if (file.isDirectory()) {
        for (File dirFile : file.listFiles()) {
          benchmarkFiles.add(dirFile);
        }
      } else {
        benchmarkFiles.add(file);
      }
    }

    for (int i = 0; i < benchmarkFiles.size(); i++) {
      File benchmarkFile = benchmarkFiles.get(i);

      try {
        BenchmarkTemplate template = BenchmarkTemplate.fromFile(benchmarkFile);

        System.out.format("Loaded benchmark '%s'\n", template.getName());

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

    System.out.format("Running benchmark '%s' with algorithm '%s'... ", benchmark.getName(), algorithm.name);
    boolean failure = false;

    ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    double[] init_times = new double[CYCLE_COUNT];
    double[] exec_times = new double[CYCLE_COUNT];

    int warmup_laps = CYCLE_COUNT / 10;

    try {
      for (int i = 0; i < CYCLE_COUNT + warmup_laps; i++) {
        long init_start = threadMXBean.getCurrentThreadCpuTime();
        Benchmark initialized = benchmark.initialize(algorithm.factory);
        long init_end = threadMXBean.getCurrentThreadCpuTime();

        long exec_start = threadMXBean.getCurrentThreadCpuTime();
        initialized.execute();
        long exec_end = threadMXBean.getCurrentThreadCpuTime();

        if (i >= warmup_laps) {
          init_time += init_times[i - warmup_laps] = init_end - init_start;
          exec_time += exec_times[i - warmup_laps] = exec_end - exec_start;
        }
      }
    } catch (Exception e) {
      failure = true;
      System.out.println("FAIL");
      e.printStackTrace(System.err);
    }

    if (!failure) {
      System.out.println("FINISHED");
    }

    double init_mean = init_time / (double) CYCLE_COUNT;
    double exec_mean = exec_time / (double) CYCLE_COUNT;

    double init_var = 0;
    double exec_var = 0;

    for (int i = 0; i < CYCLE_COUNT; i++) {
      init_var += Math.pow((init_times[i] - init_mean) / 1000000., 2);
      exec_var += Math.pow((exec_times[i] - exec_mean) / 1000000., 2);
    }

    init_var /= CYCLE_COUNT - 1.;
    exec_var /= CYCLE_COUNT - 1.;

    double init_per_cycle = (double) init_time / (double) CYCLE_COUNT / 1000000.;
    double exec_per_cycle = (double) exec_time / (double) CYCLE_COUNT / 1000000.;

    BenchmarkResult result = new BenchmarkResult(algorithm.name, benchmark.getName(), init_per_cycle, Math.sqrt(init_var), exec_per_cycle, Math.sqrt(exec_var));

    formatter.format(result);
  }
}


