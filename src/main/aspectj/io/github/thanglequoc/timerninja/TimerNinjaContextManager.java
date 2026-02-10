package io.github.thanglequoc.timerninja;

/**
 * Central manager for Timer Ninja tracking context.
 * <p>
 * This class manages the ThreadLocal tracking context that is shared between
 * {@link TimeTrackingAspect} and {@link TimerNinjaBlock}, ensuring that code blocks
 * tracked with {@link TimerNinjaBlock} are integrated into the same trace context
 * as methods and constructors tracked with {@link TimerNinjaTracker}.
 * </p>
 */
public class TimerNinjaContextManager {

    /**
     * The shared ThreadLocal tracking context used by both the aspect and code block tracking.
     * This is a single, stable reference shared across all threads where each thread stores its own value.
     */
    private static final ThreadLocal<TimerNinjaThreadContext> localTrackingCtx = new ThreadLocal<>();

    /**
     * Gets the current tracking context for the current thread.
     *
     * @return the TimerNinjaThreadContext for the current thread, or null if not initialized
     */
    public static ThreadLocal<TimerNinjaThreadContext> getLocalTrackingCtx() {
        return localTrackingCtx;
    }

    /**
     * Checks if the current tracking context is null.
     *
     * @return true if tracking context is null, false otherwise
     */
    public static boolean isTrackingContextNull() {
        return localTrackingCtx.get() == null;
    }

    /**
     * Initializes a new tracking context for the current thread.
     * Instead of creating a new ThreadLocal object, this sets a new value
     * in the existing ThreadLocal for the current thread.
     */
    public static void initTrackingContext() {
        TimerNinjaThreadContext timerNinjaThreadContext = new TimerNinjaThreadContext();
        localTrackingCtx.set(timerNinjaThreadContext);
        Thread currentThread = Thread.currentThread();
        TimerNinjaThreadContext.LOGGER.debug("{} ({})| TimerNinjaTracking context {} initiated",
            currentThread.getName(),
            currentThread.threadId(),
            timerNinjaThreadContext.getTraceContextId()
        );
    }
}