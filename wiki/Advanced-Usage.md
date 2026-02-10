# Advanced Usage 🚀

This guide covers advanced features and optimization techniques for Timer Ninja.

## Table of Contents

1. [Nested Tracking Deep Dive](#nested-tracking-deep-dive)
2. [Advanced Threshold Strategies](#advanced-threshold-strategies)
3. [Block Tracking Patterns](#block-tracking-patterns)
4. [Performance Optimization](#performance-optimization)
5. [Integration with Other Libraries](#integration-with-other-libraries)
6. [Custom Logging Strategies](#custom-logging-strategies)
7. [Troubleshooting Complex Scenarios](#troubleshooting-complex-scenarios)

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
        // Validation logic
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

### Output with Multi-Level Nesting

```
{===== Start of trace context id: abc123... =====}
public void processOrder(Order order) - 1850 ms
   |-- private void validateOrder(Order order) - 450 ms
   |   |-- private void validateCustomer(Long customerId) - 320 ms
   |   |   |-- public Customer findById(Long customerId) - 280 ms
   |   |-- private void validateItems(List<OrderItem> items) - 110 ms
   |       |-- private void validateItem(OrderItem item) - 52 ms ¤ [Threshold Exceed !!: 50 ms]
   |       |-- private void validateItem(OrderItem item) - 48 ms
   |-- public void processPayment(Order order) - 1200 ms
   |-- public void shipOrder(Order order) - 200 ms
{====== End of trace context id: abc123... ======}
```

### Key Points

1. **Automatic Hierarchy**: Timer Ninja automatically builds the call tree
2. **Indentation Levels**: Each nested level is indented with `|--`
3. **Independent Thresholds**: Each method can have its own threshold
4. **Context Sharing**: All methods in a call chain share the same trace context ID

---

## Advanced Threshold Strategies

### Dynamic Thresholds Based on Input

```java
@Service
public class QueryService {
    
    @TimerNinjaTracker(includeArgs = true)
    public void executeQuery(String query, int expectedRows) {
        // Use block tracking with dynamic threshold
        int threshold = calculateThreshold(expectedRows);
        
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setThreshold(threshold)
            .setTimeUnit(ChronoUnit.MILLIS);
        
        TimerNinjaBlock.measure("query execution", config, () -> {
            database.execute(query);
        });
    }
    
    private int calculateThreshold(int expectedRows) {
        // More rows = higher acceptable threshold
        return Math.min(100 + (expectedRows / 10), 1000);
    }
}
```

### Adaptive Thresholds in Production

```java
@Service
public class AdaptiveTrackingService {
    
    private final Map<String, Integer> adaptiveThresholds = new ConcurrentHashMap<>();
    
    @TimerNinjaTracker(includeArgs = true)
    public void processRequest(String operation, RequestData data) {
        int threshold = adaptiveThresholds.getOrDefault(operation, 200);
        
        // Track execution
        long startTime = System.currentTimeMillis();
        try {
            // Process the request
            performOperation(operation, data);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            
            // Adjust threshold based on observed performance
            adjustThreshold(operation, duration, threshold);
        }
    }
    
    private void adjustThreshold(String operation, long duration, int currentThreshold) {
        if (duration > currentThreshold * 2) {
            // Consistently slow - increase threshold
            adaptiveThresholds.put(operation, (int) (currentThreshold * 1.5));
        } else if (duration < currentThreshold / 2 && duration > 50) {
            // Faster than expected - consider lowering threshold
            adaptiveThresholds.put(operation, (int) (currentThreshold * 0.8));
        }
    }
}
```

### Threshold Tiers

```java
@Service
public class TieredTrackingService {
    
    // Fast operations
    @TimerNinjaTracker(threshold = 50)
    public void cacheLookup(String key) {
        // Very fast cache access
    }
    
    // Standard operations
    @TimerNinjaTracker(threshold = 200)
    public void databaseQuery(String query) {
        // Standard database operation
    }
    
    // Slow operations
    @TimerNinjaTracker(threshold = 1000)
    public void externalApiCall(String endpoint) {
        // External API call
    }
    
    // Very slow operations
    @TimerNinjaTracker(threshold = 5000)
    public void batchProcess(String batchId) {
        // Batch processing
    }
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
        // Phase 1: Extraction
        RawData raw = TimerNinjaBlock.measure("extract", () -> {
            return extractor.extract(dataId);
        });
        
        // Phase 2: Transformation
        ProcessedData processed = TimerNinjaBlock.measure("transform", () -> {
            return transformer.transform(raw);
        });
        
        // Phase 3: Loading
        TimerNinjaBlock.measure("load", () -> {
            loader.load(processed);
        });
        
        // Phase 4: Cleanup
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
        // Always track main processing
        processMain(data);
        
        // Conditionally track detailed steps
        if (enableDetailedTracking) {
            TimerNinjaBlock.measure("detailed validation", () -> {
                validateDetailed(data);
            });
            
            TimerNinjaBlock.measure("detailed transformation", () -> {
                transformDetailed(data);
            });
        } else {
            // Fast path without detailed tracking
            validateQuick(data);
            transformQuick(data);
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
                    throw new RuntimeException("Operation failed after " + maxRetries + " attempts", e);
                }
                
                // Track delay before retry
                TimerNinjaBlock.measure("retry delay", () -> {
                    try {
                        Thread.sleep(calculateBackoff(attempt));
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                    }
                });
            }
        }
        
        throw new IllegalStateException("Should not reach here");
    }
    
    private long calculateBackoff(int attempt) {
        return (long) Math.pow(2, attempt) * 100; // Exponential backoff
    }
}
```

### Pattern 4: Parallel Processing Tracking

```java
@Service
public class ParallelTrackingService {
    
    @TimerNinjaTracker
    public void processInParallel(List<Task> tasks) {
        ExecutorService executor = Executors.newFixedThreadPool(4);
        
        try {
            List<Future<Result>> futures = tasks.stream()
                .map(task -> executor.submit(() -> 
                    TimerNinjaBlock.measure(
                        "task-" + task.getId(),
                        () -> processTask(task)
                    )
                ))
                .collect(Collectors.toList());
            
            // Track the waiting/aggregation phase
            TimerNinjaBlock.measure("aggregate results", () -> {
                futures.forEach(future -> {
                    try {
                        future.get();
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                });
            });
            
        } finally {
            executor.shutdown();
        }
    }
}
```

---

## Performance Optimization

### Minimizing Overhead

Timer Ninja is designed to be lightweight, but here are tips to minimize overhead:

#### 1. Selective Tracking

```java
// ❌ Bad - tracking everything
@TimerNinjaTracker
public String getUserName(Long userId) {
    return userRepository.findById(userId).getName();
}

@TimerNinjaTracker
public String getUserEmail(Long userId) {
    return userRepository.findById(userId).getEmail();
}

// ✅ Good - tracking only the repository call
public String getUserName(Long userId) {
    return userRepository.findById(userId).getName();
}

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
// ❌ Too many fast operations cluttering logs
@TimerNinjaTracker(threshold = 1)
public void fastOperation() {
    // Very fast operation
}

// ✅ Appropriate threshold
@TimerNinjaTracker(threshold = 100)
public void meaningfulOperation() {
    // Operation that should be reasonably fast
}
```

#### 3. Avoid Circular Dependencies in toString()

```java
// ❌ Bad - circular reference in toString()
public class User {
    private List<Order> orders;
    
    @Override
    public String toString() {
        return "User{orders=" + orders + "}"; // Orders contain Users
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

### Memory Management

#### 1. Clean Up Trace Contexts

Timer Ninja automatically cleans up trace contexts, but you can ensure proper cleanup:

```java
@Service
public class SafeExecutionService {
    
    @TimerNinjaTracker
    public void executeSafely(Task task) {
        try {
            task.run();
        } catch (Exception e) {
            // Trace context is still cleaned up automatically
            throw e;
        }
    }
}
```

#### 2. Large Argument Objects

For large objects, avoid including them in logs:

```java
@TimerNinjaTracker // No includeArgs for large objects
public void processLargeData(LargeDataSet dataSet) {
    // Processing logic
}

// Or provide a summary
@TimerNinjaTracker(includeArgs = true)
public void processData(DataSummary summary) {
    // Processing with summary
}
```

---

## Integration with Other Libraries

### Spring Boot Integration

```java
@Configuration
public class TimerNinjaConfiguration {
    
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
    
    private boolean isDevelopmentEnvironment() {
        return Arrays.asList(args).contains("--dev") || 
               "dev".equals(System.getProperty("spring.profiles.active"));
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

### Custom Metrics Integration

```java
@Service
public class MetricsIntegrationService {
    
    private final MeterRegistry meterRegistry;
    
    public MetricsIntegrationService(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }
    
    @TimerNinjaTracker(includeArgs = true)
    public void trackWithMetrics(String operation, Data data) {
        // Timer Ninja tracks execution time
        // Also record custom metrics
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            executeOperation(operation, data);
            
            // Record success metrics
            sample.stop(Timer.builder("operation.duration")
                .tag("operation", operation)
                .tag("status", "success")
                .register(meterRegistry));
                
        } catch (Exception e) {
            // Record failure metrics
            sample.stop(Timer.builder("operation.duration")
                .tag("operation", operation)
                .tag("status", "failure")
                .register(meterRegistry));
                
            throw e;
        }
    }
}
```

### AOP Integration with Other Aspects

```java
@Aspect
@Component
public class MonitoringAspect {
    
    @Around("@annotation(io.github.thanglequoc.timerninja.TimerNinjaTracker)")
    public Object aroundTimerNinjaTracker(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        // Additional monitoring logic
        Metrics.increment("method.calls", "method", methodName);
        
        try {
            Object result = joinPoint.proceed();
            
            // Success metrics
            Metrics.increment("method.success", "method", methodName);
            return result;
            
        } catch (Exception e) {
            // Failure metrics
            Metrics.increment("method.failure", "method", methodName, "error", e.getClass().getSimpleName());
            throw e;
        }
    }
}
```

---

## Custom Logging Strategies

### Structured Logging

```java
@Service
public class StructuredLoggingService {
    
    @TimerNinjaTracker(includeArgs = true)
    public void processRequest(Request request) {
        // Timer Ninja provides timing
        // Add structured logging for additional context
        log.info("Processing request: {}", request.toStructuredLog());
        
        // Business logic
    }
}
```

### Conditional Argument Logging

```java
@Service
public class ConditionalArgLoggingService {
    
    @TimerNinjaTracker
    public void processSensitiveData(SensitiveData data) {
        // Never log sensitive data
        // Use block tracking for phases instead
        TimerNinjaBlock.measure("validation", () -> {
            validateData(data);
        });
        
        TimerNinjaBlock.measure("processing", () -> {
            processData(data);
        });
    }
}
```

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

### Issue 1: Missing Nested Traces

**Problem:** Nested method calls not appearing in traces.

**Solution:**

1. Verify annotations are present on nested methods
```java
@TimerNinjaTracker
public void parent() {
    child(); // Must also have @TimerNinjaTracker
}

@TimerNinjaTracker
public void child() {
    // Child logic
}
```

2. Check if methods are private/internal
```java
// Public/package-private methods are tracked
// Private methods are also tracked if annotated
```

3. Verify AspectJ weaving is working correctly
```groovy
// Ensure aspect dependency is included
aspect 'io.github.thanglequoc:timer-ninja:1.2.0'
```

### Issue 2: Performance Degradation

**Problem:** Timer Ninja causing noticeable performance impact.

**Solution:**

1. Reduce tracking overhead
```java
// Remove tracking from hot paths
// Use higher thresholds
@TimerNinjaTracker(threshold = 500) // Instead of 100
```

2. Disable in production
```java
@TimerNinjaTracker(enabled = isDevelopment())
public void debugMethod() {
    // Only tracked in dev
}
```

3. Use selective tracking
```java
// Track only entry points
@TimerNinjaTracker
public void processRequest() {
    // Nested methods not tracked
}
```

### Issue 3: Large Trace Outputs

**Problem:** Trace logs are too large and cluttered.

**Solution:**

1. Use thresholds effectively
```java
@TimerNinjaTracker(threshold = 200)
// Only shows methods taking > 200ms
```

2. Disable argument logging
```java
@TimerNinjaTracker(includeArgs = false) // Default
// Or selectively enable
@TimerNinjaTracker(includeArgs = true)
public void criticalOperation(Data data) {
    // Only log args for critical operations
}
```

3. Use block tracking for phases instead of individual methods
```java
@TimerNinjaTracker
public void processOrder(Order order) {
    TimerNinjaBlock.measure("validation", () -> validate(order));
    TimerNinjaBlock.measure("processing", () -> process(order));
    TimerNinjaBlock.measure("notification", () -> notify(order));
}
// One line instead of multiple method traces
```

### Issue 4: Thread Safety Concerns

**Problem:** Tracking in multi-threaded environments.

**Solution:**

Timer Ninja is thread-safe by design. Each thread maintains its own trace context:

```java
@TimerNinjaTracker
public void parallelProcessing() {
    ExecutorService executor = Executors.newFixedThreadPool(4);
    
    // Each task gets its own trace context
    for (int i = 0; i < 10; i++) {
        final int taskId = i;
        executor.submit(() -> {
            processTask(taskId); // Independent trace
        });
    }
    
    executor.shutdown();
}
```

---

## Best Practices Summary

### Do ✅

1. **Track entry points** (controllers, main methods, public services)
2. **Use appropriate thresholds** based on expected performance
3. **Enable argument logging** for debugging critical operations
4. **Combine annotation and block tracking** for comprehensive monitoring
5. **Track external operations** (API calls, database queries, file I/O)
6. **Monitor constructor chains** for slow initialization
7. **Use structured logging** for additional context
8. **Disable in production** when performance is critical

### Don't ❌

1. **Track every method** - focus on meaningful operations
2. **Use very low thresholds** - creates noise in logs
3. **Log sensitive data** - even with `includeArgs`
4. **Create circular toString()** - causes infinite loops
5. **Track simple getters/setters** - adds unnecessary overhead
6. **Ignore nested tracking** - leverage call hierarchy insights
7. **Forget to configure AspectJ** - tracking won't work
8. **Use in hot paths** without considering performance impact

---

## Further Reading

- **[User Guide](User-Guide)** - Detailed feature documentation
- **[Examples](Examples)** - Real-world usage patterns
- **[Home](Home)** - Quick start and installation guide