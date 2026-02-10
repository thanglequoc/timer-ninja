package io.github.thanglequoc.timerninja;

import java.time.temporal.ChronoUnit;
import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.JoinPoint.StaticPart;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.reflect.CodeSignature;

import static io.github.thanglequoc.timerninja.TimerNinjaThreadContext.LOGGER;

/**
 * AspectJ aspect that provides automatic time tracking for methods and constructors annotated with {@link TimerNinjaTracker}.
 * <p>
 * This aspect intercepts method and constructor executions at runtime, measuring their execution time
 * and integrating the results into a shared tracking context managed by {@link TimerNinjaContextManager}.
 * The tracking supports nested calls, where methods tracked with {@code @TimerNinjaTracker} that call
 * other tracked methods will have their execution times aggregated in a hierarchical trace.
 * </p>
 * <p>
 * <b>Key Features:</b>
 * </p>
 * <ul>
 *   <li>Automatic interception via AspectJ pointcuts</li>
 *   <li>Support for both methods and constructors</li>
 *   <li>Nested tracking with hierarchical trace output</li>
 *   <li>Thread-local context management for multi-threaded applications</li>
 *   <li>Configurable time units (milliseconds, seconds, microseconds)</li>
 *   <li>Threshold filtering to exclude fast operations from traces</li>
 *   <li>Optional argument logging for debugging</li>
 * </ul>
 * <p>
 * <b>How It Works:</b>
 * </p>
 * <ol>
 *   <li>The aspect defines pointcuts to match methods and constructors with {@code @TimerNinjaTracker}</li>
 *   <li>Around advice wraps the execution of matched methods/constructors</li>
 *   <li>A {@link TrackerItemContext} is created to store tracking information</li>
 *   <li>Execution time is measured by capturing timestamps before and after method invocation</li>
 *   <li>When the root method completes, the complete trace is logged via {@link TimerNinjaUtil#logTimerContextTrace}</li>
 * </ol>
 * <p>
 * <b>Pointcuts:</b>
 * </p>
 * <ul>
 *   <li>{@code methodAnnotatedWithTimerNinjaTracker()}: Matches any method execution with {@code @TimerNinjaTracker}</li>
 *   <li>{@code constructorAnnotatedWithTimerNinjaTracker()}: Matches any constructor execution with {@code @TimerNinjaTracker}</li>
 * </ul>
 * <p>
 * <b>Thread Safety:</b>
 * </p>
 * <p>
 * This aspect is thread-safe. Each thread maintains its own tracking context via ThreadLocal storage
 * managed by {@link TimerNinjaContextManager}, allowing concurrent tracking without interference.
 * </p>
 * <p>
 * <b>Example Output:</b>
 * </p>
 * <pre>
 * Timer Ninja trace context id: 123e4567-e89b-12d3-a456-426614174000 | Trace timestamp: 2023-03-27T11:24:46.948Z
 * {===== Start of trace context id: 123e4567-e89b-12d3-a456-426614174000 =====}
 * public void BankService.processPayment(BankCard card, long amount) - Args: [card={type='VISA', number='****1234'}, amount=500] - 150 ms
 *   |-- public boolean CardService.validateCard(BankCard card) - Args: [card={type='VISA', number='****1234'}] - 25 ms
 *   |-- public void NotificationService.sendPaymentConfirmation(User user) - 75 ms
 * {====== End of trace context id: 123e4567-e89b-12d3-a456-426614174000 ======}
 * </pre>
 */
