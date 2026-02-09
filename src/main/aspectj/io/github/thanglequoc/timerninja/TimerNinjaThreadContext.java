package io.github.thanglequoc.timerninja;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Timer Ninja Thread context, which store the current timer tracking state and
 * tracker execution trace
 */
public class TimerNinjaThreadContext {

    /**
     * The logger class, to be used as logger in the TimeTrackingAspect code
     */
    public static Logger LOGGER = LoggerFactory.getLogger(TimerNinjaThreadContext.class);

    private final String traceContextId;

    /**
     * The creation time since this thread context starts
     */
    private final Instant creationTime;

    /**
     * The current pointer depth of the timer tracking context.<br>
     * This depth increases for each nested annotated tracker method that is called
     * within the current
     * method being evaluated.
     */
    private int pointerDepth;

    /**
     * Item context map to store the executing method being tracked. This contextMap
     * will act as a stacktrace to be printed out later
     * Key: The uuid generated for each tracker item context
     * Value: the tracker item context of the method being tracked
     */
    private Map<String, TrackerItemContext> itemContextMap;

    /**
     * The current parent tracker ID for statistics hierarchy tracking.
     * Used to establish parent-child relationships between tracked methods.
     */
    private String currentParentTrackerId;

    /**
     * Basic constructor of a TimerNinjaThreadContext, with the following default
     * values scheme: <br>
     * - traceContextId: a random uuid
     * - creationTime: current instant.now()
     * - pointerDepth: root pointer, starts at 0
     */
    public TimerNinjaThreadContext() {
        traceContextId = UUID.randomUUID().toString();
        creationTime = Instant.now();
        pointerDepth = 0;
        itemContextMap = new LinkedHashMap<>();
    }

    /**
     * Get the current method pointer depth
     * 
     * @return the current pointer depth. Root is 0, one nested level is +1
     */
    public int getPointerDepth() {
        return pointerDepth;
    }

    /**
     * Get the trace context id
     * 
     * @return The random generated trace context id in UUID format
     */
    public String getTraceContextId() {
        return traceContextId;
    }

    /**
     * Get the tracking context creation time
     * 
     * @return The creation time of the Timer Ninja thread context
     */
    public Instant getCreationTime() {
        return creationTime;
    }

    /**
     * Increase the pointer depth by one level
     * 
     * @return The increased pointer depth
     */
    public int increasePointerDepth() {
        return ++pointerDepth;
    }

    /**
     * Decrease the pointer depth by one level
     * 
     * @return The decreased pointer depth
     */
    public int decreasePointerDepth() {
        return --pointerDepth;
    }

    /**
     * Get the current item context map. With the key is the tracker item context
     * uuid, and value is the item itself
     * 
     * @return The item context map
     */
    public Map<String, TrackerItemContext> getItemContextMap() {
        return itemContextMap;
    }

    /**
     * Add the tracking method to the item context map
     * 
     * @param uuid               The uuid of an item context
     * @param trackerItemContext The tracker item context, represents a method
     *                           invocation
     */
    public void addItemContext(String uuid, TrackerItemContext trackerItemContext) {
        this.itemContextMap.put(uuid, trackerItemContext);
    }

    /**
     * Get the current parent tracker ID for statistics hierarchy.
     * 
     * @return the current parent tracker ID
     */
    public String getCurrentParentTrackerId() {
        return currentParentTrackerId;
    }

    /**
     * Set the current parent tracker ID for statistics hierarchy.
     * 
     * @param currentParentTrackerId the tracker ID of the current parent
     */
    public void setCurrentParentTrackerId(String currentParentTrackerId) {
        this.currentParentTrackerId = currentParentTrackerId;
    }
}
