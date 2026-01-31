package io.github.thanglequoc.timerninja;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Utility class for generating formatted statistics reports.
 * <p>
 * Supports three output formats:
 * <ul>
 * <li><b>text</b> - Plain text table format</li>
 * <li><b>json</b> - JSON format for programmatic consumption</li>
 * <li><b>html</b> - Self-contained HTML with Bootstrap 5 styling</li>
 * </ul>
 */
public class StatisticsReportGenerator {

  private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ISO_INSTANT.withZone(ZoneOffset.UTC);

  private StatisticsReportGenerator() {
    // Utility class
  }

  /**
   * Generates a report in the specified format.
   *
   * @param stats  collection of method statistics
   * @param format output format: "text", "json", or "html"
   * @return formatted report as string
   */
  public static String generateReport(Collection<MethodStatistics> stats, String format) {
    return switch (format.toLowerCase()) {
      case "json" -> generateJsonReport(stats);
      case "html" -> generateHtmlReport(stats);
      default -> generateTextReport(stats);
    };
  }

  /**
   * Generates a report as bytes for download.
   *
   * @param stats  collection of method statistics
   * @param format output format: "text", "json", or "html"
   * @return report as byte array
   */
  public static byte[] generateReportAsBytes(Collection<MethodStatistics> stats, String format) {
    return generateReport(stats, format).getBytes(StandardCharsets.UTF_8);
  }

  /**
   * Generates a plain text report.
   */
  public static String generateTextReport(Collection<MethodStatistics> stats) {
    StringBuilder sb = new StringBuilder();
    String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());

    sb.append("===== Timer Ninja Statistics Report =====\n");
    sb.append("Generated: ").append(timestamp).append("\n");
    sb.append("Total tracked methods: ").append(stats.size()).append("\n\n");

    if (stats.isEmpty()) {
      sb.append("No statistics recorded.\n");
    } else {
      // Header
      sb.append(String.format("%-50s | %7s | %8s | %8s | %8s | %8s | %8s | %8s | %10s | %10s%n",
          "Tracker ID", "Count", "Avg", "p50", "p90", "p95", "Min", "Max", "Threshold↑", "Threshold↓"));
      sb.append("-".repeat(140)).append("\n");

      // Sort by tracker ID and handle hierarchy
      List<MethodStatistics> sorted = sortForDisplay(stats);

      for (MethodStatistics stat : sorted) {
        String prefix = stat.getParentTrackerId() != null ? "  └─ " : "";
        String trackerId = truncate(prefix + stat.getTrackerId(), 50);

        String thresholdUp = stat.getThresholdMs() > 0
            ? String.valueOf(stat.getThresholdExceededCount())
            : "-";
        String thresholdDown = stat.getThresholdMs() > 0
            ? String.valueOf(stat.getThresholdWithinCount())
            : "-";

        sb.append(String.format("%-50s | %7d | %6dms | %6dms | %6dms | %6dms | %6dms | %6dms | %10s | %10s%n",
            trackerId,
            stat.getSampleCount(),
            stat.calculateAverage(),
            stat.calculatePercentile(50),
            stat.calculatePercentile(90),
            stat.calculatePercentile(95),
            stat.getMin(),
            stat.getMax(),
            thresholdUp,
            thresholdDown));
      }
    }

