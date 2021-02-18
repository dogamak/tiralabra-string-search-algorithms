/**
 * @author : dogamak
 * @created : 2021-02-04
**/

package tiralabra.app;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.ByteArrayOutputStream;

public class BrowserResultFormatter extends ResultFormatter {
  private FileWriter writer = null;
  private String prevBenchmark = null;

  public BrowserResultFormatter(File file) {
    try {
      writer = new FileWriter(file);
    } catch (IOException ioe) {
      writer = null;
    }
  }

  private String readResourceFile(String path) throws IOException {
    InputStream stream = getClass().getResourceAsStream(path);
    ByteArrayOutputStream result = new ByteArrayOutputStream();

    byte[] buffer = new byte[1024];
    int length;

    while ((length = stream.read(buffer)) != -1) {
      result.write(buffer, 0, length);
    }

    return result.toString("UTF-8");
  }

  public void begin() throws IOException {
    String styles = readResourceFile("/results/styles.css");

    writer.write(
      "<html>" +
      "  <head>" +
      "    <title>Benchmark Results</title>" +
      "    <meta charset=\"UTF-8\" />" +
      "    <style>" + styles + "</style>" +
      "  </head>" +
      "  <body>"
    );

    writer.write("<table><tr><th>Behchmark</th>");

    Object[] algorithms = getAlgorithms().toArray();

    for (Object algo : algorithms) {
      writer.write("<th>" + algo + "</th>");
    }

    writer.write("</tr>");
  }

  public void format(BenchmarkResult result) throws IOException {
    if (prevBenchmark != result.getName()) {
      if (prevBenchmark != null) {
        writer.write("</tr>");
      }

      writer.write("<tr><th>" + result.getName() + "</th>");
    }

    writer.write("<td data-init=\"" + result.getAverageInitTime() + "\" data-exec=\"" + result.getAverageExecTime() + "\">");

    writeMetric("Initialization", "ms/iter", result.getAverageInitTime());
    writeMetric("Execution", "ms/iter", result.getAverageExecTime());

    writer.write("</td>");

    prevBenchmark = result.getName();
  }

  private void writeMetric(String name, String unit, double value) throws IOException {
    writer.write(String.format(
      "<div class=\"metric-group\">" +
      "  <span class=\"metric-label\">%s</span>" +
      "  <span class=\"metric-value\">%.4f</span>" +
      "  <span class=\"metric-unit\">%s</span>" +
      "</div>",
      name, value, unit
    ));
  }

  private void includeJavascriptResource(String path) throws IOException {
    writer.write("<script lang=\"text/javascript\">" + readResourceFile(path) + "</script>");
  }

  public void end() throws IOException {
    if (prevBenchmark != null) {
      writer.write("</tr>");
    }

    writer.write("</table>");

    includeJavascriptResource("/results/jquery-3.5.1.min.js");
    includeJavascriptResource("/results/javascript.js");

    writer.write("</body></html>");

    writer.flush();
    writer.close();
  }
}
