# Timer Ninja

<p align="center"><img alt="timer-ninja-mascot" src="https://i.postimg.cc/02xG4vmH/timer-ninja-mascot.png" width="120" /></p>
<p style="font-size: 25px" align="center"><b>A sneaky library for Java Method Timing</b></p>

[![Sonartype](https://maven-badges.sml.io/sonatype-central/io.github.thanglequoc/timer-ninja/badge.svg)](https://central.sonatype.com/artifact/io.github.thanglequoc/timer-ninja)
<br/>


Timer Ninja is a lightweight Java library that makes measuring method execution time effortless. Simply annotate your methods, and it automatically tracks execution duration, preserves the full call hierarchy, and displays it in a clear, visual call tree. With support for multiple time units and optional argument logging, Timer Ninja provides instant insights into your code’s performance.  
Built on Aspect-Oriented Programming (AOP) with AspectJ, it integrates seamlessly into your application with minimal setup.

## Problem Space
Measuring code execution time is a fundamental practice in software development. Whether optimizing performance, debugging slow processes, or ensuring system efficiency, developers frequently need insights into how long the methods take to execute.

### Traditional approach
A common way to measure method execution time is by capturing timestamps before and after the method runs and calculating the difference.  
This approach is straightforward and fast but quickly becomes cumbersome. You must manually declare two timestamp points around every method that needs evaluation, leading to excessive boilerplate code and reduced maintainability. As the codebase grows, keeping track of these measurements becomes inefficient and error-prone.

```java
long beforeExecution = System.currentTimeMillis();
doSomethingInteresting();
long afterExecution = System.currentTimeMillis();
System.out.println("Execution time (ms): " + (afterExecution - beforeExecution));
```

### Timer Ninja to the rescue
[![Gemini-Generated-Image-1g42wl1g42wl1g42.png](https://i.postimg.cc/LX8KDwD1/Gemini-Generated-Image-1g42wl1g42wl1g42.png)](https://postimg.cc/4KqSN8rf)
Timer Ninja simplifies measuring method execution time by leveraging Aspect-Oriented Programming (AOP) with AspectJ under the hood. Instead of manually capturing timestamps, you simply annotate any method you want to track with the `@TimerNinjaTracker` annotation

```java
@TimerNinjaTracker
public String doSomethingInteresting() {
}
```

Timer Ninja automatically keeps track of the method execution context. If a tracked method calls another tracked method, Timer Ninja preserves the execution hierarchy, making it easy to see the call relationships and timing details in a single trace output.

**Example Timer Ninja trace output**  
```shell
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

## Installation
To use Timer Ninja, you need to do two things: add the `timer-ninja` dependency and apply an AspectJ plugin so the library’s aspects can be compiled.

### Add the Timer Ninja dependency
**Gradle**  
```groovy
implementation group: 'io.github.thanglequoc', name: 'timer-ninja', version: '1.3.0'
```

**Maven**  
```xml
<dependency>
    <groupId>io.github.thanglequoc</groupId>
    <artifactId>timer-ninja</artifactId>
    <version>1.3.0</version>
    <scope>compile</scope>
</dependency>
```

### Declare plugin to compile the aspect
**Gradle**  
You can use the [FreeFair AspectJ Gradle plugin](https://github.com/freefair/gradle-plugins)

Example project's `build.gradle`:

```groovy
plugins {
    // ...
    id "io.freefair.aspectj.post-compile-weaving" version '9.1.0'
}

dependencies {
    // ...
    // Timer ninja dependency
    implementation group: 'io.github.thanglequoc', name: 'timer-ninja', version: '1.3.0'
    aspect 'io.github.thanglequoc:timer-ninja:1.3.0'

    // Enable this if you want to track method in Test classes
	testAspect("io.github.thanglequoc:timer-ninja:1.3.0")
}
```

### Maven project
You can use the [Forked Mojo's AspectJ Plugin](https://github.com/dev-aspectj/aspectj-maven-plugin)  
Example project's `pom.xml`

```xml
<properties>
    <java.version>25</java.version>
    <aspectj.version>1.9.25</aspectj.version>
</properties>
<dependencies>
    <dependency>
        <groupId>org.aspectj</groupId>
        <artifactId>aspectjrt</artifactId>
        <version>${aspectj.version}</version>
    </dependency>
    <dependency>
        <groupId>io.github.thanglequoc</groupId>
        <artifactId>timer-ninja</artifactId>
        <version>1.3.0</version>
        <scope>compile</scope>
    </dependency>
</dependencies>

<build>
<plugins>
    <!-- Forked Codehaus Maven plugin (forked and up-to-date)
      https://github.com/dev-aspectj/aspectj-maven-plugin
     -->
    <plugin>
        <groupId>dev.aspectj</groupId>
        <artifactId>aspectj-maven-plugin</artifactId>
        <version>1.14.1</version>
        <dependencies>
            <dependency>
                <groupId>org.aspectj</groupId>
                <artifactId>aspectjtools</artifactId>
                <!-- AspectJ compiler version, in sync with runtime -->
                <version>${aspectj.version}</version>
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
                    <!-- Enable the test-compile goal if you want to use Tracker in the Java Test -->
                    <goal>test-compile</goal>
                </goals>
            </execution>
        </executions>
    </plugin>
</plugins>
</build>
```

### Getting the time trace output
The library is logging the time trace with [SLF4J Logger](https://www.slf4j.org/). So if you've already had an Slf4j provider (e.g: Logback, Log4J) in your project, then
you should be able to see the time trace output after the method executed.  
Otherwise, you will need to add a log provider into the project, my personal recommendation is [Logback](https://logback.qos.ch/) for 
robustness and simplicity. You can refer to this [Logback tutorial from Baeldung](https://www.baeldung.com/logback)

See this [Slf4j manual](https://slf4j.org/manual.html) for how to configure your logging framework with Slf4j

**Note**: Spring Boot project uses Logback as it default log provider, so you don't need to do anything here.

The logger class is `io.github.thanglequoc.timerninja.TimerNinjaUtil`, with the default log level is `INFO`.

If logging framework is not your preference, and you just want to have a quick result. Then you can choose to fall back
to the good old `System.out.println` output by executing this code **once** (since this is a singleton configuration instance). This setting will instruct
Timer Ninja to also print the time trace output to `System.out`

```java
TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
```

## `@TimerNinjaTracker` usage
Now that you're all set and ready to go. Just simply place the tracker by annotating `@TimerNinjaTracker` on any method/constructor
that you want to measure

```java
@TimerNinjaTracker
public void processPayment(User user, int amount) {
    // Method logic
}
```

### Tracker Options

The following options is available on the `@TimerNinjaTracker` annotation

```java
@TimerNinjaTracker(enabled = true, timeUnit = ChronoUnit.MILLIS, includeArgs = true, threshold = 2000)
public void processPayment(User user, int amount) {
    // The method implementation
}
```
#### Toggle tracking
Determine if this tracker should be active. Set to `false` will disable this tracker from the overall tracking trace result. Default: `true`
> @TimerNinjaTracker(enabled = false)

#### Timing Unit
The tracker allows specifying the time unit for measurement. Supported units include:  
•   Seconds (`ChronoUnit.SECONDS`)  
•   Milliseconds (`ChronoUnit.MILLIS`)  
•   Microseconds (`ChronoUnit.MICROS`)  
By default, the time unit of the tracker is **millisecond (ms)**.
```java
import java.time.temporal.ChronoUnit;

@TimerNinjaTracker(timeUnit = ChronoUnit.MICROS)
public void processPayment(User user, int amount) {

}
```

#### Include argument information in the log trace context
The tracker can optionally log the arguments passed to the tracked method. This is particularly useful for gaining insights into the input data when analyzing performance. Default: `false`

**Note**: Ensure that the `toString()` method of the argument objects is properly implemented to display meaningful details in the logs.

```java
@TimerNinjaTracker(includeArgs = true)
public void processPayment(User user, int amount) {
    // Method logic
}
```

**Sample output:**
> public void processPayment(User user, int amount) - Args: [user={name='John Doe', email=johndoe@gmail.com}, amount={500}] - 770 ms

#### Threshold setting
Timer Ninja allows you to suppress “acceptable speed” methods by defining a time threshold.
If the method’s execution time is below the threshold, it will be skipped in the final trace.  
The threshold value uses the same timeUnit defined on the tracker (default is **millisecond (ms)**)  

When combined with `includeArgs` opt, threshold filtering becomes even more powerful:
you see only the slow methods along with the exact arguments that caused the delay—ideal for debugging performance issues tied to specific inputs.
```java
@TimerNinjaTracker(includeArgs = true, threshold = 200)
public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) {
    // Method logic
}
```

**Sample output:**
> public void requestMoneyTransfer(int sourceUserId, int targetUserId, int amount) - Args: [sourceUserId={1}, targetUserId={2}, amount={3000}] - 1037 ms ¤ [Threshold Exceed !!: 200 ms]

## Reading the time trace output
Once the method is executed, you should be able to find the result similar to this one in the output/log

```log
2023-04-06T21:27:50.878+07:00  INFO 14796 --- [nio-8080-exec-1] c.g.t.timerninja.TimerNinjaUtil          : Timer Ninja trace context id: c9ffeb39-3457-48d4-9b73-9ffe7d612165
2023-04-06T21:27:50.878+07:00  INFO 14796 --- [nio-8080-exec-1] c.g.t.timerninja.TimerNinjaUtil          : Trace timestamp: 2023-04-06T14:27:50.322Z
2023-04-06T21:27:50.878+07:00  INFO 14796 --- [nio-8080-exec-1] c.g.t.timerninja.TimerNinjaUtil          : {===== Start of trace context id: c9ffeb39-3457-48d4-9b73-9ffe7d612165 =====}
2023-04-06T21:27:50.878+07:00  INFO 14796 --- [nio-8080-exec-1] c.g.t.timerninja.TimerNinjaUtil          : public User getUserById(int userId) - 554 ms
2023-04-06T21:27:50.878+07:00  INFO 14796 --- [nio-8080-exec-1] c.g.t.timerninja.TimerNinjaUtil          :   |-- public User findUserById(int userId) - 251 ms
2023-04-06T21:27:50.878+07:00  INFO 14796 --- [nio-8080-exec-1] c.g.t.timerninja.TimerNinjaUtil          : {====== End of trace context id: c9ffeb39-3457-48d4-9b73-9ffe7d612165 ======}
```
In detail:  
`Timer Ninja trace context id`: The auto generated uuid of a trace context. A trace context is initiated for the very first method encountered with `@TimerNinjaTracker` annotation.
Any sequence execution of other annotated tracker methods inside the parent method will also be accounted for in the existing trace context.  
`Trace timestamp`: The timestamp when the trace context is initiated, in UTC timezone.  
`Begin-end of trace context`: The detailed execution time of each method. The `|--` sign indicate the call to this method originated from the above parent method, which help to visualize the execution stacktrace.

## Troubleshooting
If you need to troubleshoot, you can toggle the `DEBUG` log level on logger `io.github.thanglequoc.timerninja.TimerNinjaThreadContext`.

## Issue and contribution
Any contribution is warmly welcome. Please feel free to open an Issue if you have any problem setting up timer-ninja. Or open a Pull Request
if you have any improvement to this project.

## Example projects
Below are some example projects which has Timer Ninja integrated for your setup reference 

[Spring Boot - Ninja Coffee Shop - Gradle](https://github.com/thanglequoc/timer-ninja-examples/tree/main/ninja-coffee-shop-gradle)  
[Spring Boot - Ninja Coffee Shop - Maven](https://github.com/thanglequoc/timer-ninja-examples/tree/main/ninja-coffee-shop-maven)


----
###### 🦥 Project logo generated by [Google Gemini](https://gemini.google.com)
