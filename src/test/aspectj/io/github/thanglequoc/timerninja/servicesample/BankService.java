package io.github.thanglequoc.timerninja.servicesample;

import io.github.thanglequoc.timerninja.BlockTrackerConfig;
import io.github.thanglequoc.timerninja.TimerNinjaBlock;
import io.github.thanglequoc.timerninja.TimerNinjaTracker;
import io.github.thanglequoc.timerninja.servicesample.entities.BankCard;
import io.github.thanglequoc.timerninja.servicesample.entities.BankRecordBook;
import io.github.thanglequoc.timerninja.servicesample.entities.User;
import io.github.thanglequoc.timerninja.servicesample.services.CardService;
import io.github.thanglequoc.timerninja.servicesample.services.NotificationService;
import io.github.thanglequoc.timerninja.servicesample.services.BalanceService;
import io.github.thanglequoc.timerninja.servicesample.services.UserService;

import java.time.temporal.ChronoUnit;

/**
 * Dummy service class for testing purpose
 */
public class BankService {

    private BalanceService balanceService;
    private CardService cardService;
    private UserService userService;

    public BankService() {
        final BankRecordBook masterRecordBook = BankRecordBook.getInstance();
        NotificationService notificationService = new NotificationService();

        try {
            Thread.sleep(90);
            this.balanceService = new BalanceService(masterRecordBook, notificationService);
            this.userService = new UserService(masterRecordBook);
            this.cardService = new CardService(balanceService);

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    /*
     * Test method to simulate transfer money
     * The threshold setting must always exceed
     */
    @TimerNinjaTracker(threshold = 200)
    public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) {
        User sourceUser = userService.findUser(sourceUserId);
        User targetUser = userService.findUser(targetUserId);
        balanceService.deductAmount(sourceUser, amount);
        balanceService.increaseAmount(targetUser, amount);
    }

    /* Test method to simulate deposit money */
    @TimerNinjaTracker(includeArgs = true, threshold = 500)
    public void depositMoney(int userId, int amount) {
        // TODO @thangle: Not yet implemented
    }

    @TimerNinjaTracker(includeArgs = true)
    public void payWithCard(int userId, BankCard card, int amount) {
        User user = userService.findUser(userId);
        cardService.charge(user, card, amount);
    }

    /**
     * Test method to simulate loan application processing.
     * This method demonstrates the integration between @TimerNinjaTracker
     * annotation
     * and TimerNinjaBlock code block tracking.
     * The overall method is tracked with @TimerNinjaTracker, while specific phases
     * are tracked using TimerNinjaBlock.measure() to show nested tracking.
     */
    @TimerNinjaTracker(includeArgs = true, threshold = 100)
    public void processLoanApplication(int userId, double loanAmount, int termMonths) {
        User user = userService.findUser(userId);

        // Phase 1: Credit check - tracked with code block tracking
        TimerNinjaBlock.measure("credit score check", () -> {
            simulateDelay(60); // Simulate credit check
        });

        // Phase 2: Income verification - tracked with code block tracking
        TimerNinjaBlock.measure("income verification", () -> {
            simulateDelay(80); // Simulate income verification
        });

        // Phase 3: Risk assessment - tracked with code block tracking and custom config
        BlockTrackerConfig riskConfig = new BlockTrackerConfig()
                .setTimeUnit(ChronoUnit.MILLIS)
                .setThreshold(30);

        TimerNinjaBlock.measure("risk assessment", riskConfig, () -> {
            simulateDelay(40); // Simulate risk assessment
        });

        // Phase 4: Final approval - tracked with code block tracking
        String approvalStatus = TimerNinjaBlock.measure("final approval", () -> {
            simulateDelay(50); // Simulate approval process
            return "APPROVED";
        });
    }

    /**
     * Helper method to simulate processing delay for demonstration purposes.
     */
    private void simulateDelay(int milliseconds) {
        try {
            Thread.sleep(milliseconds);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }

    /**
     * Test static method
     */
    @TimerNinjaTracker(threshold = 0)
    public static void printBankInfo() {
        try {
            System.out.println("Bank Info: TimerNinja Bank");
            Thread.sleep(50);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TimerNinjaTracker(includeArgs = true, threshold = 0)
    public static void printWithArgs(String message, int count) {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TimerNinjaTracker(threshold = 0)
    public static void printAndThrow() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        throw new RuntimeException("Simulated static error");
    }

    @TimerNinjaTracker(threshold = 0)
    public static void nestedStaticMethodA() {
        try {
            Thread.sleep(20);
            nestedStaticMethodB();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @TimerNinjaTracker(threshold = 0)
    public static void nestedStaticMethodB() {
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

}