    sb.append("\n===== End of Report =====\n");
    return sb.toString();
  }

  /**
   * Generates a JSON report.
   */
  public static String generateJsonReport(Collection<MethodStatistics> stats) {
    StringBuilder sb = new StringBuilder();
    String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());

    sb.append("{\n");
    sb.append("  \"generatedAt\": \"").append(timestamp).append("\",\n");
    sb.append("  \"totalMethods\": ").append(stats.size()).append(",\n");
    sb.append("  \"trackers\": [\n");

    List<MethodStatistics> statsList = new ArrayList<>(stats);
    for (int i = 0; i < statsList.size(); i++) {
      MethodStatistics stat = statsList.get(i);
      sb.append("    {\n");
      sb.append("      \"trackerId\": \"").append(escapeJson(stat.getTrackerId())).append("\",\n");
      sb.append("      \"className\": \"").append(escapeJson(stat.getClassName())).append("\",\n");
      sb.append("      \"methodSignature\": \"").append(escapeJson(stat.getMethodSignature())).append("\",\n");
      sb.append("      \"count\": ").append(stat.getSampleCount()).append(",\n");
      sb.append("      \"avgMs\": ").append(stat.calculateAverage()).append(",\n");
      sb.append("      \"p50Ms\": ").append(stat.calculatePercentile(50)).append(",\n");
      sb.append("      \"p90Ms\": ").append(stat.calculatePercentile(90)).append(",\n");
      sb.append("      \"p95Ms\": ").append(stat.calculatePercentile(95)).append(",\n");
      sb.append("      \"minMs\": ").append(stat.getMin()).append(",\n");
      sb.append("      \"maxMs\": ").append(stat.getMax()).append(",\n");
      sb.append("      \"thresholdMs\": ").append(stat.getThresholdMs()).append(",\n");
      sb.append("      \"thresholdExceeded\": ").append(stat.getThresholdExceededCount()).append(",\n");
      sb.append("      \"thresholdWithin\": ").append(stat.getThresholdWithinCount()).append(",\n");
      sb.append("      \"parentTrackerId\": ").append(stat.getParentTrackerId() != null
          ? "\"" + escapeJson(stat.getParentTrackerId()) + "\""
          : "null").append(",\n");
      sb.append("      \"childTrackerIds\": [");
      List<String> children = stat.getChildTrackerIds();
      for (int j = 0; j < children.size(); j++) {
        sb.append("\"").append(escapeJson(children.get(j))).append("\"");
        if (j < children.size() - 1)
          sb.append(", ");
      }
      sb.append("]\n");
      sb.append("    }");
      if (i < statsList.size() - 1)
        sb.append(",");
      sb.append("\n");
    }

    sb.append("  ]\n");
    sb.append("}\n");
    return sb.toString();
  }

  /**
   * Generates a self-contained HTML report with Bootstrap 5 styling.
   */
  public static String generateHtmlReport(Collection<MethodStatistics> stats) {
    StringBuilder sb = new StringBuilder();
    String timestamp = TIMESTAMP_FORMATTER.format(Instant.now());

    // HTML Header with Bootstrap CDN
    sb.append("""
        <!DOCTYPE html>
        <html lang="en">
        <head>
            <meta charset="UTF-8">
            <meta name="viewport" content="width=device-width, initial-scale=1.0">
            <title>Timer Ninja Statistics Report</title>
            <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
            <style>
                body { background-color: #1a1a2e; color: #eaeaea; }
                .card { background-color: #16213e; border: 1px solid #0f3460; }
                .table { color: #eaeaea; }
                .table-dark { --bs-table-bg: #16213e; --bs-table-border-color: #0f3460; }
                .threshold-exceed { color: #e94560; font-weight: bold; }
                .threshold-within { color: #00d9ff; }
                .child-row { background-color: #0f3460 !important; }
                .expand-btn { cursor: pointer; user-select: none; }
                .expand-btn:hover { background-color: #1a1a4e !important; }
                .ninja-icon { font-size: 2rem; }
                .stat-value { font-family: 'Courier New', monospace; }
                .badge-threshold { font-size: 0.75rem; }
            </style>
        </head>
        <body>
            <div class="container py-4">
                <div class="d-flex align-items-center mb-4">
                    <span class="ninja-icon me-3">🥷</span>
                    <div>
                        <h1 class="mb-0">Timer Ninja Statistics Report</h1>
                        <p class="text-muted mb-0">Generated: %s</p>
                    </div>
                </div>

                <div class="card mb-4">
                    <div class="card-body">
                        <h5 class="card-title">Summary</h5>
                        <p class="card-text">Total tracked methods: <strong>%d</strong></p>
                    </div>
                </div>
        """.formatted(timestamp, stats.size()));

    if (stats.isEmpty()) {
      sb.append("""
              <div class="alert alert-info">
                  No statistics recorded yet. Enable statistics reporting and execute some tracked methods.
              </div>
          """);
    } else {
      sb.append("""
              <div class="table-responsive">
                  <table class="table table-dark table-hover align-middle">
                      <thead>
                          <tr>
                              <th>Tracker ID</th>
                              <th class="text-end">Count</th>
                              <th class="text-end">Avg</th>
                              <th class="text-end">p50</th>
                              <th class="text-end">p90</th>
                              <th class="text-end">p95</th>
                              <th class="text-end">Min</th>
                              <th class="text-end">Max</th>
                              <th class="text-center">Threshold</th>
                          </tr>
                      </thead>
                      <tbody>
          """);

      // Group by parent for hierarchy display
      List<MethodStatistics> roots = stats.stream()
          .filter(s -> s.getParentTrackerId() == null)
          .sorted(Comparator.comparing(MethodStatistics::getTrackerId))
          .collect(Collectors.toList());

      Map<String, List<MethodStatistics>> byParent = stats.stream()
          .filter(s -> s.getParentTrackerId() != null)
          .collect(Collectors.groupingBy(MethodStatistics::getParentTrackerId));

      int rowIndex = 0;
      for (MethodStatistics root : roots) {
        rowIndex++;
        List<MethodStatistics> children = byParent.getOrDefault(root.getTrackerId(), List.of());
        boolean hasChildren = !children.isEmpty();

        appendHtmlRow(sb, root, rowIndex, hasChildren, false);

        if (hasChildren) {
          for (MethodStatistics child : children) {
            appendHtmlChildRow(sb, child, rowIndex);
          }
        }
      }

      // Also add orphan children (parent not in stats)
      for (MethodStatistics stat : stats) {
        if (stat.getParentTrackerId() != null && !roots.stream()
            .anyMatch(r -> r.getTrackerId().equals(stat.getParentTrackerId()))) {
          rowIndex++;
          appendHtmlRow(sb, stat, rowIndex, false, true);
        }
      }

      sb.append("""
                      </tbody>
                  </table>
              </div>
          """);
    }

    // HTML Footer
    sb.append("""
            </div>
            <script src="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/js/bootstrap.bundle.min.js"></script>
            <script>
                document.querySelectorAll('.expand-btn').forEach(btn => {
                    btn.addEventListener('click', () => {
                        const target = btn.getAttribute('data-target');
                        const icon = btn.querySelector('.expand-icon');
                        document.querySelectorAll(target).forEach(row => {
                            row.classList.toggle('d-none');
                        });
                        icon.textContent = icon.textContent === '▶' ? '▼' : '▶';
                    });
                });
            </script>
        </body>
        </html>
        """);

    return sb.toString();
  }

  private static void appendHtmlRow(StringBuilder sb, MethodStatistics stat, int rowIndex,
      boolean hasChildren, boolean isOrphan) {
    String expandBtn = hasChildren
        ? "<span class='expand-icon'>▶</span> "
        : "";
    String rowClass = hasChildren ? "expand-btn" : "";
    String dataTarget = hasChildren ? "data-target='.child-row-" + rowIndex + "'" : "";
    String prefix = isOrphan ? "<span class='text-muted'>└─</span> " : "";

    sb.append(String.format("""
            <tr class="%s" %s>
                <td>%s%s%s</td>
                <td class="text-end stat-value">%,d</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-center">%s</td>
            </tr>
        """,
        rowClass, dataTarget,
        expandBtn, prefix, escapeHtml(stat.getTrackerId()),
        stat.getSampleCount(),
        stat.calculateAverage(),
        stat.calculatePercentile(50),
        stat.calculatePercentile(90),
        stat.calculatePercentile(95),
        stat.getMin(),
        stat.getMax(),
        formatThresholdBadge(stat)));
  }

  private static void appendHtmlChildRow(StringBuilder sb, MethodStatistics stat, int parentIndex) {
    sb.append(String.format("""
            <tr class="child-row child-row-%d d-none">
                <td class="ps-4"><span class="text-muted">└─</span> %s</td>
                <td class="text-end stat-value">%,d</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-end stat-value">%dms</td>
                <td class="text-center">%s</td>
            </tr>
        """,
        parentIndex,
        escapeHtml(stat.getTrackerId()),
        stat.getSampleCount(),
        stat.calculateAverage(),
        stat.calculatePercentile(50),
        stat.calculatePercentile(90),
        stat.calculatePercentile(95),
        stat.getMin(),
        stat.getMax(),
        formatThresholdBadge(stat)));
  }

  private static String formatThresholdBadge(MethodStatistics stat) {
    if (stat.getThresholdMs() <= 0) {
      return "<span class='text-muted'>-</span>";
    }
    return String.format(
        "<span class='badge bg-danger badge-threshold me-1' title='Exceeded'>↑%d</span>" +
            "<span class='badge bg-success badge-threshold' title='Within'>↓%d</span>",
        stat.getThresholdExceededCount(),
        stat.getThresholdWithinCount());
  }

  private static List<MethodStatistics> sortForDisplay(Collection<MethodStatistics> stats) {
    // Sort roots first, then children
    return stats.stream()
        .sorted((a, b) -> {
          if (a.getParentTrackerId() == null && b.getParentTrackerId() != null)
            return -1;
          if (a.getParentTrackerId() != null && b.getParentTrackerId() == null)
            return 1;
          return a.getTrackerId().compareTo(b.getTrackerId());
        })
        .collect(Collectors.toList());
  }

  private static String truncate(String str, int maxLen) {
    if (str.length() <= maxLen)
      return str;
    return str.substring(0, maxLen - 3) + "...";
  }

  private static String escapeJson(String str) {
    if (str == null)
      return "";
    return str.replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
  }

  private static String escapeHtml(String str) {
    if (str == null)
      return "";
    return str.replace("&", "&amp;")
        .replace("<", "&lt;")
        .replace(">", "&gt;")
        .replace("\"", "&quot;");
  }
}
