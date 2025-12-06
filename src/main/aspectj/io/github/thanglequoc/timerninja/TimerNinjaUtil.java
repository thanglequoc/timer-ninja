package io.github.thanglequoc.timerninja;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.CodeSignature;
import org.aspectj.lang.reflect.ConstructorSignature;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

/**
 * General utility class of TimerNinja library
 * */
public class TimerNinjaUtil {

    private static Logger LOGGER = LoggerFactory.getLogger(TimerNinjaUtil.class);

    /**
     * The timer ninja util is a util class with static method, so instance creation is not allowed on this util class
     * */
    private TimerNinjaUtil() {
    }

    /**
     * Determines whether the {@link TimerNinjaTracker} annotation is enabled on the given method.
     *
     * @param methodSignature the AspectJ {@code MethodSignature} representing
     *                        the annotated method; must not be {@code null}
     * @return {@code true} if the {@link TimerNinjaTracker#enabled()} flag is set to {@code true}
     *         on the method, {@code false} otherwise
     * @throws IllegalArgumentException if {@code methodSignature} is {@code null}
     */
    public static boolean isTimerNinjaTrackerEnabled(MethodSignature methodSignature) {
        if (methodSignature == null) {
            throw new IllegalArgumentException("MethodSignature must be present");
        }

        TimerNinjaTracker annotation = methodSignature.getMethod().getAnnotation(TimerNinjaTracker.class);
        return annotation.enabled();
    }

    /**
     * Determines whether the {@link TimerNinjaTracker} annotation is enabled on the given constructor.
     *
     * @param constructorSignature the AspectJ {@code ConstructorSignature}
     *                             representing the annotated constructor; must not be {@code null}
     * @return {@code true} if the {@link TimerNinjaTracker#enabled()} flag is set to {@code true}
     *         on the constructor, {@code false} otherwise
     * @throws IllegalArgumentException if {@code constructorSignature} is {@code null}
     */
    public static boolean isTimerNinjaTrackerEnabled(ConstructorSignature constructorSignature) {
        if (constructorSignature == null) {
            throw new IllegalArgumentException("ConstructorSignature must be present");
        }
        TimerNinjaTracker annotation = (TimerNinjaTracker) constructorSignature.getConstructor().getAnnotation(TimerNinjaTracker.class);
        return annotation.enabled();
    }

    /**
     * Retrieves the threshold value defined in the {@link TimerNinjaTracker} annotation on the given method.
     *
     * @param methodSignature the AspectJ {@code MethodSignature} representing
     *                        the annotated method; must not be {@code null}
     * @return the threshold value configured via {@link TimerNinjaTracker#threshold()}
     * @throws IllegalArgumentException if {@code methodSignature} is {@code null}
     */
    public static int getThreshold(MethodSignature methodSignature) {
        if (methodSignature == null) {
            throw new IllegalArgumentException("MethodSignature must be present");
        }
        TimerNinjaTracker annotation = methodSignature.getMethod().getAnnotation(TimerNinjaTracker.class);
        return annotation.threshold();
    }

    /**
     * Retrieves the threshold value defined in the {@link TimerNinjaTracker} annotation on the given constructor.
     *
     * @param constructorSignature the AspectJ {@code ConstructorSignature}
     *                             representing the annotated constructor;
     *                             must not be {@code null}
     * @return the threshold value configured via {@link TimerNinjaTracker#threshold()}
     * @throws IllegalArgumentException if {@code constructorSignature} is {@code null}
     */
    public static int getThreshold(ConstructorSignature constructorSignature) {
        if (constructorSignature == null) {
            throw new IllegalArgumentException("ConstructorSignature must be present");
        }
        TimerNinjaTracker annotation = (TimerNinjaTracker) constructorSignature.getConstructor().getAnnotation(TimerNinjaTracker.class);
        return annotation.threshold();
    }

