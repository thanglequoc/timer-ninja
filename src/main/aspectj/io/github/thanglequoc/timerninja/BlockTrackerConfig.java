package io.github.thanglequoc.timerninja;

import java.time.temporal.ChronoUnit;

/**
 * Configuration class for code block tracking in {@link TimerNinjaBlock}.
 * <p>
 * This class provides a builder-style API to configure how a code block should be tracked,
 * similar to the options available in {@link TimerNinjaTracker} annotation.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * BlockTrackerConfig config = new BlockTrackerConfig()
 *     .setTimeUnit(ChronoUnit.SECONDS)
 *     .setEnabled(true)
 *     .setThreshold(5);
 * 
 * TimerNinjaBlock.measure("my block", config, () -> {
 *     // code to track
 * });
 * </pre>
 */
public class BlockTrackerConfig {

    /**
     * The time unit to use for the tracker.
     * Supported time units: second, millisecond (default), microsecond
     */
    private ChronoUnit timeUnit = ChronoUnit.MILLIS;

    /**
     * Determine if this tracker should be active.
     * Set to false will disable this tracker from the overall tracking trace result.
     */
    private boolean enabled = true;

    /**
     * Set the threshold of the tracker. If the execution time of the code block is less than
     * the threshold, the tracker will not be included in the tracking result.
     * Default is 0, which means no threshold is set.
     */
    private int threshold = 0;

    /**
     * Default constructor creating a config with default values:
     * <ul>
     *   <li>timeUnit: MILLIS</li>
     *   <li>enabled: true</li>
     *   <li>threshold: 0 (no threshold)</li>
     * </ul>
     */
    public BlockTrackerConfig() {
    }

    /**
     * Sets the time unit for tracking execution time.
     *
     * @param timeUnit the time unit to use (MILLIS, SECONDS, or MICROS)
     * @return this config instance for method chaining
     */
    public BlockTrackerConfig setTimeUnit(ChronoUnit timeUnit) {
        if (timeUnit == null) {
            throw new IllegalArgumentException("Time unit cannot be null");
        }
        this.timeUnit = timeUnit;
        return this;
    }

    /**
     * Sets whether this tracker is enabled.
     *
     * @param enabled true to enable tracking, false to disable
     * @return this config instance for method chaining
     */
    public BlockTrackerConfig setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * Sets the threshold for execution time.
     * Code blocks with execution time less than this threshold will not be included in the trace.
     *
     * @param threshold the threshold value in the configured time unit.
     *                  Set to 0 or negative to disable threshold filtering.
     * @return this config instance for method chaining
     */
    public BlockTrackerConfig setThreshold(int threshold) {
        this.threshold = threshold;
        return this;
    }

    /**
     * Gets the configured time unit.
     *
     * @return the time unit for tracking
     */
    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Checks if this tracker is enabled.
     *
     * @return true if enabled, false otherwise
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Gets the configured threshold.
     *
     * @return the threshold value (0 or negative means no threshold)
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Creates a copy of this configuration.
     *
     * @return a new BlockTrackerConfig instance with the same values
     */
    public BlockTrackerConfig copy() {
        BlockTrackerConfig copy = new BlockTrackerConfig();
        copy.timeUnit = this.timeUnit;
        copy.enabled = this.enabled;
        copy.threshold = this.threshold;
        return copy;
    }

    @Override
    public String toString() {
        return "BlockTrackerConfig{" +
                "timeUnit=" + timeUnit +
                ", enabled=" + enabled +
                ", threshold=" + threshold +
                '}';
    }
}