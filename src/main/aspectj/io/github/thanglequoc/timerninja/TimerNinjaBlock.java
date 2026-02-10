package io.github.thanglequoc.timerninja;

import java.time.temporal.ChronoUnit;
import java.util.UUID;
import java.util.function.Supplier;

import static io.github.thanglequoc.timerninja.TimerNinjaThreadContext.LOGGER;

/**
 * Utility class for measuring execution time of arbitrary code blocks.
 * <p>
 * This class provides static methods to wrap any code block with time tracking functionality,
 * similar to how {@link TimerNinjaTracker} annotation works for methods and constructors.
 * Code blocks tracked with this class will be integrated into the existing Timer Ninja
 * tracking context if called from within an already-tracked method.
 * </p>
 * <p>
 * The tracking context is managed by {@link TimerNinjaContextManager}, ensuring that
 * code blocks tracked with this class share the same trace context as methods and constructors
 * tracked with {@link TimerNinjaTracker}.
 * </p>
 * <p>
 * Example usage:
 * <pre>
 * TimerNinjaBlock.measure(() -> {
 *     // Your code here
 *     doSomething();
 * });
 * </pre>
 * <p>
 * Example with custom name:
 * <pre>
 * TimerNinjaBlock.measure("data processing", () -> {
 *     processData(data);
 * });
 * </pre>
 * <p>
 * Example with configuration:
 * <pre>
 * BlockTrackerConfig config = new BlockTrackerConfig()
 *     .setTimeUnit(ChronoUnit.SECONDS)
 *     .setThreshold(5);
 * TimerNinjaBlock.measure("expensive operation", config, () -> {
 *     expensiveOperation();
 * });
 * </pre>
 */
public class TimerNinjaBlock {

    /**
     * Measures the execution time of a code block with default configuration.
     * The block will be named "Code Block" by default.
     *
     * @param codeBlock the code block to measure
     */
    public static void measure(Runnable codeBlock) {
        measure("Code Block", null, codeBlock);
    }

    /**
     * Measures the execution time of a code block with a custom name.
     *
     * @param blockName the name to identify this code block in the trace
     * @param codeBlock the code block to measure
     */
    public static void measure(String blockName, Runnable codeBlock) {
        measure(blockName, null, codeBlock);
    }

    /**
     * Measures the execution time of a code block with custom configuration.
     *
     * @param blockName the name to identify this code block in the trace
     * @param config the configuration for this code block (can be null for defaults)
     * @param codeBlock the code block to measure
     */
    public static void measure(String blockName, BlockTrackerConfig config, Runnable codeBlock) {
        if (codeBlock == null) {
            throw new IllegalArgumentException("Code block cannot be null");
        }
        
        // Use default configuration if none provided
        BlockTrackerConfig actualConfig = config != null ? config : new BlockTrackerConfig();
        
        // Initialize tracking context if needed
        if (TimerNinjaContextManager.isTrackingContextNull()) {
            TimerNinjaContextManager.initTrackingContext();
        }

        ThreadLocal<TimerNinjaThreadContext> localTrackingCtx = TimerNinjaContextManager.getLocalTrackingCtx();
        TimerNinjaThreadContext trackingCtx = localTrackingCtx.get();
        String traceContextId = trackingCtx.getTraceContextId();
        boolean isTrackerEnabled = actualConfig.isEnabled();

        TrackerItemContext trackerItemContext = new TrackerItemContext(
            trackingCtx.getPointerDepth(), 
            blockName
        );
        
        // Set threshold if configured
        if (actualConfig.getThreshold() > 0) {
            trackerItemContext.setThreshold(actualConfig.getThreshold());
        }

        String uuid = UUID.randomUUID().toString();
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        long threadId = currentThread.getId();

        if (isTrackerEnabled) {
            LOGGER.debug("{} ({})|{}| TrackerItemContext {} initiated, start tracking on code block: {}",
                threadName, threadId, traceContextId, uuid, blockName);
            trackingCtx.addItemContext(uuid, trackerItemContext);
            trackingCtx.increasePointerDepth();
        }

        // Execute code block
        long startTime = System.currentTimeMillis();
        try {
            codeBlock.run();
        } catch (Exception e) {
            LOGGER.warn("{} ({})|{}| Exception occurred in code block {}: {}",
                threadName, threadId, traceContextId, blockName, e.getMessage());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();

            if (isTrackerEnabled) {
                LOGGER.debug("{} ({})|{}| TrackerItemContext {} finished tracking on code block: {}. Evaluating execution time...",
                    threadName, threadId, traceContextId, uuid, blockName);
                ChronoUnit trackingTimeUnit = actualConfig.getTimeUnit();
                long executionTime = TimerNinjaUtil.convertFromMillis(endTime - startTime, trackingTimeUnit);
                trackerItemContext.setExecutionTime(executionTime);
                trackerItemContext.setTimeUnit(trackingTimeUnit);
                LOGGER.debug("{} ({})|{}| TrackerItemContext: {}", threadName, threadId, traceContextId, trackerItemContext);
                trackingCtx.decreasePointerDepth();
            }

            // Check if tracking is complete
            if (trackingCtx.getPointerDepth() == 0) {
                TimerNinjaUtil.logTimerContextTrace(trackingCtx);
                localTrackingCtx.remove();
                LOGGER.debug("{} ({})| TimerNinjaTracking context {} is completed and has been removed",
                    threadName, threadId, traceContextId);
            }
        }
    }