    /**
     * Determines whether argument logging is enabled for the given constructor
     * annotated with {@link TimerNinjaTracker}.
     *
     * @param constructorSignature the AspectJ {@code ConstructorSignature} representing
     *                             the annotated constructor; must not be {@code null}
     * @return {@code true} if the {@link TimerNinjaTracker#includeArgs()} flag is enabled
     *         on the constructor, {@code false} otherwise
     * @throws IllegalArgumentException if {@code constructorSignature} is {@code null}
     */
    public static boolean isArgsIncluded(ConstructorSignature constructorSignature) {
        if (constructorSignature == null) {
            throw new IllegalArgumentException("ConstructorSignature must be present");
        }
        TimerNinjaTracker annotation = (TimerNinjaTracker) constructorSignature.getConstructor().getAnnotation(TimerNinjaTracker.class);
        return annotation.includeArgs();
    }

    /**
     * Determines whether argument logging is enabled for the given method
     * annotated with {@link TimerNinjaTracker}.
     *
     * @param methodSignature the AspectJ {@code MethodSignature} representing
     *                        the annotated method; must not be {@code null}
     * @return {@code true} if the {@link TimerNinjaTracker#includeArgs()} flag is enabled
     *         on the method, {@code false} otherwise
     * @throws IllegalArgumentException if {@code methodSignature} is {@code null}
     */
    public static boolean isArgsIncluded(MethodSignature methodSignature) {
        if (methodSignature == null) {
            throw new IllegalArgumentException("MethodSignature must be present");
        }
        TimerNinjaTracker annotation = methodSignature.getMethod().getAnnotation(TimerNinjaTracker.class);
        return annotation.includeArgs();
    }

    /**
     * Retrieves the {@link ChronoUnit} time unit defined in the {@link TimerNinjaTracker} annotation on the given method.
     *
     * @param methodSignature the AspectJ {@code MethodSignature} representing
     *                        the annotated method; must not be {@code null}
     * @return the time unit configured via {@link TimerNinjaTracker#timeUnit()}
     * @throws IllegalArgumentException if {@code methodSignature} is {@code null}
     */
    public static ChronoUnit getTrackingTimeUnit(MethodSignature methodSignature) {
        if (methodSignature == null) {
            throw new IllegalArgumentException("MethodSignature must be present");
        }

        TimerNinjaTracker annotation = methodSignature.getMethod().getAnnotation(TimerNinjaTracker.class);
        return annotation.timeUnit();
    }

    /**
     * Retrieves the {@link ChronoUnit} time unit defined in the {@link TimerNinjaTracker} annotation on the given constructor.
     *
     * @param constructorSignature the AspectJ {@code ConstructorSignature}
     *                             representing the annotated constructor;
     *                             must not be {@code null}
     * @return the time unit configured via {@link TimerNinjaTracker#timeUnit()}
     * @throws IllegalArgumentException if {@code constructorSignature} is {@code null}
     */
    public static ChronoUnit getTrackingTimeUnit(ConstructorSignature constructorSignature) {
        if (constructorSignature == null) {
            throw new IllegalArgumentException("ConstructorSignature must be present");
        }

        TimerNinjaTracker annotation = (TimerNinjaTracker) constructorSignature.getConstructor().getAnnotation(TimerNinjaTracker.class);
        return annotation.timeUnit();
    }

