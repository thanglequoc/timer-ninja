---
layout: docs
title: Advanced Usage
description: "Advanced Java method timing patterns — nested call tracking, dynamic thresholds, Spring Boot integration, and performance optimization with Timer Ninja."
prev_page:
  title: Getting Started
  url: /getting-started/
---

# Advanced Usage

Real-world examples, advanced patterns, and optimization techniques for Timer Ninja.

---

## Real-World Examples

### Banking Service

A comprehensive banking service demonstrating thresholds, argument tracking, and nested call hierarchies.

```java
public class BankService {
    private BalanceService balanceService;
    private UserService userService;
    private NotificationService notificationService;

    @TimerNinjaTracker(threshold = 200)
    public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) {
        User sourceUser = userService.findUser(sourceUserId);
        User targetUser = userService.findUser(targetUserId);
        balanceService.deductAmount(sourceUser, amount);
        balanceService.increaseAmount(targetUser, amount);
    }

    @TimerNinjaTracker(includeArgs = true, threshold = 500)
    public void depositMoney(int userId, int amount) {
        // Deposit logic
    }

    @TimerNinjaTracker(includeArgs = true)
    public void payWithCard(int userId, BankCard card, int amount) {
        User user = userService.findUser(userId);
        // Card payment logic
    }
}
```

**Output:**
```
{===== Start of trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c =====}
public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) - 1037 ms ¤ [Threshold Exceed !!: 200 ms]
   |-- public User findUser(int userId) - 105 ms
   |-- public User findUser(int userId) - 108 ms
   |-- public void deductAmount(User user, int amount) - 306 ms
   |-- public void increaseAmount(User user, int amount) - 418 ms
{====== End of trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c ======}
```

### Notification Service — Nested Tracking

Demonstrates nested method tracking with multiple levels.

```java
public class NotificationService {

    @TimerNinjaTracker
    public void notify(User user) {
        notifyViaSMS(user);
        notifyViaEmail(user);
    }

    @TimerNinjaTracker
    private void notifyViaSMS(User user) {
        try { Thread.sleep(50); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }

    @TimerNinjaTracker
    private void notifyViaEmail(User user) {
        try { Thread.sleep(200); }
        catch (InterruptedException e) { throw new RuntimeException(e); }
    }
}
```

**Output:**
```
{===== Start of trace context id: abc123... =====}
public void notify(User user) - 258 ms
   |-- private void notifyViaSMS(User user) - 53 ms
   |-- private void notifyViaEmail(User user) - 205 ms
{====== End of trace context id: abc123... ======}
```

### Loan Processing — Mixed Annotation + Block Tracking

Combines `@TimerNinjaTracker` with `TimerNinjaBlock` for phased tracking within a single method.

```java
public class LoanService {

    @TimerNinjaTracker(includeArgs = true, threshold = 100)
    public void processLoanApplication(int userId, double loanAmount, int termMonths) {
        User user = userService.findUser(userId);

        TimerNinjaBlock.measure("credit score check", () -> {
            simulateDelay(60);
        });

        TimerNinjaBlock.measure("income verification", () -> {
            simulateDelay(80);
        });

        BlockTrackerConfig riskConfig = new BlockTrackerConfig()
            .setTimeUnit(ChronoUnit.MILLIS)
            .setThreshold(30);

        TimerNinjaBlock.measure("risk assessment", riskConfig, () -> {
            simulateDelay(40);
        });

        String approvalStatus = TimerNinjaBlock.measure("final approval", () -> {
            simulateDelay(50);
            return "APPROVED";
        });
    }
}
```

**Output:**
```
{===== Start of trace context id: ghi789... =====}
public void processLoanApplication(int userId, double loanAmount, int termMonths) - Args: [userId={123}, loanAmount={50000.0}, termMonths={36}] - 345 ms
   |-- [Block] credit score check - 60 ms
   |-- [Block] income verification - 80 ms
   |-- [Block] risk assessment - 40 ms
   |-- [Block] final approval - 50 ms
{====== End of trace context id: ghi789... ======}
```

