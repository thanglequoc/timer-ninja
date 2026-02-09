package io.github.thanglequoc.timerninja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StatisticsReportGenerator class.
 */
class StatisticsReportGeneratorTest {

  private List<MethodStatistics> stats;

  @BeforeEach
  void setUp() {
    stats = new ArrayList<>();

    // Create sample statistics
    MethodStatistics stat1 = new MethodStatistics("UserService.getUser", "UserService", "getUser()", 100);
    stat1.recordExecution(50, 100);
    stat1.recordExecution(150, 100);
    stat1.recordExecution(100, 100);
    stats.add(stat1);

    MethodStatistics stat2 = new MethodStatistics("OrderService.processOrder", "OrderService", "processOrder()", 100);
    stat2.recordExecution(200, -1);
    stat2.recordExecution(300, -1);
    stats.add(stat2);
  }

  @Test
  @DisplayName("Should generate text report")
  void testGenerateTextReport() {
    String report = StatisticsReportGenerator.generateTextReport(stats);

    assertNotNull(report);
    assertTrue(report.contains("Timer Ninja Statistics Report"));
    assertTrue(report.contains("UserService.getUser"));
    assertTrue(report.contains("OrderService.processOrder"));
    assertTrue(report.contains("Generated:"));
  }

  @Test
  @DisplayName("Should generate JSON report")
  void testGenerateJsonReport() {
    String report = StatisticsReportGenerator.generateJsonReport(stats);

    assertNotNull(report);
    assertTrue(report.contains("\"generatedAt\""));
    assertTrue(report.contains("\"totalMethods\": 2"));
    assertTrue(report.contains("\"trackerId\": \"UserService.getUser\""));
    assertTrue(report.contains("\"trackerId\": \"OrderService.processOrder\""));
    assertTrue(report.contains("\"avgMs\""));
    assertTrue(report.contains("\"p50Ms\""));
    assertTrue(report.contains("\"p90Ms\""));
  }

  @Test
  @DisplayName("Should generate HTML report with Bootstrap")
  void testGenerateHtmlReport() {
    String report = StatisticsReportGenerator.generateHtmlReport(stats);

    assertNotNull(report);
    assertTrue(report.contains("<!DOCTYPE html>"));
    assertTrue(report.contains("bootstrap"));
    assertTrue(report.contains("Timer Ninja Statistics Report"));
    assertTrue(report.contains("UserService.getUser"));
    assertTrue(report.contains("<table"));
  }

  @Test
  @DisplayName("Should generate report as bytes")
  void testGenerateReportAsBytes() {
    byte[] textBytes = StatisticsReportGenerator.generateReportAsBytes(stats, "text");
    byte[] jsonBytes = StatisticsReportGenerator.generateReportAsBytes(stats, "json");
    byte[] htmlBytes = StatisticsReportGenerator.generateReportAsBytes(stats, "html");

    assertTrue(textBytes.length > 0);
    assertTrue(jsonBytes.length > 0);
    assertTrue(htmlBytes.length > 0);

    // Verify content
    String textContent = new String(textBytes);
    assertTrue(textContent.contains("Timer Ninja"));
  }

  @Test
  @DisplayName("Should handle empty statistics")
  void testEmptyStatistics() {
    List<MethodStatistics> emptyStats = new ArrayList<>();

    String textReport = StatisticsReportGenerator.generateTextReport(emptyStats);
    assertTrue(textReport.contains("No statistics recorded"));

    String jsonReport = StatisticsReportGenerator.generateJsonReport(emptyStats);
    assertTrue(jsonReport.contains("\"totalMethods\": 0"));

    String htmlReport = StatisticsReportGenerator.generateHtmlReport(emptyStats);
    assertTrue(htmlReport.contains("No statistics recorded"));
  }

  @Test
  @DisplayName("Should use format parameter correctly")
  void testFormatParameter() {
    String text = StatisticsReportGenerator.generateReport(stats, "text");
    String json = StatisticsReportGenerator.generateReport(stats, "json");
    String html = StatisticsReportGenerator.generateReport(stats, "html");
    String defaultFormat = StatisticsReportGenerator.generateReport(stats, "unknown");

    // Default falls back to text
    assertTrue(defaultFormat.contains("Timer Ninja Statistics Report"));
    assertFalse(defaultFormat.contains("<!DOCTYPE")); // Not HTML

    // JSON should be parseable structure
    assertTrue(json.startsWith("{"));
    assertTrue(json.endsWith("}\n"));

    // HTML should have doctype
    assertTrue(html.contains("<!DOCTYPE html>"));
  }

  @Test
  @DisplayName("Should show threshold information in report")
  void testThresholdInReport() {
    String textReport = StatisticsReportGenerator.generateTextReport(stats);
    String jsonReport = StatisticsReportGenerator.generateJsonReport(stats);

    // Text report should show threshold counts
    assertTrue(textReport.contains("Threshold"));

    // JSON should include threshold fields
    assertTrue(jsonReport.contains("\"thresholdMs\""));
    assertTrue(jsonReport.contains("\"thresholdExceeded\""));
    assertTrue(jsonReport.contains("\"thresholdWithin\""));
  }

  @Test
  @DisplayName("Should escape special characters in JSON")
  void testJsonEscaping() {
    MethodStatistics specialStat = new MethodStatistics(
        "Test.method\"with'special",
        "Test",
        "method\\path",
        100);
    specialStat.recordExecution(100, -1);

    List<MethodStatistics> specialStats = new ArrayList<>();
    specialStats.add(specialStat);

    String json = StatisticsReportGenerator.generateJsonReport(specialStats);

    // Should be valid JSON (special chars escaped)
    assertTrue(json.contains("\\\""));
    assertTrue(json.contains("\\\\"));
  }
}