    /**
     * Builds a human-readable representation of a method signature, including its
     * modifiers, return type, method name, and parameter list.
     * <p>
     * The output format resembles a simplified Java method declaration.
     * Example:
     * <pre>
     * public static String prettyGetMethodSignature(MethodSignature methodSignature)
     * </pre>
     *
     * @param methodSignature the AspectJ {@code MethodSignature} to render; must not be {@code null}
     * @return a formatted string containing the method modifiers, return type, name,
     *         and parameters
     * @throws IllegalArgumentException if {@code methodSignature} is {@code null}
     */
    public static String prettyGetMethodSignature(MethodSignature methodSignature) {
        if (methodSignature == null) {
            throw new IllegalArgumentException("MethodSignature must be present");
        }

        StringBuilder sb = new StringBuilder();

        String methodModifier = Modifier.toString(methodSignature.getModifiers());
        sb.append(methodModifier);
        if (!methodModifier.isEmpty()) {
            sb.append(" ");
        }

        String returnType = methodSignature.getReturnType().getSimpleName();
        sb.append(returnType).append(" ");

        String methodName = methodSignature.getName();
        sb.append(methodName).append("(");

        // pretty print the parameter names
        String[] parameterNames = methodSignature.getParameterNames();
        Class<?>[] parameterClasses = methodSignature.getParameterTypes();
        for (int i = 0; i < parameterNames.length; i++) {
            sb.append(parameterClasses[i].getSimpleName()).append(" ").append(parameterNames[i]);
            if (i != parameterNames.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Builds a human-readable representation of a constructor signature, including its
     * modifiers, class name, and parameter list.
     * <p>
     * The output format resembles a simplified Java constructor declaration.
     * Example:
     * <pre>
     * public TrackerItemContext(String abc)
     * </pre>
     *
     * @param constructorSignature the AspectJ {@code ConstructorSignature} to render;
     *                             must not be {@code null}
     * @return a formatted string containing the constructor modifiers, class name,
     *         and parameters
     * @throws IllegalArgumentException if {@code constructorSignature} is {@code null}
     */
    public static String prettyGetConstructorSignature(ConstructorSignature constructorSignature) {
        if (constructorSignature == null) {
            throw new IllegalArgumentException("ConstructorSignature must be present");
        }

        StringBuilder sb = new StringBuilder();

        String modifier = Modifier.toString(constructorSignature.getModifiers());
        sb.append(modifier);
        if (!modifier.isEmpty()) {
            sb.append(" ");
        }

        Constructor<?> constructor = constructorSignature.getConstructor();
        String constructingClassType = constructor.getDeclaringClass().getSimpleName();
        sb.append(constructingClassType).append("(");

        // pretty print the parameter names
        String[] parameterNames = constructorSignature.getParameterNames();
        Class<?>[] parameterClasses = constructorSignature.getParameterTypes();
        for (int i = 0; i < parameterNames.length; i++) {
            sb.append(parameterClasses[i].getSimpleName()).append(" ").append(parameterNames[i]);
            if (i != parameterNames.length - 1) {
                sb.append(", ");
            }
        }
        sb.append(")");

        return sb.toString();
    }

    /**
     * Builds a human-readable representation of the arguments passed to a method or
     * constructor at a given join point. Each argument is rendered in the format:
     * <code>name={value}</code>.
     * <p>
     * Example output:
     * <pre>
     * user={name='John Doe', age=30}, amount={500}
     * </pre>
     *
     * @param joinPoint the AspectJ {@code JoinPoint} containing argument and parameter
     *                  name information; must not be {@code null}
     * @return a formatted string listing parameter names and their corresponding values
     * @throws IllegalArgumentException if {@code joinPoint} is {@code null}
     */
    public static String prettyGetArguments(JoinPoint joinPoint) {
        if (joinPoint == null) {
            throw new IllegalArgumentException("JoinPoint must be present");
        }

        StringBuilder sb = new StringBuilder();
        Object[] args = joinPoint.getArgs();
        String[] names = ((CodeSignature)joinPoint.getSignature()).getParameterNames();
        for (int i = 0; i < args.length; i++) {
            sb.append(names[i]);
            sb.append("={");
            sb.append(args[i]);
            sb.append("}");
            if (i != args.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }

    /**
     * Logs a detailed execution trace for the provided {@link TimerNinjaThreadContext}.
     * <p>
     * The output includes:
     * <ul>
     *     <li>the timer-ninja trace context ID</li>
     *     <li>the UTC creation timestamp of the tracking context</li>
     *     <li>a formatted breakdown of each tracked method or constructor,
     *         including indentation, arguments (if enabled), execution times,
     *         and threshold exceed indicators</li>
     * </ul>
     *
     * <p>
     * Trace output is sent to the SLF4J logging API.
     * Additionally, output may also be mirrored to {@code System.out} depending on
     * configuration defined in {@link TimerNinjaConfiguration}.
     *
     * @param timerNinjaThreadContext the thread-local tracking context containing
     *                                all recorded {@link TrackerItemContext} entries;
     *                                must not be {@code null}
     * @throws IllegalArgumentException if {@code timerNinjaThreadContext} is {@code null}
     */
    public static void logTimerContextTrace(TimerNinjaThreadContext timerNinjaThreadContext) {
        if (timerNinjaThreadContext == null) {
            throw new IllegalArgumentException("TimerNinjaThreadContext must be present");
        }

        String traceContextId = timerNinjaThreadContext.getTraceContextId();
        String utcTimeString = toUTCTimestampString(timerNinjaThreadContext.getCreationTime());

        logMessage("Timer Ninja trace context id: {} | Trace timestamp: {}", traceContextId, utcTimeString);
        if (timerNinjaThreadContext.getItemContextMap().isEmpty()) {
            logMessage("There isn't any tracker enabled in the tracking context");
            return;
        }
        logMessage("{===== Start of trace context id: {} =====}", traceContextId);
        int currentMethodPointerDepthWithThresholdMeet = -1; // unassigned
        boolean withinThresholdZone = false;

        for (TrackerItemContext item : timerNinjaThreadContext.getItemContextMap().values()) {
            if (withinThresholdZone && item.getPointerDepth() == currentMethodPointerDepthWithThresholdMeet) {
                withinThresholdZone = false;
            }

            // Item has threshold & still within limit
            if (!withinThresholdZone && (item.isEnableThreshold() && item.getExecutionTime() < item.getThreshold())) {
                currentMethodPointerDepthWithThresholdMeet = item.getPointerDepth();
                withinThresholdZone = true;
            }
            if (withinThresholdZone) {
                continue;
            }

            /*
            * Breakdown msg format
                {}{}: Indent + Method name
                - Args: [{}]: Args information (if included?)
                - {} {}: Execution time + unit
                ¤ [Threshold Exceed !!: {} ms]: If the threshold exceeded
            *  */
            List<Object> argList = new ArrayList<>();
            StringBuilder msgFormat = new StringBuilder();

            // Indent + Method name
            msgFormat.append("{}{}");
            String indent = generateIndent(item.getPointerDepth());
            String methodName = item.getMethodName();
            argList.add(indent);
            argList.add(methodName);

            // Argument information (if included?)
            if (item.isIncludeArgs()) {
                msgFormat.append(" - Args: [{}]");
                String args = item.getArguments();
                argList.add(args);
            }

            msgFormat.append(" - {} {}");
            long executionTime = item.getExecutionTime();
            String timeUnit = getPresentationUnit(item.getTimeUnit());
            argList.add(executionTime);
            argList.add(timeUnit);

            if (item.isEnableThreshold() && item.getExecutionTime() >= item.getThreshold()) {
                msgFormat.append(" ¤ [Threshold Exceed !!: {} ms]");
                argList.add(item.getThreshold());
            }
            logMessage(msgFormat.toString(), argList.toArray());
        }
        logMessage("{====== End of trace context id: {} ======}", traceContextId);
    }

    /**
     * Logs a formatted message to the SLF4J {@code LOGGER} and optionally to
     * {@code System.out} if enabled in {@link TimerNinjaConfiguration}.
     * <p>
     * This method is primarily used internally by Timer Ninja to output
     * execution traces and diagnostic messages.
     *
     * @param format the message format string, supporting '{}' placeholders
     *               for argument substitution
     * @param args the arguments to be substituted into the format string
     */
    private static void logMessage(String format, Object... args) {
        LOGGER.info(format, args);
        if (TimerNinjaConfiguration.getInstance().isSystemOutLogEnabled()) {
            System.out.printf(format.replace("{}", "%s") + "%n", args);
        }
    }

    /**
     * Generates a visual indentation prefix for a tracked method or constructor
     * based on its pointer depth. This is used to align nested tracker outputs
     * in the execution trace.
     * <p>
     * The prefix consists of spaces proportional to the pointer depth, followed
     * by the "|-- " marker.
     *
     * @param pointerDepth the depth of the method/constructor in the trace hierarchy;
     *                     zero indicates no indentation
     * @return a formatted string containing leading spaces and the "|-- " prefix,
     *         suitable for aligning nested method traces
     */
    private static String generateIndent(int pointerDepth) {
        if (pointerDepth == 0) {
            return "";
        }
        int spaceCount = pointerDepth * 2;
        StringBuilder sb = new StringBuilder();
        for (int i = 1; i<= spaceCount; i++) {
            sb.append(" ");
        }
        sb.append("|-- ");
        return sb.toString();
    }

    /**
     * Determines whether the execution time of a tracked method or constructor
     * has exceeded the threshold defined in its {@link TrackerItemContext}.
     *
     * @param item the {@link TrackerItemContext} representing the tracked method
     *             or constructor; must not be {@code null}
     * @return {@code true} if the threshold is greater than zero and the execution
     *         time exceeds the threshold, {@code false} otherwise
     * @throws IllegalArgumentException if {@code item} is {@code null}
     */
    public static boolean isThresholdExceeded(TrackerItemContext item) {
        return item.getThreshold() > 0 && item.getExecutionTime() > item.getThreshold();
    }

    /**
     * Converts a time value in milliseconds to the specified {@link ChronoUnit}.
     * <p>
     * Supported conversions:
     * <ul>
     *     <li>{@link ChronoUnit#MILLIS}: returns the same value</li>
     *     <li>{@link ChronoUnit#SECONDS}: converts milliseconds to seconds</li>
     *     <li>{@link ChronoUnit#MICROS}: converts milliseconds to microseconds</li>
     * </ul>
     *
     * @param timeInMillis the time value in milliseconds
     * @param unitToConvert the target {@link ChronoUnit} to convert the time into;
     *                      must be one of the supported units
     * @return the converted time in the specified {@code ChronoUnit}
     * @throws IllegalStateException if {@code unitToConvert} is not supported by this method
     */
    public static long convertFromMillis(long timeInMillis, ChronoUnit unitToConvert) {
       if (ChronoUnit.MILLIS.equals(unitToConvert)) {
           return timeInMillis;
       }
       else if (ChronoUnit.SECONDS.equals(unitToConvert)) {
           return timeInMillis / 1000;
       } else if (ChronoUnit.MICROS.equals(unitToConvert)) {
           return timeInMillis * 1000;
       }
       throw new IllegalStateException("Time unit not supported");
    }

    /**
     * Returns a short, human-readable string representing the given {@link ChronoUnit}.
     * <p>
     * Supported units:
     * <ul>
     *     <li>{@link ChronoUnit#MILLIS}: "ms"</li>
     *     <li>{@link ChronoUnit#SECONDS}: "s"</li>
     *     <li>{@link ChronoUnit#MICROS}: "µs"</li>
     * </ul>
     *
     * @param chronoUnit the {@link ChronoUnit} to get the short display unit for;
     *                   must be one of the supported units
     * @return the short string representation of the time unit
     * @throws IllegalStateException if {@code chronoUnit} is not supported by this method
     */
    private static String getPresentationUnit(ChronoUnit chronoUnit) {
        if (ChronoUnit.MILLIS.equals(chronoUnit)) {
            return "ms";
        } else if (ChronoUnit.SECONDS.equals(chronoUnit)) {
            return "s";
        } else if (ChronoUnit.MICROS.equals(chronoUnit)) {
            return "µs";
        }
        throw new IllegalStateException("Time unit not supported");
    }

    /**
     * Converts a {@link Instant} to a UTC timestamp string in ISO 8601 format
     * with millisecond precision.
     * <p>
     * The output pattern is {@code yyyy-MM-dd'T'HH:mm:ss.SSS'Z'}.
     * Example output:
     * <pre>
     * 2023-03-27T11:24:46.948Z
     * </pre>
     *
     * @param instant the {@link Instant} to convert; must not be {@code null}
     * @return the formatted UTC timestamp string
     * @throws NullPointerException if {@code instant} is {@code null}
     */
    private static String toUTCTimestampString(Instant instant) {
        return DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
                                .withZone(ZoneOffset.UTC)
                                .format(instant);
    }
}
