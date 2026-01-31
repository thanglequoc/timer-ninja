package io.github.thanglequoc.timerninja;

import java.util.List;
import java.util.concurrent.TimeUnit;

import io.github.thanglequoc.timerninja.extension.LogCaptureExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import static org.junit.jupiter.api.Assertions.*;

public class TimerNinjaBlockTest {

    @RegisterExtension
    private LogCaptureExtension logCaptureExtension = new LogCaptureExtension();

    @Test
    public void testBasicCodeBlockTracking() {
        TimerNinjaBlock.measure(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        assertTrue(formattedMessages.get(0).contains("Timer Ninja trace context id:"));
        assertTrue(formattedMessages.get(0).contains("Trace timestamp:"));
        assertTrue(formattedMessages.get(1).contains("{===== Start of trace context id:"));
        assertTrue(formattedMessages.get(2).contains("Code Block -"));
        assertTrue(formattedMessages.get(2).contains("ms"));
        assertTrue(formattedMessages.get(3).contains("{====== End of trace context id:"));
    }

    @Test
    public void testNamedCodeBlockTracking() {
        TimerNinjaBlock.measure("data processing", () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        assertTrue(formattedMessages.get(2).contains("data processing -"));
        assertTrue(formattedMessages.get(2).contains("ms"));
    }

    @Test
    public void testCodeBlockWithReturnValue() {
        String result = TimerNinjaBlock.measure(() -> {
            try {
                TimeUnit.MILLISECONDS.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            return "test result";
        });

        assertEquals("test result", result);

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        assertTrue(formattedMessages.get(2).contains("Code Block -"));
        assertTrue(formattedMessages.get(2).contains("ms"));
    }

    @Test
    public void testCodeBlockWithReturnValueAndName() {
        int result = TimerNinjaBlock.measure("calculation", () -> {
            int sum = 0;
            for (int i = 0; i < 100; i++) {
                sum += i;
            }
            return sum;
        });

        assertEquals(4950, result);

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        assertTrue(formattedMessages.get(2).contains("calculation -"));
    }

    @Test
    public void testCodeBlockWithCustomTimeUnit() {
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setTimeUnit(java.time.temporal.ChronoUnit.SECONDS);

        TimerNinjaBlock.measure("slow operation", config, () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        assertTrue(formattedMessages.get(2).contains("slow operation -"));
        assertTrue(formattedMessages.get(2).contains("s")); // seconds unit
    }

    @Test
    public void testCodeBlockWithThreshold() {
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setThreshold(200); // 200ms threshold

        // This block takes about 100ms, which is below the threshold
        TimerNinjaBlock.measure("fast operation", config, () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        // The block should be filtered out from the trace because it's below threshold
        // The trace will show start and end markers but no block content
        assertTrue(formattedMessages.get(0).contains("Timer Ninja trace context id:"));
        assertTrue(formattedMessages.get(1).contains("{===== Start of trace context id:"));
        assertTrue(formattedMessages.get(2).contains("{====== End of trace context id:"));
    }

    @Test
    public void testCodeBlockExceedingThreshold() {
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setThreshold(50); // 50ms threshold

        // This block takes about 100ms, which exceeds the threshold
        TimerNinjaBlock.measure("slow operation", config, () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        // The block should be included and show threshold exceed warning
        assertTrue(formattedMessages.get(2).contains("slow operation -"));
        assertTrue(formattedMessages.get(2).contains("[Threshold Exceed !!:"));
    }

    @Test
    public void testCodeBlockDisabled() {
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setEnabled(false);

        TimerNinjaBlock.measure("disabled block", config, () -> {
            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        // The block should not be tracked
        assertTrue(formattedMessages.get(1).contains("There isn't any tracker enabled in the tracking context"));
    }

    @Test
    public void testNestedCodeBlocks() {
        TimerNinjaBlock.measure("outer block", () -> {
            TimerNinjaBlock.measure("inner block 1", () -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            TimerNinjaBlock.measure("inner block 2", () -> {
                try {
                    TimeUnit.MILLISECONDS.sleep(50);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        // Should show nested structure
        assertTrue(formattedMessages.get(2).contains("outer block -"));
        assertTrue(formattedMessages.get(3).startsWith("  |-- inner block 1 -"));
        assertTrue(formattedMessages.get(4).startsWith("  |-- inner block 2 -"));
    }

    @Test
    public void testCodeBlockWithException() {
        assertThrows(RuntimeException.class, () -> {
            TimerNinjaBlock.measure("failing block", () -> {
                throw new RuntimeException("Test exception");
            });
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        // Should still log the tracking attempt even if it fails
        assertFalse(formattedMessages.isEmpty());
    }

    @Test
    public void testNullCodeBlockThrowsException() {
        assertThrows(IllegalArgumentException.class, () -> {
            TimerNinjaBlock.measure((Runnable) null);
        });
    }

    @Test
    public void testCodeBlockWithMicroseconds() {
        BlockTrackerConfig config = new BlockTrackerConfig()
            .setTimeUnit(java.time.temporal.ChronoUnit.MICROS);

        TimerNinjaBlock.measure("micro operation", config, () -> {
            // Very short operation
            int sum = 0;
            for (int i = 0; i < 1000; i++) {
                sum += i;
            }
        });

        List<String> formattedMessages = logCaptureExtension.getFormattedMessages();
        assertFalse(formattedMessages.isEmpty());
        
        assertTrue(formattedMessages.get(2).contains("micro operation -"));
        assertTrue(formattedMessages.get(2).contains("µs")); // microseconds unit
    }
}