### E-commerce Order Processing

```java
@Service
public class OrderService {

    @TimerNinjaTracker
    public Order createOrder(OrderRequest request) {
        Order order = validateAndCreateOrder(request);
        PaymentResult paymentResult = processPayment(order);
        updateInventory(order);
        sendConfirmation(order);
        return order;
    }

    @TimerNinjaTracker(threshold = 500, includeArgs = true)
    private PaymentResult processPayment(Order order) {
        return paymentService.charge(
            order.getUserId(), order.getPaymentMethod(), order.getTotalAmount()
        );
    }

    @TimerNinjaTracker(threshold = 200)
    private void updateInventory(Order order) {
        order.getItems().forEach(item ->
            inventoryService.deductStock(item.getProductId(), item.getQuantity())
        );
    }

    @TimerNinjaTracker
    private void sendConfirmation(Order order) {
        notificationService.sendEmailConfirmation(order.getUserEmail(), order);
    }
}
```

**Output:**
```
{===== Start of trace context id: jkl012... =====}
public Order createOrder(OrderRequest request) - 2150 ms
   |-- public Order validateAndCreateOrder(OrderRequest request) - 120 ms
   |-- public PaymentResult processPayment(Order order) - Args: [order={id=ORD-12345, ...}] - 1250 ms ¤ [Threshold Exceed !!: 500 ms]
      |-- public PaymentResult charge(int userId, String paymentMethod, double amount) - 1180 ms
   |-- public void updateInventory(Order order) - 450 ms
   |-- public void sendConfirmation(Order order) - 330 ms
{====== End of trace context id: jkl012... ======}
```

### Constructor Tracking

Track constructor initialization chains to identify slow startup:

```java
public class TransportationService {
    private ShippingService shippingService;

    @TimerNinjaTracker
    public TransportationService() {
        this.shippingService = new ShippingService();
    }
}

public class ShippingService {
    @TimerNinjaTracker
    public ShippingService() {
        // Shipping service initialization
    }
}
```

**Output:**
```
{===== Start of trace context id: def456... =====}
public TransportationService() - 150 ms
   |-- public ShippingService() - 80 ms
{====== End of trace context id: def456... ======}
```

---

## Nested Tracking Deep Dive

Timer Ninja automatically detects and preserves nested method calls annotated with `@TimerNinjaTracker`, providing a complete view of the execution stack.

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

**Key points:**
- **Automatic hierarchy** — Timer Ninja builds the call tree automatically
- **Independent thresholds** — each method can have its own threshold
- **Context sharing** — all methods in a call chain share the same trace context ID

---

## Advanced Threshold Strategies

### Dynamic Thresholds with BlockTrackerConfig

Use `BlockTrackerConfig` to set thresholds dynamically based on runtime conditions:

```java
@Service
public class QueryService {

    @TimerNinjaTracker(includeArgs = true)
    public void executeQuery(String query, int expectedRows) {
        int threshold = Math.min(100 + (expectedRows / 10), 1000);

        BlockTrackerConfig config = new BlockTrackerConfig()
            .setThreshold(threshold)
            .setTimeUnit(ChronoUnit.MILLIS);

        TimerNinjaBlock.measure("query execution", config, () -> {
            database.execute(query);
        });
    }
}
```

### Threshold Tiers

Assign different thresholds based on expected operation speed:

```java
@TimerNinjaTracker(threshold = 50)     // Fast: cache lookups
public void cacheLookup(String key) { }

@TimerNinjaTracker(threshold = 200)    // Standard: database queries
public void databaseQuery(String query) { }

@TimerNinjaTracker(threshold = 1000)   // Slow: external API calls
public void externalApiCall(String endpoint) { }

@TimerNinjaTracker(threshold = 5000)   // Very slow: batch processing
public void batchProcess(String batchId) { }
```

---

## Block Tracking Patterns

### Phased Processing (ETL Pipeline)

```java
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
}
```

### Conditional Tracking

```java
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
```

### Retry Logic Tracking

