---
layout: docs
title: Advanced Usage
description: "Advanced features, optimization techniques, and best practices for Timer Ninja."
prev_page:
  title: Examples
  url: /examples/
---

# Advanced Usage

This guide covers advanced features and optimization techniques for Timer Ninja.

---

## Nested Tracking Deep Dive

Timer Ninja automatically detects and preserves nested method calls that are also annotated with `@TimerNinjaTracker`. This provides a complete view of the execution stack.

### Multi-Level Nesting Example

```java
@Service
public class OrderProcessingService {

    @TimerNinjaTracker
    public void processOrder(Order order) {
        validateOrder(order);
        processPayment(order);
        shipOrder(order);
    }

    @TimerNinjaTracker
    private void validateOrder(Order order) {
        validateCustomer(order.getCustomerId());
        validateItems(order.getItems());
    }

    @TimerNinjaTracker
    private void validateCustomer(Long customerId) {
        Customer customer = customerService.findById(customerId);
    }

    @TimerNinjaTracker(threshold = 100)
    private void validateItems(List<OrderItem> items) {
        items.forEach(this::validateItem);
    }

    @TimerNinjaTracker(threshold = 50)
    private void validateItem(OrderItem item) {
        // Item validation
    }
}
```

**Output:**
```
{===== Start of trace context id: abc123... =====}
public void processOrder(Order order) - 1850 ms
   |-- private void validateOrder(Order order) - 450 ms
   |   |-- private void validateCustomer(Long customerId) - 320 ms
   |   |-- private void validateItems(List<OrderItem> items) - 110 ms
   |       |-- private void validateItem(OrderItem item) - 52 ms ¤ [Threshold Exceed !!: 50 ms]
   |-- public void processPayment(Order order) - 1200 ms
   |-- public void shipOrder(Order order) - 200 ms
{====== End of trace context id: abc123... ======}
```

### Key Points

1. **Automatic Hierarchy** — Timer Ninja automatically builds the call tree
2. **Independent Thresholds** — Each method can have its own threshold
3. **Context Sharing** — All methods in a call chain share the same trace context ID

---

## Advanced Threshold Strategies

### Dynamic Thresholds Based on Input

```java
@Service
public class QueryService {

    @TimerNinjaTracker(includeArgs = true)
    public void executeQuery(String query, int expectedRows) {
        int threshold = calculateThreshold(expectedRows);

        BlockTrackerConfig config = new BlockTrackerConfig()
            .setThreshold(threshold)
            .setTimeUnit(ChronoUnit.MILLIS);

        TimerNinjaBlock.measure("query execution", config, () -> {
            database.execute(query);
        });
    }

    private int calculateThreshold(int expectedRows) {
        return Math.min(100 + (expectedRows / 10), 1000);
    }
}
```

### Threshold Tiers

```java
@Service
public class TieredTrackingService {

    @TimerNinjaTracker(threshold = 50)    // Fast operations
    public void cacheLookup(String key) { }

    @TimerNinjaTracker(threshold = 200)   // Standard operations
    public void databaseQuery(String query) { }

    @TimerNinjaTracker(threshold = 1000)  // Slow operations
    public void externalApiCall(String endpoint) { }

    @TimerNinjaTracker(threshold = 5000)  // Very slow operations
    public void batchProcess(String batchId) { }
}
```

---

## Block Tracking Patterns

### Pattern 1: Phased Processing

```java
@Service
public class DataPipelineService {

    @TimerNinjaTracker
    public void runPipeline(String dataId) {
        RawData raw = TimerNinjaBlock.measure("extract", () -> {
            return extractor.extract(dataId);
        });

        ProcessedData processed = TimerNinjaBlock.measure("transform", () -> {
            return transformer.transform(raw);
        });

        TimerNinjaBlock.measure("load", () -> {
            loader.load(processed);
        });

        TimerNinjaBlock.measure("cleanup", () -> {
            cleanupService.cleanup(dataId);
        });
    }
}
```

### Pattern 2: Conditional Tracking

```java
@Service
public class ConditionalTrackingService {

    @TimerNinjaTracker
    public void processWithTracking(boolean enableDetailedTracking, Data data) {
        processMain(data);

        if (enableDetailedTracking) {
            TimerNinjaBlock.measure("detailed validation", () -> {
                validateDetailed(data);
            });

            TimerNinjaBlock.measure("detailed transformation", () -> {
                transformDetailed(data);
            });
        }
    }
}
```

### Pattern 3: Retry Logic Tracking

