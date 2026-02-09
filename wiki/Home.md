<p align="center"><img alt="timer-ninja-mascot" src="https://i.postimg.cc/02xG4vmH/timer-ninja-mascot.png" width="120" /></p>
<p style="font-size: 25px" align="center"><b>A sneaky library for Java Method Timing</b></p>

# Timer Ninja

**Timer Ninja** is a lightweight Java library that makes measuring method execution time effortless. Simply annotate your methods, and it automatically tracks execution duration, preserves the full call hierarchy, and displays it in a clear, visual call tree. With support for multiple time units and optional argument logging, Timer Ninja provides instant insights into your code's performance.

Built on Aspect-Oriented Programming (AOP) with AspectJ, it integrates seamlessly into your application with minimal setup.

## Why Timer Ninja?

### Traditional Approach ❌

Measuring execution time typically requires manual timestamp capture:

```java
long beforeExecution = System.currentTimeMillis();
doSomethingInteresting();
long afterExecution = System.currentTimeMillis();
System.out.println("Execution time (ms): " + (afterExecution - beforeExecution));
```

This leads to:
- Excessive boilerplate code
- Reduced maintainability
- Difficult to track call hierarchies
- Error-prone as codebase grows

### Timer Ninja Approach ✅

Just annotate and track:

```java
@TimerNinjaTracker
public String doSomethingInteresting() {
    // Method logic
}
```

Timer Ninja automatically:
- Tracks execution time
- Preserves call hierarchy
- Visualizes execution stack
- Filters by thresholds
- Optionally logs arguments

## Key Features

- **Annotation-based Tracking**: Simply annotate methods or constructors with `@TimerNinjaTracker`
- **Nested Tracking**: Visualize call hierarchies with properly indented logs
- **Block Tracking**: Measure arbitrary code blocks within methods using `TimerNinjaBlock`
- **Flexible Configuration**: Set thresholds, time units, and toggle argument logging
- **Zero-Intrusion**: Keep your source code clean and focused on business logic
- **SLF4J Integration**: Seamlessly integrates with Logback, Log4j2, and other logging frameworks
- **Multiple Time Units**: Support for Seconds, Milliseconds, and Microseconds

## Installation

### Step 1: Add the Timer Ninja Dependency

**Gradle**
```groovy
implementation 'io.github.thanglequoc:timer-ninja:1.2.0'
aspect 'io.github.thanglequoc:timer-ninja:1.2.0'

// Enable this if you want to track methods in Test classes
testAspect 'io.github.thanglequoc:timer-ninja:1.2.0'
```

**Maven**
```xml
<dependency>
    <groupId>io.github.thanglequoc</groupId>
    <artifactId>timer-ninja</artifactId>
    <version>1.2.0</version>
</dependency>
```

### Step 2: Configure AspectJ Plugin

**Gradle** - Using FreeFair AspectJ plugin:
```groovy
plugins {
    id "io.freefair.aspectj.post-compile-weaving" version '9.1.0'
}
```

**Maven** - Using AspectJ Maven plugin:
```xml
<plugin>
    <groupId>dev.aspectj</groupId>
    <artifactId>aspectj-maven-plugin</artifactId>
    <version>1.14.1</version>
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

## Quick Start

1. **Add the dependency and AspectJ plugin** to your project
2. **Annotate** methods with `@TimerNinjaTracker`
3. **Run** your application
4. **View** the execution trace in your logs

```java
@TimerNinjaTracker
public void processData() {
    // Your business logic
}
```

**Output:**
```
Timer Ninja trace context id: a1b2c3d4-e5f6-7890-abcd-ef1234567890
Trace timestamp: 2023-04-03T14:27:50.322Z
{===== Start of trace context id: a1b2c3d4-e5f6-7890-abcd-ef1234567890 =====}
public void processData() - 42 ms
{====== End of trace context id: a1b2c3d4-e5f6-7890-abcd-ef1234567890 ======}
```

## Logging Setup

Timer Ninja uses **SLF4J** for logging. If you already have an SLF4J provider (Logback, Log4j2), you're ready to go!

- **Spring Boot projects**: Already configured with Logback - no setup needed
- **Other projects**: Add a logging provider like [Logback](https://logback.qos.ch/)

For quick testing without a logging framework, enable System.out output:

```java
TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
```

The logger class is `io.github.thanglequoc.timerninja.TimerNinjaUtil` at `INFO` level.

## Example Trace Output

Here's a realistic example showing nested method calls with arguments:

```
Timer Ninja trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c
Trace timestamp: 2023-04-03T07:16:48.491Z
{===== Start of trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c =====}
public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) - Args: [sourceUserId={1}, targetUserId={2}, amount={500}] - 1747 ms
   |-- public User findUser(int userId) - 105000 µs
   |-- public void processPayment(User user, int amount) - Args: [user={name='John Doe', email=johndoe@gmail.com}, amount={500}] - 770 ms
     |-- public boolean changeAmount(User user, int amount) - 306 ms
     |-- public void notify(User user) - 258 ms
       |-- private void notifyViaSMS(User user) - 53 ms
       |-- private void notifyViaEmail(User user) - 205 ms
{====== End of trace context id: 851ac23b-2669-4883-8c97-032b8fd2d45c ======}
```

## Documentation

- **[User Guide](User-Guide)** - Detailed feature documentation and configuration options
- **[Examples](Examples)** - Real-world usage examples and patterns
- **[Advanced Usage](Advanced-Usage)** - Advanced features and optimization techniques

## Example Projects

Complete working examples:

- [Spring Boot - Ninja Coffee Shop - Gradle](https://github.com/thanglequoc/timer-ninja-examples/tree/main/ninja-coffee-shop-gradle)
- [Spring Boot - Ninja Coffee Shop - Maven](https://github.com/thanglequoc/timer-ninja-examples/tree/main/ninja-coffee-shop-maven)

## License

This project is licensed under the terms specified in the repository.

---

🦥 **Project logo generated by [Google Gemini](https://gemini.google.com)**