public aspect TimeTrackingAspect {

    /**
     * Point cut is any method, or constructor annotated with @TimerNinjaTracker
     * */
    pointcut methodAnnotatedWithTimerNinjaTracker(): execution(@io.github.thanglequoc.timerninja.TimerNinjaTracker * * (..));
    pointcut constructorAnnotatedWithTimerNinjaTracker(): execution(@io.github.thanglequoc.timerninja.TimerNinjaTracker *.new(..));

    /**
     * Around advice for method that is annotated with {@code @TimerNinjaTracker} annotation
     * */
    Object around(): methodAnnotatedWithTimerNinjaTracker() {
        StaticPart staticPart = thisJoinPointStaticPart;
        Signature signature = staticPart.getSignature();
        MethodSignature methodSignature = (MethodSignature) signature;

        if (TimerNinjaContextManager.isTrackingContextNull()) {
            TimerNinjaContextManager.setLocalTrackingCtx(TimerNinjaContextManager.initTrackingContext());
        }

        ThreadLocal<TimerNinjaThreadContext> localTrackingCtx = TimerNinjaContextManager.getLocalTrackingCtx();
        TimerNinjaThreadContext trackingCtx = localTrackingCtx.get();
        String traceContextId = trackingCtx.getTraceContextId();
        boolean isTrackerEnabled = TimerNinjaUtil.isTimerNinjaTrackerEnabled(methodSignature);

        String methodSignatureString = TimerNinjaUtil.prettyGetMethodSignature(methodSignature);
        String methodArgumentString = TimerNinjaUtil.prettyGetArguments(thisJoinPoint);
        boolean isIncludeArgsInLog = TimerNinjaUtil.isArgsIncluded(methodSignature);
        int threshold = TimerNinjaUtil.getThreshold(methodSignature);

        TrackerItemContext trackerItemContext = new TrackerItemContext(trackingCtx.getPointerDepth(), methodSignatureString, methodArgumentString, isIncludeArgsInLog);
        trackerItemContext.setThreshold(threshold);
        String uuid = UUID.randomUUID().toString();

        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        long threadId = currentThread.getId();

        if (isTrackerEnabled) {
            LOGGER.debug("{} ({})|{}| TrackerItemContext {} initiated, start tracking on: {} - {}",
                threadName, threadId, traceContextId, uuid, methodSignatureString, methodArgumentString);
            trackingCtx.addItemContext(uuid, trackerItemContext);
            trackingCtx.increasePointerDepth();
        }

        // Method invocation
        long startTime = System.currentTimeMillis();
        Object object = proceed();
        long endTime = System.currentTimeMillis();

        if (isTrackerEnabled) {
            LOGGER.debug("{} ({})|{}| TrackerItemContext {} finished tracking on: {} - {}. Evaluating execution time...",
                threadName, threadId, traceContextId, uuid, methodSignatureString, methodArgumentString);
            ChronoUnit trackingTimeUnit = TimerNinjaUtil.getTrackingTimeUnit(methodSignature);
            long executionTime = TimerNinjaUtil.convertFromMillis(endTime - startTime, trackingTimeUnit);
            trackerItemContext.setExecutionTime(executionTime);
            trackerItemContext.setTimeUnit(trackingTimeUnit);
            LOGGER.debug("{} ({})|{}| TrackerItemContext: {}", threadName, threadId, traceContextId, trackerItemContext);
            trackingCtx.decreasePointerDepth();
        }

        if (trackingCtx.getPointerDepth() == 0) {
            TimerNinjaUtil.logTimerContextTrace(trackingCtx);
            localTrackingCtx.remove();
            LOGGER.debug("{} ({})| TimerNinjaTracking context {} is completed and has been removed",
                threadName, threadId, traceContextId);
        }

        return object;
    }

    /**
     * Around advice for constructor that is annotated with {@code @TimerNinjaTracker} annotation
     * */
    Object around(): constructorAnnotatedWithTimerNinjaTracker() {
        StaticPart staticPart = thisJoinPointStaticPart;
        Signature signature = staticPart.getSignature();
        ConstructorSignature constructorSignature = (ConstructorSignature) signature;

        if (TimerNinjaContextManager.isTrackingContextNull()) {
            TimerNinjaContextManager.setLocalTrackingCtx(TimerNinjaContextManager.initTrackingContext());
        }
        ThreadLocal<TimerNinjaThreadContext> localTrackingCtx = TimerNinjaContextManager.getLocalTrackingCtx();
        TimerNinjaThreadContext trackingCtx = localTrackingCtx.get();
        String traceContextId = trackingCtx.getTraceContextId();
        boolean isTrackerEnabled = TimerNinjaUtil.isTimerNinjaTrackerEnabled(constructorSignature);

        String constructorSignatureString = TimerNinjaUtil.prettyGetConstructorSignature(constructorSignature);
        String constructorArgumentString = TimerNinjaUtil.prettyGetArguments(thisJoinPoint);
        boolean isIncludeArgsInLog = TimerNinjaUtil.isArgsIncluded(constructorSignature);
        int threshold = TimerNinjaUtil.getThreshold(constructorSignature);

        TrackerItemContext trackerItemContext = new TrackerItemContext(trackingCtx.getPointerDepth(), constructorSignatureString, constructorArgumentString, isIncludeArgsInLog);
        trackerItemContext.setThreshold(threshold);
        String uuid = UUID.randomUUID().toString();

        Thread currentThread = Thread.currentThread();
        String threadName = currentThread.getName();
        long threadId = currentThread.getId();

        if (isTrackerEnabled) {
            LOGGER.debug("{} ({})|{}| TrackerItemContext {} initiated, start tracking on constructor: {} - {}",
                threadName, threadId, traceContextId, uuid, constructorSignatureString, constructorArgumentString);
            trackingCtx.addItemContext(uuid, trackerItemContext);
            trackingCtx.increasePointerDepth();
        }

        // Method invocation
        long startTime = System.currentTimeMillis();
        Object object = proceed();
        long endTime = System.currentTimeMillis();

        if (isTrackerEnabled) {
            LOGGER.debug("{} ({})|{}| TrackerItemContext {} finished tracking on constructor: {} - {}. Evaluating execution time...",
                threadName, threadId, traceContextId, uuid, constructorSignatureString, constructorArgumentString);
            ChronoUnit trackingTimeUnit = TimerNinjaUtil.getTrackingTimeUnit(constructorSignature);
            trackerItemContext.setExecutionTime(TimerNinjaUtil.convertFromMillis(endTime - startTime, trackingTimeUnit));
            trackerItemContext.setTimeUnit(trackingTimeUnit);
            LOGGER.debug("{} ({})|{}| TrackerItemContext: {}", threadName, threadId, traceContextId, trackerItemContext);
            trackingCtx.decreasePointerDepth();
        }

        if (trackingCtx.getPointerDepth() == 0) {
            TimerNinjaUtil.logTimerContextTrace(trackingCtx);
            localTrackingCtx.remove();
            LOGGER.debug("{} ({})| TimerNinjaTracking context {} is completed and has been removed",
                threadName, threadId, traceContextId);
        }

        return object;
    }

}
