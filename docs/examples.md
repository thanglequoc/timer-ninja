---
layout: docs
title: Examples
description: "Real-world examples demonstrating Timer Ninja usage patterns."
prev_page:
  title: User Guide
  url: /user-guide/
next_page:
  title: Advanced Usage
  url: /advanced-usage/
---

# Examples

This page provides real-world examples demonstrating Timer Ninja usage patterns.

---

## Basic Method Tracking

### Simple Tracking

```java
@TimerNinjaTracker
public void processRequest() {
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

---

## Constructor Tracking

### Service Initialization Chain

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

## Loan Processing Example

Combines annotation-based tracking with block tracking.

```java
public class LoanService {
    private UserService userService;

    @TimerNinjaTracker(includeArgs = true, threshold = 100)
    public void processLoanApplication(int userId, double loanAmount, int termMonths) {
        User user = userService.findUser(userId);

        // Phase 1: Credit check
        TimerNinjaBlock.measure("credit score check", () -> {
            simulateDelay(60);
        });

        // Phase 2: Income verification
        TimerNinjaBlock.measure("income verification", () -> {
            simulateDelay(80);
        });

        // Phase 3: Risk assessment with custom config
        BlockTrackerConfig riskConfig = new BlockTrackerConfig()
            .setTimeUnit(ChronoUnit.MILLIS)
            .setThreshold(30);

        TimerNinjaBlock.measure("risk assessment", riskConfig, () -> {
            simulateDelay(40);
        });

        // Phase 4: Final approval with return value
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

---

## E-commerce Order Processing

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

---

## API Controller Example

```java
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

## Key Takeaways

1. **Entry Point Tracking** — Track high-level methods to capture full call hierarchies
2. **Threshold Usage** — Use thresholds to filter noise and focus on slow operations
3. **Argument Tracking** — Enable `includeArgs` for debugging and performance analysis
4. **Block Tracking** — Use `TimerNinjaBlock` for granular tracking without method extraction
5. **Constructor Tracking** — Track initialization chains to identify slow startup times
6. **Mixed Tracking** — Combine annotation and block tracking for comprehensive monitoring
