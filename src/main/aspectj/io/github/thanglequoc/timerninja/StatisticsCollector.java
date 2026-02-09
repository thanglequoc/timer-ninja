package io.github.thanglequoc.timerninja;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Singleton class managing statistics collection for all tracked methods.
 * <p>
 * This collector aggregates execution time data for methods and code blocks
 * that are annotated with {@link TimerNinjaTracker} or measured using
 * {@link TimerNinjaBlock}.
 * <p>
 * Thread-safe implementation using {@link ConcurrentHashMap}.
 */
public class StatisticsCollector {

  private static volatile StatisticsCollector instance;

  // Tracker ID -> Statistics
  private final ConcurrentHashMap<String, MethodStatistics> statisticsMap;
  private int maxBufferSizePerMethod = 1000;

  private StatisticsCollector() {
    this.statisticsMap = new ConcurrentHashMap<>();
  }

  /**
   * Returns the singleton instance of StatisticsCollector.
   *
   * @return the singleton instance
   */
  public static StatisticsCollector getInstance() {
    if (instance == null) {
      synchronized (StatisticsCollector.class) {
        if (instance == null) {
          instance = new StatisticsCollector();
        }
      }
    }
    return instance;
  }

  /**
   * Records an execution time for a tracked method or block.
   * <p>
   * If this is the first recording for the given trackerId, a new
   * {@link MethodStatistics} instance will be created.
   *
   * @param trackerId       unique identifier for this tracker
   * @param className       fully qualified class name
   * @param methodSignature method signature
   * @param executionTimeMs execution time in milliseconds
   * @param thresholdMs     threshold value (-1 if not set)
   * @param parentTrackerId parent tracker ID for hierarchy (null if root)
   */
  public void recordExecution(String trackerId, String className, String methodSignature,
      long executionTimeMs, int thresholdMs, String parentTrackerId) {

    MethodStatistics stats = statisticsMap.computeIfAbsent(trackerId,
        id -> new MethodStatistics(id, className, methodSignature, maxBufferSizePerMethod));

    stats.recordExecution(executionTimeMs, thresholdMs);

    // Set parent relationship
    if (parentTrackerId != null && !parentTrackerId.isEmpty()) {
      stats.setParentTrackerId(parentTrackerId);

      // Add this as a child to the parent
      MethodStatistics parentStats = statisticsMap.get(parentTrackerId);
      if (parentStats != null) {
        parentStats.addChildTrackerId(trackerId);
      }
    }
  }

  /**
   * Gets statistics for a specific tracker.
   *
   * @param trackerId the tracker ID
   * @return the statistics, or null if not found
   */
  public MethodStatistics getStatistics(String trackerId) {
    return statisticsMap.get(trackerId);
  }

  /**
   * Gets all statistics.
   *
   * @return collection of all MethodStatistics
   */
  public Collection<MethodStatistics> getAllStatistics() {
    return statisticsMap.values();
  }

  /**
   * Gets the statistics map.
   *
   * @return the statistics map
   */
  public ConcurrentHashMap<String, MethodStatistics> getStatisticsMap() {
    return statisticsMap;
  }

  /**
   * Resets all collected statistics.
   */
  public void reset() {
    statisticsMap.clear();
  }

  /**
   * Sets the maximum buffer size per method.
   *
   * @param size maximum number of samples to keep per method
   */
  public void setMaxBufferSize(int size) {
    if (size < 1) {
      throw new IllegalArgumentException("Buffer size must be at least 1");
    }
    this.maxBufferSizePerMethod = size;
  }

  /**
   * Gets the maximum buffer size per method.
   *
   * @return the maximum buffer size
   */
  public int getMaxBufferSize() {
    return maxBufferSizePerMethod;
  }

  /**
   * Returns the count of tracked methods.
   *
   * @return number of unique tracker IDs
   */
  public int getTrackedMethodCount() {
    return statisticsMap.size();
  }

  /**
   * Checks if a tracker ID exists.
   *
   * @param trackerId the tracker ID to check
   * @return true if the tracker exists
   */
  public boolean hasTracker(String trackerId) {
    return statisticsMap.containsKey(trackerId);
  }
}