    /**
     * Measures the execution time of a code block that returns a value.
     *
     * @param codeBlock the code block to measure
     * @param <T> the return type of the code block
     * @return the result of the code block execution
     */
    public static <T> T measure(Supplier<T> codeBlock) {
        return measure("Code Block", null, codeBlock);
    }

    /**
     * Measures the execution time of a code block that returns a value with a custom name.
     *
     * @param blockName the name to identify this code block in the trace
     * @param codeBlock the code block to measure
     * @param <T> the return type of the code block
     * @return the result of the code block execution
     */
    public static <T> T measure(String blockName, Supplier<T> codeBlock) {
        return measure(blockName, null, codeBlock);
    }

    /**
     * Measures the execution time of a code block that returns a value with custom configuration.
     *
     * @param blockName the name to identify this code block in the trace
     * @param config the configuration for this code block (can be null for defaults)
     * @param codeBlock the code block to measure
     * @param <T> the return type of the code block
     * @return the result of the code block execution
     */
    public static <T> T measure(String blockName, BlockTrackerConfig config, Supplier<T> codeBlock) {
        if (codeBlock == null) {
            throw new IllegalArgumentException("Code block cannot be null");
        }
        
        // Use default configuration if none provided
        BlockTrackerConfig actualConfig = config != null ? config : new BlockTrackerConfig();
        
        // Initialize tracking context if needed
        if (TimerNinjaContextManager.isTrackingContextNull()) {
            TimerNinjaContextManager.initTrackingContext();
        }

        ThreadLocal<TimerNinjaThreadContext> localTrackingCtx = TimerNinjaContextManager.getLocalTrackingCtx();
        TimerNinjaThreadContext trackingCtx = localTrackingCtx.get();
        String traceContextId = trackingCtx.getTraceContextId();
        boolean isTrackerEnabled = actualConfig.isEnabled();

        TrackerItemContext trackerItemContext = new TrackerItemContext(
            trackingCtx.getPointerDepth(), 
            blockName
        );
        
        // Set threshold if configured
        if (actualConfig.getThreshold() > 0) {
            trackerItemContext.setThreshold(actualConfig.getThreshold());
        }

        String uuid = UUID.randomUUID().toString();
        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        long threadId = currentThread.getId();

        if (isTrackerEnabled) {
            LOGGER.debug("{} ({})|{}| TrackerItemContext {} initiated, start tracking on code block: {}",
                threadName, threadId, traceContextId, uuid, blockName);
            trackingCtx.addItemContext(uuid, trackerItemContext);
            trackingCtx.increasePointerDepth();
        }

        // Execute code block
        long startTime = System.currentTimeMillis();
        T result;
        try {
            result = codeBlock.get();
        } catch (Exception e) {
            LOGGER.warn("{} ({})|{}| Exception occurred in code block {}: {}",
                threadName, threadId, traceContextId, blockName, e.getMessage());
            throw e;
        } finally {
            long endTime = System.currentTimeMillis();

            if (isTrackerEnabled) {
                LOGGER.debug("{} ({})|{}| TrackerItemContext {} finished tracking on code block: {}. Evaluating execution time...",
                    threadName, threadId, traceContextId, uuid, blockName);
                ChronoUnit trackingTimeUnit = actualConfig.getTimeUnit();
                long executionTime = TimerNinjaUtil.convertFromMillis(endTime - startTime, trackingTimeUnit);
                trackerItemContext.setExecutionTime(executionTime);
                trackerItemContext.setTimeUnit(trackingTimeUnit);
                LOGGER.debug("{} ({})|{}| TrackerItemContext: {}", threadName, threadId, traceContextId, trackerItemContext);
                trackingCtx.decreasePointerDepth();
            }

            // Check if tracking is complete
            if (trackingCtx.getPointerDepth() == 0) {
                TimerNinjaUtil.logTimerContextTrace(trackingCtx);
                localTrackingCtx.remove();
                LOGGER.debug("{} ({})| TimerNinjaTracking context {} is completed and has been removed",
                    threadName, threadId, traceContextId);
            }
        }

        return result;
    }

}
