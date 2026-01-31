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
 * TimeTracking aspect definition
 * */
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
