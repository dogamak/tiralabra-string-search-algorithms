# Performance Comparison of String Search Algorithms

## Documentation and coursework

 - [Project Definition](docs/project-definition.md)
 - [Weekly Report 01](docs/weekly-report-01.md)

## Usage of the Command-Line Utility

After building the project using `gradle build` the JAR file should be located under `build/libs/tiralabra.jar`.
When executed without any arguments, the utility prints the following usage explanation:

```
Usage: java -jar tiralabra.jar [--rabin-karp] [--pattern=<PATTERN>...]
                               [--input=<FILE>...] [<PATTERN>] [<FILE>...]

 --rabin-karp | Use the Rabin-Karp algorithm for the subsequent patterns (Default)
    <PATTERN> | Substring to be searched from the input streams
       <FILE> | Path to a file or - for standard input.
```

The functionality is very limited as of now, but more functionality is on the way.
The utility has a `find`-like command-line interface, where the order of the arguments matters
and they are evaluated from left to right. Specifying a search algorithm causes any subsequently
defined patterns to be evaluated using the algorithm. This means that a single invocation of the
utility can be used to execute different searches using different algorithms. Moreover, specifying
the same algorithm multiple times causes multiple instances of that same algorithm to be created.
The way how you divide your search patterns to these instances may affect the performance.

Below is an example invocation and it's output:

```
$ java -jar build/libs/tiralabra.jar -p Rabin Karp src/main/java/tiralabra/app/Main.java - < docs/project-definition.md
Match at offset 191 on input stdin: Rabin
Match at offset 197 on input stdin: Karp
Match at offset 464 on input src/main/java/tiralabra/app/Main.java: Rabin
Match at offset 469 on input src/main/java/tiralabra/app/Main.java: Karp
Match at offset 474 on input src/main/java/tiralabra/app/Main.java: Rabin
Match at offset 479 on input src/main/java/tiralabra/app/Main.java: Karp
Match at offset 650 on input stdin: Rabin
Match at offset 656 on input stdin: Karp
Match at offset 1071 on input src/main/java/tiralabra/app/Main.java: Rabin
Match at offset 1076 on input src/main/java/tiralabra/app/Main.java: Karp
Match at offset 1304 on input src/main/java/tiralabra/app/Main.java: Karp
Match at offset 1373 on input src/main/java/tiralabra/app/Main.java: Karp
Match at offset 1406 on input stdin: Rabin
Match at offset 1412 on input stdin: Karp
Match at offset 1411 on input src/main/java/tiralabra/app/Main.java: Rabin
Match at offset 1416 on input src/main/java/tiralabra/app/Main.java: Karp
```
