package io.github.thanglequoc.timerninja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MethodStatistics class.
 */
class MethodStatisticsTest {

  private MethodStatistics stats;

  @BeforeEach
  void setUp() {
    stats = new MethodStatistics("TestClass.testMethod", "TestClass", "testMethod()", 100);
  }

  @Test
  @DisplayName("Should record execution time correctly")
  void testRecordExecution() {
    stats.recordExecution(100, -1);
    stats.recordExecution(200, -1);
    stats.recordExecution(150, -1);

    assertEquals(3, stats.getSampleCount());
    List<Long> times = stats.getExecutionTimesCopy();
    assertTrue(times.contains(100L));
    assertTrue(times.contains(200L));
    assertTrue(times.contains(150L));
  }

  @Test
  @DisplayName("Should calculate average correctly")
  void testCalculateAverage() {
    stats.recordExecution(100, -1);
    stats.recordExecution(200, -1);
    stats.recordExecution(300, -1);

    assertEquals(200, stats.calculateAverage());
  }

  @Test
  @DisplayName("Should calculate percentiles correctly")
  void testCalculatePercentile() {
    // Add 10 samples: 10, 20, 30, 40, 50, 60, 70, 80, 90, 100
    for (int i = 1; i <= 10; i++) {
      stats.recordExecution(i * 10, -1);
    }

    assertEquals(50, stats.calculatePercentile(50)); // p50 = 50
    assertEquals(90, stats.calculatePercentile(90)); // p90 = 90
    assertEquals(100, stats.calculatePercentile(95)); // p95 = 100
  }

  @Test
  @DisplayName("Should track min and max correctly")
  void testMinMax() {
    stats.recordExecution(50, -1);
    stats.recordExecution(100, -1);
    stats.recordExecution(25, -1);
    stats.recordExecution(75, -1);

    assertEquals(25, stats.getMin());
    assertEquals(100, stats.getMax());
  }

  @Test
  @DisplayName("Should track threshold exceeded and within counts")
  void testThresholdTracking() {
    // Threshold = 100ms
    stats.recordExecution(50, 100); // within
    stats.recordExecution(150, 100); // exceeded
    stats.recordExecution(100, 100); // within (equal)
    stats.recordExecution(200, 100); // exceeded

    assertEquals(2, stats.getThresholdExceededCount());
    assertEquals(2, stats.getThresholdWithinCount());
    assertEquals(100, stats.getThresholdMs());
  }

  @Test
  @DisplayName("Should implement FIFO eviction when buffer is full")
  void testFifoEviction() {
    // Create a small buffer of 3
    MethodStatistics smallStats = new MethodStatistics("test", "Test", "test()", 3);

    smallStats.recordExecution(10, -1);
    smallStats.recordExecution(20, -1);
    smallStats.recordExecution(30, -1);
    assertEquals(3, smallStats.getSampleCount());

    // Add 4th item - should evict the first (10)
    smallStats.recordExecution(40, -1);
    assertEquals(3, smallStats.getSampleCount());

    List<Long> times = smallStats.getExecutionTimesCopy();
    assertFalse(times.contains(10L));
    assertTrue(times.contains(20L));
    assertTrue(times.contains(30L));
    assertTrue(times.contains(40L));
  }

  @Test
  @DisplayName("Should return 0 when no samples recorded")
  void testEmptyStatistics() {
    assertEquals(0, stats.calculateAverage());
    assertEquals(0, stats.calculatePercentile(50));
    assertEquals(0, stats.getMin());
    assertEquals(0, stats.getMax());
    assertEquals(0, stats.getSampleCount());
  }

  @Test
  @DisplayName("Should reset all statistics correctly")
  void testReset() {
    stats.recordExecution(100, 50);
    stats.recordExecution(200, 50);
    stats.addChildTrackerId("child1");

    stats.reset();

    assertEquals(0, stats.getSampleCount());
    assertEquals(0, stats.getThresholdExceededCount());
    assertEquals(0, stats.getThresholdWithinCount());
    assertTrue(stats.getChildTrackerIds().isEmpty());
  }

  @Test
  @DisplayName("Should track parent-child relationships")
  void testParentChildRelationships() {
    stats.setParentTrackerId("Parent.method");
    stats.addChildTrackerId("Child.method1");
    stats.addChildTrackerId("Child.method2");
    stats.addChildTrackerId("Child.method1"); // Duplicate, should not be added

    assertEquals("Parent.method", stats.getParentTrackerId());
    assertEquals(2, stats.getChildTrackerIds().size());
    assertTrue(stats.getChildTrackerIds().contains("Child.method1"));
    assertTrue(stats.getChildTrackerIds().contains("Child.method2"));
  }

  @Test
  @DisplayName("Should throw exception for invalid percentile")
  void testInvalidPercentile() {
    stats.recordExecution(100, -1);

    assertThrows(IllegalArgumentException.class, () -> stats.calculatePercentile(-1));
    assertThrows(IllegalArgumentException.class, () -> stats.calculatePercentile(101));
  }
}
