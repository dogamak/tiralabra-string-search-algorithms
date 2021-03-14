# User Guide

The project can be built using gradle by running `gradle build` in the project's root directory.
After successfully building the project, a `build/libs` subdirectory containing a `tiralabra.jar` file
should have been created.  

## Using the Search Utility

This project contains a `grep`-like command-line utility for searching for patterns in files.
This utility can be used by executing the `tiralabra.jar` file. Below is the output which is printed
when the `--help` flag is given to the utility:

```
Usage: java -jar tiralabra.jar [--rabin-karp] [--rabin-karp-bs]
                               [--knuth-morris-pratt] [--boyer-moore]
                               [--aho-corasick] [--naive]
                               [--pattern=<PATTERN>...] [--input=<FILE>...]
                               [<PATTERN>] [<FILE>...]

  --knuth-morris-pratt | Use the Knuth-Morris-Pratt algorithm for the subsequent patterns
       --rabin-karp-bs | Use the Rabin-Karp algorithm (using bit shift hashing)
                       | for the subsequent patterns
        --aho-corasick | Use the Aho-Corasick algorithm for the subsequent patterns
         --boyer-moore | Use the Boyer-Moore algorithm for the subsequent patterns
          --rabin-karp | Use the Rabin-Karp algorithm for the subsequent patterns
               --naive | Use the naive baseline algorithm for the subsequent patterns
 -i, --input=<PATTERN> | Substring to be searched from the input streams
  -p, --pattern=<FILE> | Path to a file or - for standard input.
```

The arguments are handled from left to right, with each flag specifying an algorithm creating a new instance of the corresponding algorithm.
The `PATTERN`s are added to the most recently created matcher instance. All matcher instances are run across all of the specified input files.
For example, the following command searches for the word `class` using the Aho-Corasick algorithm and for words `private` and `public` using Rabin-Karp:

```
$ java -jar tiralabra.jar \
    --aho-corasick -p class \
    --rabin-karp -p private -p public \
    -i input_file.java -i input_file2.java
```

## Running the Benchmarks

Benchmarks can be executed by building the project and executing the `BenchmarkRunner`:

```
$ gradle build
$ java -cp build/libs/tiralabra.java tiralabra.app.benchmark.BenchmarkRunner benchmarks/
Loaded benchmark 'No Matches'
Loaded benchmark 'Overlapping Pattern'
Loaded benchmark 'Short Pattern'
…
Running benchmark 'No Matches' with algorithm 'Knuth-Morris-Pratt'... FINISHED
Running benchmark 'No Matches' with algorithm 'Boyer-Moore'... FINISHED
Running benchmark 'No Matches' with algorithm 'Aho-Corasick'... FINISHED
…
```

The above command executes all benchmarks under the `benchmarks/` subdirectory. Alternatively you can list the specific benchmark files you want to run, if
executing all benchmarks is not needed.

By default the benchmarks are quite light, but this can be adjusted using the `--iterations` and `--input-multiplier` arguments.
The `--iterations` (or `-i`) can be used to multiply the per-benchmark iteration count by an integer multiplier and the `--input-multiplier` (or `-x`) argument
causes the benchmarks' input to be looped over the specified number of times.

The results are written to a file named `results.html`, which can be viewed using a web browser.
