# User Guide 📘

This guide provides detailed documentation on how to use Timer Ninja's features effectively.

## Table of Contents

1. [Annotation-based Tracking](#annotation-based-tracking)
2. [Block Tracking](#block-tracking)
3. [Configuration Options](#configuration-options)
4. [Understanding Trace Output](#understanding-trace-output)
5. [Best Practices](#best-practices)

---

## Annotation-based Tracking

The `@TimerNinjaTracker` annotation is the primary way to track method execution time.

### Basic Usage

Annotate any method or constructor to start tracking:

```java
@TimerNinjaTracker
public void performTask() {
    // Your business logic
}
```

### Tracking Constructors

You can also track constructor execution:

```java
@TimerNinjaTracker
public class NotificationService {
    public NotificationService() {
        // Constructor logic
    }
}
```

**Output:**
```
{===== Start of trace context id: abc123... =====}
public NotificationService() - 80 ms
{====== End of trace context id: abc123... ======}
```

### Annotation Attributes

The `@TimerNinjaTracker` annotation supports several configuration options:

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `enabled` | `boolean` | `true` | Enable or disable tracking for this method |
| `timeUnit` | `ChronoUnit` | `MILLIS` | Time unit for measurement (SECONDS, MILLIS, MICROS) |
| `includeArgs` | `boolean` | `false` | Include method arguments in the log trace |
| `threshold` | `int` | `-1` | Minimum execution time required to log (in specified timeUnit) |

---

## Configuration Options

### 1. Enable/Disable Tracking

Control whether a specific method is tracked:

```java
@TimerNinjaTracker(enabled = true)
public void trackThis() {
    // This will be tracked
}

@TimerNinjaTracker(enabled = false)
public void dontTrackThis() {
    // This will NOT be tracked
}
```

**Use Case:** Temporarily disable tracking for a method without removing the annotation.

### 2. Time Unit Selection

Choose the appropriate time unit for your measurement needs:

```java
import java.time.temporal.ChronoUnit;

@TimerNinjaTracker(timeUnit = ChronoUnit.SECONDS)
public void longRunningOperation() {
    // For operations taking seconds
}

@TimerNinjaTracker(timeUnit = ChronoUnit.MILLIS)
public void standardOperation() {
    // For operations taking milliseconds (default)
}

@TimerNinjaTracker(timeUnit = ChronoUnit.MICROS)
public void preciseOperation() {
    // For operations requiring microsecond precision
}
```

**Supported Units:**
- `ChronoUnit.SECONDS` - Seconds
- `ChronoUnit.MILLIS` - Milliseconds (default)
- `ChronoUnit.MICROS` - Microseconds

### 3. Include Method Arguments

Log method arguments for better debugging context:

```java
@TimerNinjaTracker(includeArgs = true)
public void processUser(int userId, String name, String email) {
    // Method logic
}
```

**Output:**
```
public void processUser(int userId, String name, String email) - Args: [userId={123}, name={John Doe}, email={john@example.com}] - 42 ms
```

**Important:** Ensure your objects have proper `toString()` implementations for meaningful output:

```java
public class User {
    private int id;
    private String name;
    private String email;
    
    @Override
    public String toString() {
        return String.format("User{id=%d, name='%s', email='%s'}", id, name, email);
    }
}
```

### 4. Threshold Filtering

Filter out fast methods to focus on performance issues:

```java
@TimerNinjaTracker(threshold = 500)  // Only log if execution > 500ms
public void potentiallySlowMethod() {
    // Method logic
}
```

**When Threshold is Exceeded:**
```
public void potentiallySlowMethod() - 723 ms ¤ [Threshold Exceed !!: 500 ms]
```

**When Below Threshold:**
- The method is suppressed from the trace output
- If all methods in a trace are below threshold, a summary is shown

**Combining with Arguments:**
```java
@TimerNinjaTracker(includeArgs = true, threshold = 200)
public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) {
    // Only logs slow transfers with full argument details
}
```

**Use Case:** Focus on slow operations while seeing the exact arguments that caused the delay.

---

## Block Tracking

For granular tracking within a method without extracting separate methods, use `TimerNinjaBlock`.

### Basic Block Tracking

```java
public void processData() {
    // Regular code not tracked
    
    TimerNinjaBlock.measure("database query", () -> {
        database.query("SELECT * FROM users");
    });
    
    // More code not tracked
}
```

### Block with Return Value

```java
public void processData() {
    String result = TimerNinjaBlock.measure("fetch data", () -> {
        return api.fetchUserData();
    });
    
    System.out.println(result);
}
```

### Block with Custom Configuration

```java
import java.time.temporal.ChronoUnit;

public void processData() {
    BlockTrackerConfig config = new BlockTrackerConfig()
        .setTimeUnit(ChronoUnit.SECONDS)
        .setThreshold(2);
    
    TimerNinjaBlock.measure("long operation", config, () -> {
        performLongRunningTask();
    });
}
```

### Nested Block Tracking

```java
public void complexProcess() {
    TimerNinjaBlock.measure("overall process", () -> {
        loadData();
        
        TimerNinjaBlock.measure("data transformation", () -> {
            transformData();
        });
        
        saveData();
    });
}
```

**Output:**
```
{===== Start of trace context id: ... =====}
[Block] overall process - 1500 ms
   |-- [Block] data transformation - 500 ms
{====== End of trace context id: ... ======}
```

---

## Understanding Trace Output

### Trace Structure

```
Timer Ninja trace context id: a1b2c3d4-e5f6-7890-abcd-ef1234567890
Trace timestamp: 2023-04-03T14:27:50.322Z
{===== Start of trace context id: a1b2c3d4-e5f6-7890-abcd-ef1234567890 =====}
public void parentMethod() - 100 ms
   |-- public void childMethod() - 50 ms
   |-- public void anotherChildMethod() - 30 ms
{====== End of trace context id: a1b2c3d4-e5f6-7890-abcd-ef1234567890 ======}
```

### Elements Explained

1. **Trace Context ID**: Auto-generated UUID for a trace
   - Initiated by the first `@TimerNinjaTracker` method encountered
   - All subsequent tracked methods in the same call stack share this ID

2. **Trace Timestamp**: When the trace context was initiated (UTC timezone)

3. **Start/End Markers**: Delimit the trace boundaries

4. **Method Lines**: Each tracked method shows:
   - Method signature
   - Arguments (if `includeArgs = true`)
   - Execution time with time unit
   - Threshold indicator (if exceeded)

5. **Indentation (`|--`)**: Shows call hierarchy
   - Indented methods are called by the method above
   - Helps visualize the execution stacktrace

### Summary Output

When all methods in a trace are below their thresholds:

```
Timer Ninja trace context id: abc123...
Trace timestamp: 2023-04-03T14:27:50.322Z
All 3 tracked items within threshold. min: 5 ms, max: 45 ms, total: 50 ms
```

This summary shows the range and total execution time without detailed traces.

---

## Global Configuration

Timer Ninja uses a singleton configuration class for global settings.

### Enable System.out Logging

For simple console applications or quick testing:

```java
TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
```

**Note:** Call this once at application startup. By default, Timer Ninja uses SLF4J logging.

### Log Level

The logger class is `io.github.thanglequoc.timerninja.TimerNinjaUtil` with default level `INFO`.

To see debug information:

```
<logger name="io.github.thanglequoc.timerninja.TimerNinjaThreadContext" level="DEBUG"/>
```

**Use Case:** Troubleshooting tracking issues or understanding internal behavior.

---

## Best Practices

### 1. Choose Appropriate Time Units

- **Seconds**: For long-running operations (API calls, file I/O, batch processing)
- **Milliseconds**: For general application logic (default)
- **Microseconds**: For performance-critical code (algorithms, calculations)

### 2. Use Thresholds Strategically

```java
// Too low - noise
@TimerNinjaTracker(threshold = 10)

// Too high - miss issues
@TimerNinjaTracker(threshold = 5000)

// Balanced - catches real issues
@TimerNinjaTracker(threshold = 200)
```

### 3. Combine with Arguments for Debugging

```java
@TimerNinjaTracker(includeArgs = true, threshold = 200)
public void searchUsers(String query, int limit) {
    // See which queries are slow
}
```

### 4. Track Entry Points

Add tracking to high-level entry points (REST controllers, main methods) to capture full execution traces:

```java
@RestController
public class UserController {
    
    @TimerNinjaTracker
    @GetMapping("/users/{id}")
    public User getUser(@PathVariable Long id) {
        // All nested tracked methods will appear in the trace
        return userService.findById(id);
    }
}
```

### 5. Use Block Tracking for Phases

For methods with distinct phases, use block tracking instead of breaking into small methods:

```java
public void processOrder(Order order) {
    // Validation phase
    TimerNinjaBlock.measure("validation", () -> {
        validateOrder(order);
    });
    
    // Processing phase
    TimerNinjaBlock.measure("processing", () -> {
        processPayment(order);
        updateInventory(order);
    });
    
    // Notification phase
    TimerNinjaBlock.measure("notification", () -> {
        sendConfirmation(order);
    });
}
```

### 6. Disable in Production When Needed

```java
@TimerNinjaTracker(enabled = isDevelopment())
public void debugMethod() {
    // Only tracked in development environment
}

private boolean isDevelopment() {
    return "dev".equals(System.getProperty("environment"));
}
```

### 7. Don't Track Everything

Focus on:
- Critical business logic
- External API calls
- Database operations
- File I/O operations
- Complex algorithms

Avoid tracking:
- Simple getters/setters
- Very fast operations (< 1ms)
- Trivial utility methods

---

## Troubleshooting

### No Output in Logs

1. Check if SLF4J provider is configured
2. Verify log level is at least INFO
3. Enable System.out for testing:
   ```java
   TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
   ```

### Methods Not Being Tracked

1. Verify AspectJ plugin is configured correctly
2. Check that the dependency includes the aspect:
   ```groovy
   aspect 'io.github.thanglequoc:timer-ninja:1.2.0'
   ```
3. Ensure `enabled = true` (or not set) on the annotation

### Missing Method Arguments

1. Set `includeArgs = true` on the annotation
2. Verify `toString()` is implemented for argument objects
3. Check if objects are null or contain sensitive data

### Inconsistent Tracking

1. Ensure you're using the correct version (1.2.0 or later)
2. Check for conflicting AOP configurations
3. Review logs for DEBUG information
   ```xml
   <logger name="io.github.thanglequoc.timerninja.TimerNinjaThreadContext" level="DEBUG"/>
   ```

---

## Further Reading

- **[Examples](Examples)** - Real-world usage patterns
- **[Advanced Usage](Advanced-Usage)** - Advanced features and optimization
- **[Home](Home)** - Quick start and installation guide