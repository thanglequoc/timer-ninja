package io.github.thanglequoc.timerninja;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.temporal.ChronoUnit;

/**
 * Annotate any Java method with this annotation to track the total execution
 * time of the method. <br>
 * This annotation also supports nested tracking. If the annotated method is
 * invoked from a parent method that is also annotated with
 * {@code @TimerNinjaTracker}, then the execution time of the method will be
 * added to the existing tracking context.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.CONSTRUCTOR, ElementType.METHOD })
public @interface TimerNinjaTracker {

    /**
     * The time unit to use for the tracker.
     * Supported time units: second, millisecond (default), microsecond
     * 
     * @return The time unit of the tracker
     */
    ChronoUnit timeUnit() default ChronoUnit.MILLIS;

    /**
     * Determine if this tracker should be active
     * Set to false will disable this tracker from the overall tracking trace result
     * 
     * @return boolean flag indicates if this tracker is active
     */
    boolean enabled() default true;

    /**
     * Determine if this tracker should also include the argument information passed
     * to the method being tracked
     * Default is false
     *
     * @return true if the tracker should include argument information, false
     *         otherwise
     */
    boolean includeArgs() default false;

    /**
     * Set the threshold of the tracker, if the execution time of the method is less
     * than the threshold,
     * the tracker will not be included in the tracking result.
     * Default is -1, which means no threshold is set.
     *
     * @return the threshold definition for the current tracker
     */
    int threshold() default -1;

    /**
     * Unique identifier for this tracker in statistics.
     * If empty, defaults to "ClassName.methodName".
     * Useful when same method signature exists in different classes.
     *
     * @return the custom tracker ID for statistics
     */
    String trackerId() default "";
}
