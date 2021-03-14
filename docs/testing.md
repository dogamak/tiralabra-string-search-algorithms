# Testing

## Unit Testing

The project is unit tested using the [JUnit 5](https://junit.org/junit5/) library and all unit tests can be found from the `src/test` subdirectory.

There are two types of unit tested components in this project: search algorithms and supporting data structures.
All search algorithms are tested by a [single parameterized test suite](src/test/java/tiralabra/algorithms/StringMatcherTest.java)
since all the search algorithms share the same interface which they need to satisfy.

The supporting data structures each have their own interface and thus each has its own test suite.
These data structures are implemented in the `tiralabra.utils` package.

## Performance Testing

The search algorithm implementations are performance tested against a collection of benchmarks found under the `benchmarks` subdirectory.
Each of the benchmarks is designed to take advantage of some algorithm's weak or strong points.

### Benchmark File Format

The benchmark files are UTF-8 encoded text files with lines with the following information:

- **Line 1** consists of a human-readable of the benchmark
- **Line 2** contains the following integers in order:
  - Number of patterns defined in this file (*𝑛*)
  - Number of separate input streams defined in this file (*𝑘*)
  - Default number of iterations for this benchmark
- **Line 3** through **Line 3+𝒏** contain a single search pattern per line
- **Line 4+n** through **Line 4+𝒏+𝒌** contain a single input stream per line

### Measured Metrics

Execution of a single benchmark instance is implemented in the class [Benchmark](src/main/java/tiralabra/app/benchmark/Benchmark.java) while
the class [BenchmarkRunner](src/main/java/tiralabra/app/benchmark/BenchmarkRunner.java) handles measuring the execution times.

Time is measured by using the
[ThreadMXBean.getCurrentThreadCpuTime()](https://docs.oracle.com/javase/7/docs/api/java/lang/management/ThreadMXBean.html#getCurrentThreadCpuTime())
method, which returns the CPU time used by the current thread in nanoseconds. In an attempt to minimize the effect JVM "warming up" has on the benchmarks,
an extra 10% of warmup iterations is executed *before* staring to measure time.

Each benchmark iteration consists of two phases: initialization and execution. The time spent on these phases is measured separately.
The initialization phase consists of allocating and setting up the supporting data structures.

From these two time measures three additional metrics are derived: the standard deviation of the initialization and execution times
and the speed of the implementation in bytes per second.

### Running the Benchmarks

Benchmarks can be executed by building the project and executing the `BenchmarkRunner`:

```shell
$ gradle build
$ java -cp build/libs/tiralabra.java tiralabra.app.benchmark.BenchmarkRunner benchmarks/
```

The above command executes all benchmarks under the `benchmarks/` subdirectory. Alternatively you can list the specific benchmark files you want to run, if
executing all benchmarks is not needed.

By default the benchmarks are quite light, but this can be adjusted using the `--iterations` and `--input-multiplier` arguments.
The `--iterations` (or `-i`) can be used to multiply the per-benchmark iteration count by an integer multiplier and the `--input-multiplier` (or `-x`) argument
causes the benchmarks' input to be looped over the specified number of times.

The results are written to a file named `results.html`, which can be viewed using a web browser.
