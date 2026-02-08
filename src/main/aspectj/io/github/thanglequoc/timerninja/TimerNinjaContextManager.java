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
     */
    private static ThreadLocal<TimerNinjaThreadContext> localTrackingCtx;

    /**
     * Gets the current tracking context for the current thread.
     *
     * @return the TimerNinjaThreadContext for the current thread, or null if not initialized
     */
    public static ThreadLocal<TimerNinjaThreadContext> getLocalTrackingCtx() {
        return localTrackingCtx;
    }

    /**
     * Sets the tracking context for the current thread.
     * This is typically called by the aspect or block tracking when initializing a new context.
     *
     * @param context the ThreadLocal tracking context to set
     */
    public static void setLocalTrackingCtx(ThreadLocal<TimerNinjaThreadContext> context) {
        localTrackingCtx = context;
    }

    /**
     * Checks if the current tracking context is null.
     *
     * @return true if tracking context is null, false otherwise
     */
    public static boolean isTrackingContextNull() {
        return localTrackingCtx == null || localTrackingCtx.get() == null;
    }

    /**
     * Initializes a new tracking context for the current thread.
     *
     * @return a new ThreadLocal containing a new TimerNinjaThreadContext
     */
    public static ThreadLocal<TimerNinjaThreadContext> initTrackingContext() {
        Thread currentThread = Thread.currentThread();
        ThreadLocal<TimerNinjaThreadContext> timerNinjaLocalThreadContext = new ThreadLocal<>();
        TimerNinjaThreadContext timerNinjaThreadContext = new TimerNinjaThreadContext();
        timerNinjaLocalThreadContext.set(timerNinjaThreadContext);
        TimerNinjaThreadContext.LOGGER.debug("{} ({})| TimerNinjaTracking context {} initiated",
            currentThread.getName(),
            currentThread.getId(),
            timerNinjaThreadContext.getTraceContextId()
        );
        return timerNinjaLocalThreadContext;
    }
}