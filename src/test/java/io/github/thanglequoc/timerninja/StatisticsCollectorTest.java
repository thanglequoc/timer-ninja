package io.github.thanglequoc.timerninja;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for StatisticsCollector class.
 */
class StatisticsCollectorTest {

  private StatisticsCollector collector;

  @BeforeEach
  void setUp() {
    collector = StatisticsCollector.getInstance();
    collector.reset();
  }

  @Test
  @DisplayName("Should record execution and create new statistics entry")
  void testRecordExecution() {
    collector.recordExecution("TestClass.method1", "TestClass", "method1()", 100, -1, null);

    assertTrue(collector.hasTracker("TestClass.method1"));
    assertEquals(1, collector.getTrackedMethodCount());

    MethodStatistics stats = collector.getStatistics("TestClass.method1");
    assertNotNull(stats);
    assertEquals(1, stats.getSampleCount());
    assertEquals(100, stats.calculateAverage());
  }

  @Test
  @DisplayName("Should aggregate multiple executions for same tracker")
  void testMultipleExecutions() {
    collector.recordExecution("TestClass.method1", "TestClass", "method1()", 100, -1, null);
    collector.recordExecution("TestClass.method1", "TestClass", "method1()", 200, -1, null);
    collector.recordExecution("TestClass.method1", "TestClass", "method1()", 300, -1, null);

    assertEquals(1, collector.getTrackedMethodCount());

    MethodStatistics stats = collector.getStatistics("TestClass.method1");
    assertEquals(3, stats.getSampleCount());
    assertEquals(200, stats.calculateAverage());
  }

  @Test
  @DisplayName("Should track multiple different methods")
  void testMultipleMethods() {
    collector.recordExecution("ClassA.method1", "ClassA", "method1()", 100, -1, null);
    collector.recordExecution("ClassB.method2", "ClassB", "method2()", 200, -1, null);
    collector.recordExecution("ClassC.method3", "ClassC", "method3()", 300, -1, null);

    assertEquals(3, collector.getTrackedMethodCount());
    assertTrue(collector.hasTracker("ClassA.method1"));
    assertTrue(collector.hasTracker("ClassB.method2"));
    assertTrue(collector.hasTracker("ClassC.method3"));
  }

  @Test
  @DisplayName("Should establish parent-child relationships")
  void testParentChildRelationships() {
    // Parent method first
    collector.recordExecution("Parent.method", "Parent", "method()", 500, -1, null);
    // Child method with parent reference
    collector.recordExecution("Child.method", "Child", "method()", 100, -1, "Parent.method");

    MethodStatistics childStats = collector.getStatistics("Child.method");
    assertEquals("Parent.method", childStats.getParentTrackerId());

    MethodStatistics parentStats = collector.getStatistics("Parent.method");
    assertTrue(parentStats.getChildTrackerIds().contains("Child.method"));
  }

  @Test
  @DisplayName("Should reset all statistics")
  void testReset() {
    collector.recordExecution("Test.method1", "Test", "method1()", 100, -1, null);
    collector.recordExecution("Test.method2", "Test", "method2()", 200, -1, null);

    assertEquals(2, collector.getTrackedMethodCount());

    collector.reset();

    assertEquals(0, collector.getTrackedMethodCount());
    assertFalse(collector.hasTracker("Test.method1"));
    assertFalse(collector.hasTracker("Test.method2"));
  }

  @Test
  @DisplayName("Should get all statistics")
  void testGetAllStatistics() {
    collector.recordExecution("Test.method1", "Test", "method1()", 100, -1, null);
    collector.recordExecution("Test.method2", "Test", "method2()", 200, -1, null);

    Collection<MethodStatistics> allStats = collector.getAllStatistics();
    assertEquals(2, allStats.size());
  }

  @Test
  @DisplayName("Should respect buffer size configuration")
  void testBufferSizeConfiguration() {
    collector.setMaxBufferSize(5);
    assertEquals(5, collector.getMaxBufferSize());

    assertThrows(IllegalArgumentException.class, () -> collector.setMaxBufferSize(0));
    assertThrows(IllegalArgumentException.class, () -> collector.setMaxBufferSize(-1));
  }

  @Test
  @DisplayName("Should be singleton")
  void testSingleton() {
    StatisticsCollector instance1 = StatisticsCollector.getInstance();
    StatisticsCollector instance2 = StatisticsCollector.getInstance();

    assertSame(instance1, instance2);
  }
}
