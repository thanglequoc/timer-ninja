package io.github.thanglequoc.timerninja;

import java.time.temporal.ChronoUnit;

/**
 * Represents a single tracker item context for a method annotated with {@code @TimerNinjaTracker}.
 * <p>
 * This context is created and updated during the AspectJ advice invocation
 * to capture execution details, arguments, timing, and threshold information.
 */
public class TrackerItemContext {

    /**
     * The relative pointer depth of this method compared to the root tracked method.
     */
    private final int pointerDepth;

    /**
     * The name of the tracked method.
     */
    private String methodName;

    /**
     * The string representation of the arguments passed to the tracked method.
     */
    private String arguments;

    /**
     * Flag indicating whether argument logging is enabled for this item.
     */
    private boolean includeArgs = false;

    /**
     * The total execution time of this method.
     */
    private long executionTime;

    /**
     * The time unit used for execution time measurements.
     */
    private ChronoUnit timeUnit;

    /**
     * The threshold configuration for this tracker item. Default is -1, meaning no threshold.
     */
    private int threshold = -1;

    /**
     * Constructs a tracker item context for a method without argument information.
     *
     * @param pointerDepth the pointer depth of this item relative to the root
     * @param methodName the name of the tracked method
     */
    TrackerItemContext(int pointerDepth, String methodName) {
        this.pointerDepth = pointerDepth;
        this.methodName = methodName;
    }

    /**
     * Constructs a tracker item context including argument information.
     *
     * @param pointerDepth the pointer depth of this item relative to the root
     * @param methodName the name of the tracked method
     * @param arguments the argument information for this method
     * @param includeArgs whether argument logging is enabled
     */
    TrackerItemContext(int pointerDepth, String methodName, String arguments, boolean includeArgs) {
        this.pointerDepth = pointerDepth;
        this.methodName = methodName;
        this.arguments = arguments;
        this.includeArgs = includeArgs;
    }

    /**
     * Returns the pointer depth of this method.
     *
     * @return the pointer depth
     */
    public int getPointerDepth() {
        return pointerDepth;
    }


    /**
     * Sets the execution time of this method.
     *
     * @param executionTime the execution time
     */
    public void setExecutionTime(long executionTime) {
        this.executionTime = executionTime;
    }

    /**
     * Returns the execution time of this method.
     *
     * @return the execution time
     */
    public long getExecutionTime() {
        return executionTime;
    }

    /**
     * Sets the time unit for execution time measurement.
     *
     * @param timeUnit the {@link ChronoUnit} to use
     */
    public void setTimeUnit(ChronoUnit timeUnit) {
        this.timeUnit = timeUnit;
    }

    /**
     * Returns the time unit of this method.
     *
     * @return the {@link ChronoUnit} used for execution time
     */
    public ChronoUnit getTimeUnit() {
        return timeUnit;
    }

    /**
     * Returns the method name being tracked.
     *
     * @return the method name
     */
    public String getMethodName() {
        return methodName;
    }

    /**
     * Sets the argument information for this method.
     *
     * @param arguments the argument string
     */
    public void setArguments(String arguments) {
        this.arguments = arguments;
    }

    /**
     * Returns the argument information of this method.
     *
     * @return the argument string
     */
    public String getArguments() {
        return arguments;
    }

    /**
     * Returns whether argument logging is enabled for this method.
     *
     * @return {@code true} if arguments are included; {@code false} otherwise
     */
    public boolean isIncludeArgs() {
        return includeArgs;
    }

    /**
     * Sets the threshold for this method.
     *
     * @param threshold the threshold value in the same unit as execution time
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }


    /**
     * Returns the threshold for this method.
     *
     * @return the threshold value, or -1 if no threshold is set
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * Checks whether threshold monitoring is enabled for this method.
     *
     * @return {@code true} if a threshold is set (not -1), {@code false} otherwise
     */
    public boolean isEnableThreshold() {
        return threshold != -1;
    }

    /**
     * Returns a string representation of this tracker item, including pointer depth,
     * method name, execution time, time unit, and argument details.
     *
     * @return a string representation of this tracker item
     */
    @Override
    public String toString() {
        return "TrackerItemContext{" + "pointerDepth=" + pointerDepth + ", methodName='" + methodName + '\'' + ", executionTime=" + executionTime + ", timeUnit=" + timeUnit + ", args=[" + arguments + "]" + '}';
    }
}