```java
@Service
public class RetryTrackingService {

    @TimerNinjaTracker
    public Result executeWithRetry(String operation, Data data) {
        int maxRetries = 3;
        int attempt = 0;

        while (attempt < maxRetries) {
            attempt++;
            try {
                Result result = TimerNinjaBlock.measure(
                    String.format("attempt %d", attempt),
                    () -> executeOperation(operation, data)
                );
                return result;
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    throw new RuntimeException("Failed after " + maxRetries + " attempts", e);
                }
                TimerNinjaBlock.measure("retry delay", () -> {
                    Thread.sleep(calculateBackoff(attempt));
                });
            }
        }
        throw new IllegalStateException("Should not reach here");
    }
}
```

---

## Performance Optimization

### Minimizing Overhead

#### 1. Selective Tracking

```java
// ❌ Bad - tracking everything
@TimerNinjaTracker
public String getUserName(Long userId) {
    return userRepository.findById(userId).getName();
}

// ✅ Good - tracking the meaningful operation
@Repository
public class UserRepository {
    @TimerNinjaTracker
    public User findById(Long userId) {
        // Database query
    }
}
```

#### 2. Use Thresholds Effectively

```java
// ✅ Appropriate threshold
@TimerNinjaTracker(threshold = 100)
public void meaningfulOperation() {
    // Operation that should be reasonably fast
}
```

#### 3. Avoid Circular Dependencies in toString()

```java
// ❌ Bad - circular reference
public class User {
    private List<Order> orders;

    @Override
    public String toString() {
        return "User{orders=" + orders + "}"; // Orders contain Users!
    }
}

// ✅ Good - selective toString()
public class User {
    private List<Order> orders;

    @Override
    public String toString() {
        return String.format("User{id=%d, ordersCount=%d}", id, orders.size());
    }
}
```

---

## Integration with Spring Boot

```java
@Configuration
public class TimerNinjaConfig {

    @Bean
    public CommandLineRunner setupTimerNinja() {
        return args -> {
            // Enable System.out for development
            if (isDevelopmentEnvironment()) {
                io.github.thanglequoc.timerninja.TimerNinjaConfiguration
                    .getInstance()
                    .toggleSystemOutLog(true);
            }
        };
    }
}

@RestController
@RequestMapping("/api")
public class ApiController {

    @TimerNinjaTracker
    @GetMapping("/data/{id}")
    public ResponseEntity<Data> getData(@PathVariable Long id) {
        return ResponseEntity.ok(dataService.findById(id));
    }
}
```

---

## Custom Logging Strategies

### Custom Log Formats

Timer Ninja uses SLF4J, so you can customize the format through your logging configuration:

**logback.xml**
```xml
<configuration>
    <appender name="STDOUT" class="ch.qos.logback.core.ConsoleAppender">
        <encoder>
            <pattern>%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level %logger{36} - %msg%n</pattern>
        </encoder>
    </appender>

    <!-- Custom format for Timer Ninja -->
    <logger name="io.github.thanglequoc.timerninja.TimerNinjaUtil" level="INFO" additivity="false">
        <appender-ref ref="STDOUT" />
    </logger>

    <root level="INFO">
        <appender-ref ref="STDOUT" />
    </root>
</configuration>
```

---

## Troubleshooting Complex Scenarios

### Missing Nested Traces

1. Verify annotations are present on nested methods
2. Check if methods are private/internal — all access levels are tracked if annotated
3. Verify AspectJ weaving is working: `aspect 'io.github.thanglequoc:timer-ninja:1.3.0'`

### Large Trace Outputs

1. Use thresholds to filter noise: `@TimerNinjaTracker(threshold = 200)`
2. Disable argument logging by default, enable selectively
3. Use block tracking for phases instead of individual method tracking

### Thread Safety

Timer Ninja is thread-safe by design. Each thread maintains its own trace context via `ThreadLocal`:

```java
@TimerNinjaTracker
public void parallelProcessing() {
    ExecutorService executor = Executors.newFixedThreadPool(4);

    // Each task gets its own trace context
    for (int i = 0; i < 10; i++) {
        final int taskId = i;
        executor.submit(() -> {
            processTask(taskId); // Independent trace per thread
        });
    }
    executor.shutdown();
}
```

---

## Best Practices Summary

### Do ✅

1. Track entry points (controllers, main methods, public services)
2. Use appropriate thresholds based on expected performance
3. Enable argument logging for debugging critical operations
4. Combine annotation and block tracking for comprehensive monitoring
5. Track external operations (API calls, database queries, file I/O)
6. Monitor constructor chains for slow initialization

### Don't ❌

1. Track every method — focus on meaningful operations
2. Use very low thresholds — creates noise
3. Log sensitive data with `includeArgs`
4. Create circular `toString()` references
5. Track simple getters/setters
6. Forget to configure AspectJ weaving
