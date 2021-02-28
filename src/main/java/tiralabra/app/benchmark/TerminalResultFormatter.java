/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app.benchmark;

/**
 * Benchmark result formatter for outputting an ASCII table
 * to the terminal.
 */
public class TerminalResultFormatter extends ResultFormatter {
  /**
   * Array containing the widths of the columns in characters.
   */
  private int[] column_width;

  /**
   * Array containing column separators.
   */
  private String[] column_separator;

  /**
   * Index of the current column (the next to be filled).
   */
  private int current_column = 0;

  /**
   * Name of the previous benchmark, whose results were formatted.
   */
  private String prevBenchmark;

  /**
   * Pads, aligns and formats the value according to the
   * {@link #column_width} and {@link #column_separators}
   * arrays.
   *
   * @param value - Value of the table cell.
   * @param span  - How many columns does this cell span.
   */
  private void formatColumn(String value, int span) {
    if (current_column == 0) {
      System.out.print(column_separator[0]);
    }

    int width = 0;

    while (span > 0) {
      width += column_width[current_column];

      if (span > 1) {
        width += column_separator[current_column+1].length();
      }

      current_column++;
      span--;
    }

    System.out.print(value);

    for (int i = 0; i < width - value.length(); i++) {
      System.out.print(" ");
    }

    System.out.print(column_separator[current_column]);
  }

  /**
   * End the current table row and begin a new one.
   */
  private void nextRow() {
    while (current_column < column_width.length) {
      formatColumn("", 1);
    }

    System.out.println();
    current_column = 0;
  }

  /**
   * Precalculates the table column widths and print's the table header.
   */
  public void begin() {
    int benchmark_name_column_width = getBenchmarks().mapToInt(String::length).max().orElse(0);
    int algorithm_name_column_width = getAlgorithms().mapToInt(String::length).max().orElse(0);

    if (algorithm_name_column_width < 18) {
      algorithm_name_column_width = 18;
    }

    column_width = new int[(int) getAlgorithms().count() * 2 + 1];
    column_width[0] = benchmark_name_column_width;

    for (int i = 1; i < column_width.length; i++) {
      column_width[i] = algorithm_name_column_width / 2;
    }

    column_separator = new String[column_width.length + 1];

    for (int i = 0; i < column_separator.length; i++) {
      column_separator[i] = i % 2 == 0 ? "  " : " | ";
    }

    formatColumn("", 1);

    getAlgorithms()
      .forEach(algo -> formatColumn(algo, 2));

    nextRow();

    formatColumn("", 1);

    getAlgorithms()
      .forEach(algo -> {
        formatColumn("Init (ns)", 1);
        formatColumn("Exec (ns)", 1);
      });

    nextRow();
  }

  /**
   * Prints the results of a benchmark formatted as table cells.
   *
   * Multiple benchmark results are on the same table row, so not
   * every call to this function results in a new table row.
   *
   * @param result - Results of a benchmark using a specific algorithm.
   */
  public void format(BenchmarkResult result) {
    if (prevBenchmark != result.getName()) {
      nextRow();
      formatColumn(result.getName(), 1);
      prevBenchmark = result.getName();
    }

    formatColumn(String.format("%.4f", result.getAverageInitTime()), 1);
    formatColumn(String.format("%.4f", result.getAverageExecTime()), 1);
  }
}
