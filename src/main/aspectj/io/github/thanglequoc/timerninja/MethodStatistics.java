package io.github.thanglequoc.timerninja;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Statistics holder for a single tracker (identified by unique trackerId).
 * <p>
 * Stores execution time samples in a circular buffer with FIFO eviction.
 * All statistical calculations (average, percentiles) are performed on-demand
 * to minimize performance overhead during recording.
 */
public class MethodStatistics {

  private final String trackerId;
  private final String methodSignature;
  private final String className;
  private final int maxBufferSize;

  // Circular buffer for execution times (in milliseconds)
  private final List<Long> executionTimes;

  // Threshold tracking
  private int thresholdMs = -1;
  private int thresholdExceededCount = 0;
  private int thresholdWithinCount = 0;

  // Parent-child hierarchy
  private String parentTrackerId;
  private final List<String> childTrackerIds;

  /**
   * Creates a new MethodStatistics instance.
   *
   * @param trackerId       unique identifier for this tracker
   * @param className       fully qualified class name
   * @param methodSignature method signature (shortened or full)
   * @param maxBufferSize   maximum number of samples to keep
   */
  public MethodStatistics(String trackerId, String className, String methodSignature, int maxBufferSize) {
    this.trackerId = trackerId;
    this.className = className;
    this.methodSignature = methodSignature;
    this.maxBufferSize = maxBufferSize;
    this.executionTimes = new ArrayList<>();
    this.childTrackerIds = new ArrayList<>();
  }

  /**
   * Records an execution time sample.
   * If the buffer is full, the oldest entry is removed (FIFO eviction).
   *
   * @param executionTimeMs execution time in milliseconds
   * @param thresholdMs     threshold value (-1 if not set)
   */
  public synchronized void recordExecution(long executionTimeMs, int thresholdMs) {
    // FIFO eviction if buffer is full
    if (executionTimes.size() >= maxBufferSize) {
      executionTimes.remove(0);
    }
    executionTimes.add(executionTimeMs);

    // Update threshold tracking
    if (thresholdMs > 0) {
      this.thresholdMs = thresholdMs;
      if (executionTimeMs > thresholdMs) {
        thresholdExceededCount++;
      } else {
        thresholdWithinCount++;
      }
    }
  }

  /**
   * Returns the total number of recorded invocations.
   * Note: This may be greater than the buffer size if eviction has occurred.
   *
   * @return total invocation count
   */
  public int getInvocationCount() {
    return thresholdExceededCount + thresholdWithinCount +
        (thresholdMs <= 0 ? executionTimes.size() : 0);
  }

  /**
   * Returns the number of samples currently in the buffer.
   *
   * @return sample count
   */
  public synchronized int getSampleCount() {
    return executionTimes.size();
  }

  /**
   * Calculates the average execution time on-demand.
   *
   * @return average execution time in milliseconds, or 0 if no samples
   */
  public synchronized long calculateAverage() {
    if (executionTimes.isEmpty()) {
      return 0;
    }
    long sum = 0;
    for (Long time : executionTimes) {
      sum += time;
    }
    return sum / executionTimes.size();
  }

  /**
   * Calculates a percentile value on-demand.
   *
   * @param percentile the percentile to calculate (e.g., 50, 90, 95)
   * @return the percentile value in milliseconds, or 0 if no samples
   */
  public synchronized long calculatePercentile(int percentile) {
    if (executionTimes.isEmpty()) {
      return 0;
    }
    if (percentile < 0 || percentile > 100) {
      throw new IllegalArgumentException("Percentile must be between 0 and 100");
    }

    List<Long> sorted = new ArrayList<>(executionTimes);
    Collections.sort(sorted);

    int index = (int) Math.ceil((percentile / 100.0) * sorted.size()) - 1;
    index = Math.max(0, Math.min(index, sorted.size() - 1));
    return sorted.get(index);
  }

  /**
   * Returns the minimum execution time.
   *
   * @return minimum time in milliseconds, or 0 if no samples
   */
  public synchronized long getMin() {
    if (executionTimes.isEmpty()) {
      return 0;
    }
    long min = Long.MAX_VALUE;
    for (Long time : executionTimes) {
      if (time < min) {
        min = time;
      }
    }
    return min;
  }

  /**
   * Returns the maximum execution time.
   *
   * @return maximum time in milliseconds, or 0 if no samples
   */
  public synchronized long getMax() {
    if (executionTimes.isEmpty()) {
      return 0;
    }
    long max = Long.MIN_VALUE;
    for (Long time : executionTimes) {
      if (time > max) {
        max = time;
      }
    }
    return max;
  }

  /**
   * Resets all statistics for this tracker.
   */
  public synchronized void reset() {
    executionTimes.clear();
    thresholdExceededCount = 0;
    thresholdWithinCount = 0;
    childTrackerIds.clear();
  }

  // --- Getters ---

  public String getTrackerId() {
    return trackerId;
  }

  public String getMethodSignature() {
    return methodSignature;
  }

  public String getClassName() {
    return className;
  }

  public int getThresholdMs() {
    return thresholdMs;
  }

  public int getThresholdExceededCount() {
    return thresholdExceededCount;
  }

  public int getThresholdWithinCount() {
    return thresholdWithinCount;
  }

  public String getParentTrackerId() {
    return parentTrackerId;
  }

  public void setParentTrackerId(String parentTrackerId) {
    this.parentTrackerId = parentTrackerId;
  }

  public List<String> getChildTrackerIds() {
    return new ArrayList<>(childTrackerIds);
  }

  public synchronized void addChildTrackerId(String childTrackerId) {
    if (!childTrackerIds.contains(childTrackerId)) {
      childTrackerIds.add(childTrackerId);
    }
  }

  /**
   * Returns a copy of the execution times buffer.
   * Useful for testing and debugging.
   *
   * @return copy of execution times list
   */
  public synchronized List<Long> getExecutionTimesCopy() {
    return new ArrayList<>(executionTimes);
  }
}
