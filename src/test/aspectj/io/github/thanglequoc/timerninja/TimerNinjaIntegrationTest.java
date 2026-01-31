package io.github.thanglequoc.timerninja;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.github.thanglequoc.timerninja.extension.LogCaptureExtension;
import io.github.thanglequoc.timerninja.servicesample.BankService;
import io.github.thanglequoc.timerninja.servicesample.constructorscenario.LocationService;
import io.github.thanglequoc.timerninja.servicesample.constructorscenario.TransportationService;
import io.github.thanglequoc.timerninja.servicesample.entities.BankRecordBook;
import io.github.thanglequoc.timerninja.servicesample.entities.User;
import io.github.thanglequoc.timerninja.servicesample.services.BalanceService;
import io.github.thanglequoc.timerninja.servicesample.services.NotificationService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

public class TimerNinjaIntegrationTest {

    @RegisterExtension
    private LogCaptureExtension logCaptureExtension = new LogCaptureExtension();

    @Test
    public void testTrackingOnConstructor() {
        TransportationService transportationService = new TransportationService();
        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        assertTrue(formattedMessages.get(0).contains("Timer Ninja trace context id:"));
        assertTrue(formattedMessages.get(0).contains("Trace timestamp:"));
        assertTrue(formattedMessages.get(1).contains("{===== Start of trace context id:"));

        // it should follow the constructor initialization order
        assertTrue(formattedMessages.get(2).contains("public TransportationService() -"));
        assertTrue(formattedMessages.get(3).contains("  |-- public ShippingService() -"));

        assertTrue(formattedMessages.get(4).contains("{====== End of trace context id:"));
    }

    @Test
    public void testTrackingOnConstructor_TrackerDisabled() {
        // traker on location service is disabled
        LocationService locationService = new LocationService();
        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());

        assertTrue(formattedMessages.get(0).contains("Timer Ninja trace context id:"));
        assertTrue(formattedMessages.get(0).contains("Trace timestamp:"));
        assertTrue(formattedMessages.get(1).contains("There isn't any tracker enabled in the tracking context"));
    }

    /* Integration testing */
    @Test
    public void testTrackingOnMethods() {
        BankService bankService = new BankService();
        bankService.requestMoneyTransfer(1, 2, 3000);

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());

        // Find requestMoneyTransfer trace context
        int transferContextStart = -1;
        for (int i = 0; i < formattedMessages.size(); i++) {
            if (formattedMessages.get(i).contains("public void requestMoneyTransfer")) {
                transferContextStart = i;
                break;
            }
        }
        assertTrue(transferContextStart > 0, "Should find requestMoneyTransfer in output");

        // Verify requestMoneyTransfer method is tracked with threshold exceed
        assertTrue(formattedMessages.get(transferContextStart).contains("public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount)"));
        assertTrue(formattedMessages.get(transferContextStart).contains("[Threshold Exceed !!:"));

        // Verify nested method calls are tracked
        boolean foundFindUser = false;
        boolean foundIncreaseAmount = false;
        boolean foundNotify = false;
        boolean foundNotifyViaSMS = false;
        boolean foundNotifyViaEmail = false;

        for (String message : formattedMessages) {
            if (message.contains("public User findUser(int userId)")) foundFindUser = true;
            if (message.contains("public void increaseAmount(User user, int amount)")) foundIncreaseAmount = true;
            if (message.contains("public void notify(User user)")) foundNotify = true;
            if (message.contains("private void notifyViaSMS(User user)")) foundNotifyViaSMS = true;
            if (message.contains("private void notifyViaEmail(User user)")) foundNotifyViaEmail = true;
        }

        assertTrue(foundFindUser, "Should find findUser call");
        assertTrue(foundIncreaseAmount, "Should find increaseAmount call");
        assertTrue(foundNotify, "Should find notify call");
        assertTrue(foundNotifyViaSMS, "Should find notifyViaSMS call");
        assertTrue(foundNotifyViaEmail, "Should find notifyViaEmail call");
    }

    @Test
    public void testTrackingOnMethods_MethodWithinExecutionThreshold() {
        BankRecordBook recordBook = Mockito.mock(BankRecordBook.class);
        NotificationService notificationService = Mockito.mock(NotificationService.class);

        User user = new User(1, "User A", "dummy@gmail.com");
        Map<User, Integer> balanceSheet = new HashMap<>();
        balanceSheet.put(user, 1000);

        // Mock behavior
        when(recordBook.getUserBalance()).thenReturn(balanceSheet);

        BalanceService balanceService = new BalanceService(recordBook, notificationService);
        balanceService.deductAmount(user, 200);

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());

        // @thangle: The output format for trace context that has all method meet the threshold does not look very good and still make the output
        // look redundant and feel like something is wrong that it's not printing out. We will come back and improve the trace output
        // for this case later.
        assertTrue(formattedMessages.get(0).startsWith("Timer Ninja trace context id:"));
        assertTrue(formattedMessages.get(0).contains("Trace timestamp:"));
        assertTrue(formattedMessages.get(1).startsWith("{===== Start of trace context id:"));
        assertTrue(formattedMessages.get(2).startsWith("{====== End of trace context id:"));

        // The notification service is called but there is no trace output printing out, this is the expected behavior
        // because its parent method met the threshold setting
        verify(notificationService, times(1)).notify(user);
    }

}
