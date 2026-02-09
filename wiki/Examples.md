# Examples 💡

This page provides real-world examples demonstrating Timer Ninja usage patterns.

## Table of Contents

1. [Basic Method Tracking](#basic-method-tracking)
2. [Banking Service Example](#banking-service-example)
3. [Notification Service Example](#notification-service-example)
4. [Constructor Tracking](#constructor-tracking)
5. [Loan Processing Example](#loan-processing-example)
6. [E-commerce Order Processing](#e-commerce-order-processing)
7. [API Controller Example](#api-controller-example)

---

## Basic Method Tracking

### Simple Tracking

```java
@TimerNinjaTracker
public void processRequest() {
    // Business logic
    System.out.println("Processing request...");
}
```

**Output:**
```
{===== Start of trace context id: abc123... =====}
public void processRequest() - 42 ms
{====== End of trace context id: abc123... ======}
```

### With Time Unit

```java
@TimerNinjaTracker(timeUnit = ChronoUnit.MICROS)
public void calculateMetrics() {
    // Precision calculation
}
```

**Output:**
```
public void calculateMetrics() - 52341 µs
```

---

## Banking Service Example

This example shows a comprehensive banking service with multiple tracking scenarios.

### Money Transfer Service

```java
public class BankService {
    private BalanceService balanceService;
    private UserService userService;
    private NotificationService notificationService;

    public BankService() {
        BankRecordBook masterRecordBook = BankRecordBook.getInstance();
        this.notificationService = new NotificationService();
        this.balanceService = new BalanceService(masterRecordBook, notificationService);
        this.userService = new UserService(masterRecordBook);
    }

    /**
     * Transfer money between users with threshold tracking
     */
    @TimerNinjaTracker(threshold = 200)
    public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) {
        User sourceUser = userService.findUser(sourceUserId);
        User targetUser = userService.findUser(targetUserId);
        balanceService.deductAmount(sourceUser, amount);
        balanceService.increaseAmount(targetUser, amount);
    }

    /**
     * Deposit money with argument tracking
     */
    @TimerNinjaTracker(includeArgs = true, threshold = 500)
    public void depositMoney(int userId, int amount) {
        // Deposit logic
    }

    /**
     * Payment with card - full argument tracking
     */
    @TimerNinjaTracker(includeArgs = true)
    public void payWithCard(int userId, BankCard card, int amount) {
        User user = userService.findUser(userId);
        // Card payment logic
    }
}
```

### Output Example

```
{===== Start of trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c =====}
public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) - 1037 ms ¤ [Threshold Exceed !!: 200 ms]
   |-- public User findUser(int userId) - 105 ms
   |-- public User findUser(int userId) - 108 ms
   |-- public void deductAmount(User user, int amount) - 306 ms
   |-- public void increaseAmount(User user, int amount) - 418 ms
{====== End of trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c ======}
```

---

## Notification Service Example

This example demonstrates nested method tracking with multiple levels.

### Notification System

```java
public class NotificationService {
    
    public NotificationService() {
        // Constructor initialization
    }

    @TimerNinjaTracker
    public void notify(User user) {
        // Main notification method calls sub-methods
        notifyViaSMS(user);
        notifyViaEmail(user);
    }

    @TimerNinjaTracker
    private void notifyViaSMS(User user) {
        // SMS notification logic
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TimerNinjaTracker
    private void notifyViaEmail(User user) {
        // Email notification logic
        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
```

### Output Example

```
{===== Start of trace context id: abc123... =====}
public void notify(User user) - 258 ms
   |-- private void notifyViaSMS(User user) - 53 ms
   |-- private void notifyViaEmail(User user) - 205 ms
{====== End of trace context id: abc123... ======}
```

---

## Constructor Tracking

### Service Initialization Chain

```java
public class TransportationService {
    private ShippingService shippingService;

    @TimerNinjaTracker
    public TransportationService() {
        this.shippingService = new ShippingService();
        // Additional initialization
    }
}

public class ShippingService {
    @TimerNinjaTracker
    public ShippingService() {
        // Shipping service initialization
    }
}
```

### Usage
```java
TransportationService service = new TransportationService();
```

### Output Example
```
{===== Start of trace context id: def456... =====}
public TransportationService() - 150 ms
   |-- public ShippingService() - 80 ms
{====== End of trace context id: def456... ======}
```

### Disabled Constructor Tracking

```java
public class LocationService {
    @TimerNinjaTracker(enabled = false)
    public LocationService() {
        // This won't be tracked
    }
}
```

**Output:**
```
There isn't any tracker enabled in the tracking context
```

---

## Loan Processing Example

This example combines annotation-based tracking with block tracking.

### Loan Application Processing

```java
public class LoanService {
    private UserService userService;

    public LoanService(UserService userService) {
        this.userService = userService;
    }

    /**
     * Process loan application with multi-phase tracking
     * Combines @TimerNinjaTracker with TimerNinjaBlock for granular tracking
     */
    @TimerNinjaTracker(includeArgs = true, threshold = 100)
    public void processLoanApplication(int userId, double loanAmount, int termMonths) {
        User user = userService.findUser(userId);
        
        // Phase 1: Credit check - block tracking
        TimerNinjaBlock.measure("credit score check", () -> {
            simulateDelay(60); // Simulate credit check
        });
        
        // Phase 2: Income verification - block tracking
        TimerNinjaBlock.measure("income verification", () -> {
            simulateDelay(80); // Simulate income verification
        });
        
        // Phase 3: Risk assessment - block tracking with custom config
        BlockTrackerConfig riskConfig = new BlockTrackerConfig()
            .setTimeUnit(ChronoUnit.MILLIS)
            .setThreshold(30);
        
        TimerNinjaBlock.measure("risk assessment", riskConfig, () -> {
            simulateDelay(40); // Simulate risk assessment
        });
        
        // Phase 4: Final approval - block tracking with return value
        String approvalStatus = TimerNinjaBlock.measure("final approval", () -> {
            simulateDelay(50); // Simulate approval process
            return "APPROVED";
        });
    }
    
    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}
```

### Output Example

```
{===== Start of trace context id: ghi789... =====}
public void processLoanApplication(int userId, double loanAmount, int termMonths) - Args: [userId={123}, loanAmount={50000.0}, termMonths={36}] - 345 ms
   |-- [Block] credit score check - 60 ms
   |-- [Block] income verification - 80 ms
   |-- [Block] risk assessment - 40 ms
   |-- [Block] final approval - 50 ms
{====== End of trace context id: ghi789... ======}
```

---

## E-commerce Order Processing

### Order Service Example

```java
@Service
public class OrderService {
    
    private PaymentService paymentService;
    private InventoryService inventoryService;
    private NotificationService notificationService;

    @TimerNinjaTracker
    public Order createOrder(OrderRequest request) {
        // Phase 1: Validate order
        Order order = validateAndCreateOrder(request);
        
        // Phase 2: Process payment
        PaymentResult paymentResult = processPayment(order);
        
        // Phase 3: Update inventory
        updateInventory(order);
        
        // Phase 4: Send confirmation
        sendConfirmation(order);
        
        return order;
    }

    @TimerNinjaTracker(threshold = 500, includeArgs = true)
    private PaymentResult processPayment(Order order) {
        return paymentService.charge(
            order.getUserId(), 
            order.getPaymentMethod(), 
            order.getTotalAmount()
        );
    }

    @TimerNinjaTracker(threshold = 200)
    private void updateInventory(Order order) {
        order.getItems().forEach(item -> {
            inventoryService.deductStock(item.getProductId(), item.getQuantity());
        });
    }

    @TimerNinjaTracker
    private void sendConfirmation(Order order) {
        notificationService.sendEmailConfirmation(order.getUserEmail(), order);
    }
}
```

### Output Example

```
{===== Start of trace context id: jkl012... =====}
public Order createOrder(OrderRequest request) - 2150 ms
   |-- public Order validateAndCreateOrder(OrderRequest request) - 120 ms
   |-- public PaymentResult processPayment(Order order) - Args: [order={id=ORD-12345, userId=789, amount=99.99}] - 1250 ms ¤ [Threshold Exceed !!: 500 ms]
      |-- public PaymentResult charge(int userId, String paymentMethod, double amount) - 1180 ms
   |-- public void updateInventory(Order order) - 450 ms
   |-- public void sendConfirmation(Order order) - 330 ms
{====== End of trace context id: jkl012... ======}
```

---

## API Controller Example

### REST Controller with Tracking

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    
    private UserService userService;
    private CacheService cacheService;

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

    @TimerNinjaTracker(threshold = 200)
    @PutMapping("/{id}")
    public ResponseEntity<User> updateUser(
        @PathVariable Long id, 
        @RequestBody UpdateUserRequest request
    ) {
        User user = userService.update(id, request);
        return ResponseEntity.ok(user);
    }

    @TimerNinjaTracker
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

### Output Example for GET Request

```
{===== Start of trace context id: mno345... =====}
public ResponseEntity<User> getUser(Long id) - 85 ms
   |-- public User findById(Long id) - 70 ms
      |-- public User queryDatabase(Long id) - 65 ms
{====== End of trace context id: mno345... ======}
```

### Output Example for POST Request

```
{===== Start of trace context id: pqr678... =====}
public ResponseEntity<User> createUser(CreateUserRequest request) - Args: [request={name='John Doe', email=john@example.com}] - 350 ms ¤ [Threshold Exceed !!: 100 ms]
   |-- public User create(CreateUserRequest request) - 320 ms
      |-- public void validateRequest(CreateUserRequest request) - 20 ms
      |-- public User saveToDatabase(User user) - 280 ms
      |-- public void invalidateCache(Long userId) - 15 ms
{====== End of trace context id: pqr678... ======}
```

---

## Data Processing Pipeline

### Batch Processing Example

```java
@Service
public class DataProcessingService {
    
    private DataExtractor extractor;
    private DataTransformer transformer;
    private DataLoader loader;

    @TimerNinjaTracker
    public void processBatch(String batchId) {
        // Phase 1: Extract data
        List<DataRecord> records = extractor.extract(batchId);
        
        // Phase 2: Transform data
        List<ProcessedRecord> processed = transformRecords(records);
        
        // Phase 3: Load data
        loader.load(processed);
    }

    private List<ProcessedRecord> transformRecords(List<DataRecord> records) {
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setTimeUnit(ChronoUnit.SECONDS)
            .setThreshold(1);
        
        return TimerNinjaBlock.measure("transform records", config, () -> {
            return records.stream()
                .map(this::transform)
                .collect(Collectors.toList());
        });
    }

    @TimerNinjaTracker(threshold = 50)
    private ProcessedRecord transform(DataRecord record) {
        return transformer.transform(record);
    }
}
```

### Output Example

```
{===== Start of trace context id: stu901... =====}
public void processBatch(String batchId) - 5230 ms
   |-- public List<DataRecord> extract(String batchId) - 1500 ms
   |-- [Block] transform records - 3 s
      |-- public ProcessedRecord transform(DataRecord record) - 52 ms ¤ [Threshold Exceed !!: 50 ms]
      |-- public ProcessedRecord transform(DataRecord record) - 55 ms ¤ [Threshold Exceed !!: 50 ms]
      |-- public ProcessedRecord transform(DataRecord record) - 48 ms
      |-- ... (100 more records)
   |-- public void load(List<ProcessedRecord> records) - 230 ms
{====== End of trace context id: stu901... ======}
```

---

## Key Takeaways

1. **Entry Point Tracking**: Track high-level methods to capture full call hierarchies
2. **Threshold Usage**: Use thresholds to filter noise and focus on slow operations
3. **Argument Tracking**: Enable `includeArgs` for debugging and performance analysis
4. **Block Tracking**: Use `TimerNinjaBlock` for granular tracking without method extraction
5. **Constructor Tracking**: Track initialization chains to identify slow startup times
6. **Mixed Tracking**: Combine annotation and block tracking for comprehensive monitoring

---

## Further Reading

- **[User Guide](User-Guide)** - Detailed feature documentation
- **[Advanced Usage](Advanced-Usage)** - Advanced features and optimization
- **[Home](Home)** - Quick start and installation guide