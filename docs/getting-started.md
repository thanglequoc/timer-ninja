---
layout: docs
title: Getting Started
description: "How to measure Java method execution time with Timer Ninja. Install the library, annotate methods, track code blocks, and visualize performance traces."
prev_page:
  title: Home
  url: /
next_page:
  title: Advanced Usage
  url: /advanced-usage/
---

# Getting Started

This guide covers everything you need to install Timer Ninja and start tracking method execution time.

---

## Installation

### Add the Timer Ninja Dependency

**Gradle:**
```groovy
implementation group: 'io.github.thanglequoc', name: 'timer-ninja', version: '1.3.0'
```

**Maven:**
```xml
<dependency>
    <groupId>io.github.thanglequoc</groupId>
    <artifactId>timer-ninja</artifactId>
    <version>1.3.0</version>
    <scope>compile</scope>
</dependency>
```

### Declare AspectJ Plugin

**Gradle** — using [FreeFair AspectJ Gradle plugin](https://github.com/freefair/gradle-plugins):

```groovy
plugins {
    id "io.freefair.aspectj.post-compile-weaving" version '9.1.0'
}

dependencies {
    implementation group: 'io.github.thanglequoc', name: 'timer-ninja', version: '1.3.0'
    aspect 'io.github.thanglequoc:timer-ninja:1.3.0'

    // Enable this if you want to track methods in test classes
    testAspect("io.github.thanglequoc:timer-ninja:1.3.0")
}
```

**Maven** — using [Forked Mojo's AspectJ Plugin](https://github.com/dev-aspectj/aspectj-maven-plugin):

```xml
<plugin>
    <groupId>dev.aspectj</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.14.1</version>
    <dependencies>
        <dependency>
            <groupId>org.aspectj</groupId>
            <artifactId>aspectjtools</artifactId>
            <version>1.9.25</version>
        </dependency>
    </dependencies>
    <configuration>
        <complianceLevel>${java.version}</complianceLevel>
        <aspectLibraries>
            <aspectLibrary>
                <groupId>io.github.thanglequoc</groupId>
                <artifactId>timer-ninja</artifactId>
            </aspectLibrary>
        </aspectLibraries>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>compile</goal>
                <goal>test-compile</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

### Set Up Logging Output

Timer Ninja uses **SLF4J** for logging. Depending on your project, choose one of the following:

**Spring Boot projects** — no extra setup needed. Spring Boot ships with Logback, so Timer Ninja output will appear in your logs automatically.

**Non-Spring projects with an SLF4J provider** — if you already have Logback, Log4j2, or another SLF4J-compatible logging framework, you're good to go. Just make sure the log level for `io.github.thanglequoc.timerninja.TimerNinjaUtil` is at least `INFO`.

**No logging framework?** — enable System.out output at application startup:

```java
TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
```

This prints trace output directly to the console — useful for quick testing or simple console applications.

---

## Annotation-based Tracking

The `@TimerNinjaTracker` annotation is the primary way to track method execution time.

### Basic Usage

Annotate any method to start tracking:

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

| Attribute | Type | Default | Description |
|-----------|------|---------|-------------|
| `enabled` | `boolean` | `true` | Enable or disable tracking for this method |
| `timeUnit` | `ChronoUnit` | `MILLIS` | Time unit for measurement (SECONDS, MILLIS, MICROS) |
| `includeArgs` | `boolean` | `false` | Include method arguments in the log trace |
| `threshold` | `int` | `-1` | Minimum execution time required to log (in specified timeUnit) |

---

## Configuration Options

### Enable/Disable Tracking

Temporarily disable tracking for a method without removing the annotation:

```java
@TimerNinjaTracker(enabled = false)
public void dontTrackThis() {
    // This will NOT be tracked
}
```

### Time Unit Selection

Choose the appropriate time unit for your measurement:

```java
import java.time.temporal.ChronoUnit;

@TimerNinjaTracker(timeUnit = ChronoUnit.SECONDS)
public void longRunningOperation() { }

@TimerNinjaTracker(timeUnit = ChronoUnit.MILLIS)   // default
public void standardOperation() { }

@TimerNinjaTracker(timeUnit = ChronoUnit.MICROS)
public void preciseOperation() { }
```

### Include Method Arguments

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

> **Important:** Ensure your objects have proper `toString()` implementations for meaningful output.

### Threshold Filtering

Filter out fast methods to focus on performance issues:

```java
@TimerNinjaTracker(threshold = 500)  // Only log if execution > 500ms
public void potentiallySlowMethod() {
    // Method logic
}
```

**When threshold is exceeded:**
```
public void potentiallySlowMethod() - 723 ms ¤ [Threshold Exceed !!: 500 ms]
```

**When below threshold:** The method is suppressed from the trace output. If all methods in a trace are below threshold, a summary is shown instead.

### Combining Options

```java
@TimerNinjaTracker(includeArgs = true, threshold = 200)
public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) {
    // Only logs slow transfers with full argument details
}
```

---

## Block Tracking

For granular tracking within a method without extracting separate methods, use `TimerNinjaBlock`.

### Basic Block Tracking

```java
public void processData() {
    TimerNinjaBlock.measure("database query", () -> {
        database.query("SELECT * FROM users");
    });
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

1. **Trace Context ID** — Auto-generated UUID. All tracked methods in the same call stack share this ID.
2. **Trace Timestamp** — When the trace context was initiated (UTC timezone).
3. **Start/End Markers** — Delimit the trace boundaries.
4. **Method Lines** — Each tracked method shows: signature, arguments (if enabled), execution time, threshold indicator.
5. **Indentation (`|--`)** — Shows call hierarchy. Indented methods are called by the method above.

### Summary Output

When all methods in a trace are below their thresholds:

```
Timer Ninja trace context id: abc123...
Trace timestamp: 2023-04-03T14:27:50.322Z
All 3 tracked items within threshold. min: 5 ms, max: 45 ms, total: 50 ms
```

---

## Global Configuration

### Enable System.out Logging

For simple console applications or quick testing:

```java
TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
```

> Call this once at application startup. By default, Timer Ninja uses SLF4J logging.

### Log Level

The logger class is `io.github.thanglequoc.timerninja.TimerNinjaUtil` with default level `INFO`.

To enable debug information:

```xml
<logger name="io.github.thanglequoc.timerninja.TimerNinjaThreadContext" level="DEBUG"/>
```