```java
@TimerNinjaTracker
public Result executeWithRetry(String operation, Data data) {
    int maxRetries = 3;
    int attempt = 0;

    while (attempt < maxRetries) {
        attempt++;
        try {
            return TimerNinjaBlock.measure(
                String.format("attempt %d", attempt),
                () -> executeOperation(operation, data)
            );
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
```

---

## Integration with Spring Boot

```java
@Configuration
public class TimerNinjaConfig {

    @Bean
    public CommandLineRunner setupTimerNinja() {
        return args -> {
            if (isDevelopmentEnvironment()) {
                io.github.thanglequoc.timerninja.TimerNinjaConfiguration
                    .getInstance()
                    .toggleSystemOutLog(true);
            }
        };
    }
}

@RestController
@RequestMapping("/api/users")
public class UserController {

    @TimerNinjaTracker
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(user);
    }

    @TimerNinjaTracker(includeArgs = true, threshold = 100)
    @PostMapping
    public ResponseEntity<User> createUser(@RequestBody CreateUserRequest request) {
        User user = userService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(user);
    }
}
```

**Output for GET request:**
```
{===== Start of trace context id: mno345... =====}
public ResponseEntity<User> getUser(Long id) - 85 ms
   |-- public User findById(Long id) - 70 ms
      |-- public User queryDatabase(Long id) - 65 ms
{====== End of trace context id: mno345... ======}
```

---

## Performance Optimization

### Selective Tracking

```java
// ❌ Bad — tracking thin wrappers
@TimerNinjaTracker
public String getUserName(Long userId) {
    return userRepository.findById(userId).getName();
}

// ✅ Good — tracking the meaningful operation
@Repository
public class UserRepository {
    @TimerNinjaTracker
    public User findById(Long userId) {
        // Database query
    }
}
```

### Avoid Circular toString()

When using `includeArgs = true`, ensure argument objects don't have circular `toString()` references:

```java
// ❌ Bad — circular reference
public class User {
    private List<Order> orders;
    @Override
    public String toString() {
        return "User{orders=" + orders + "}"; // Orders contain Users!
    }
}

// ✅ Good — selective toString()
public class User {
    private List<Order> orders;
    @Override
    public String toString() {
        return String.format("User{id=%d, ordersCount=%d}", id, orders.size());
    }
}
```

---

## Custom Logging Configuration

Timer Ninja uses SLF4J. Customize the output format through your logging configuration:

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

## Troubleshooting

### No Output in Logs

1. Check if SLF4J provider is configured (Logback, Log4j2, etc.)
2. Verify log level is at least `INFO`
3. Enable `System.out` for testing: `TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);`

### Methods Not Being Tracked

1. Verify AspectJ plugin is configured correctly
2. Check that the dependency includes the aspect: `aspect 'io.github.thanglequoc:timer-ninja:1.3.0'`
3. Ensure `enabled = true` (or not set) on the annotation

### Missing Nested Traces

1. Verify `@TimerNinjaTracker` is present on nested methods — all access levels (public, private, etc.) are tracked if annotated
2. Verify AspectJ weaving is working: ensure the `aspect` dependency is declared

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

## Best Practices

### Do ✅

1. **Track entry points** — controllers, main methods, public service methods
2. **Use appropriate thresholds** — based on expected performance for each operation tier
3. **Enable argument logging selectively** — for debugging critical operations only
4. **Combine annotation and block tracking** — annotations for methods, blocks for phases within methods
5. **Track external operations** — API calls, database queries, file I/O
6. **Monitor constructor chains** — identify slow initialization
7. **Use structured logging** — configure logback/log4j2 for Timer Ninja output

### Don't ❌

1. **Track every method** — focus on meaningful operations
2. **Use very low thresholds** — creates noise in logs
3. **Log sensitive data** with `includeArgs` — mask or exclude sensitive fields
4. **Create circular `toString()` references** — causes infinite loops with argument logging
5. **Track simple getters/setters** — adds unnecessary overhead
6. **Forget to configure AspectJ weaving** — tracking won't work without it
7. **Use in hot paths** without considering performance impact
