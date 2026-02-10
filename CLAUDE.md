# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Timer Ninja is a lightweight Java library that measures method execution time using Aspect-Oriented Programming (AOP) with AspectJ. It provides annotation-based tracking (`@TimerNinjaTracker`) and programmatic tracking (`TimerNinjaBlock.measure()`) with automatic call hierarchy preservation and visual tree output.

## Build System

This project uses Gradle with the **io.freefair.aspectj** plugin (v9.1.0) for AspectJ post-compile weaving.

### Common Commands

```bash
# Build the entire project
./gradlew build

# Run all tests
./gradlew test

# Run a specific test class
./gradlew test --tests TimerNinjaIntegrationTest

# Run a specific test method
./gradlew test --tests TimerNinjaIntegrationTest.testSingleTracker

# Build and publish to local Maven repository
./gradlew publishToMavenLocal

# Clean build artifacts
./gradlew clean
```

### Publishing to Maven Central

```bash
# Build and stage for release (requires signing credentials)
./gradlew build publish

# Deploy staged artifacts to Maven Central via JReleaser
./gradlew jreleaserDeploy
```

Note: JReleaser configuration in build.gradle:79-121 handles signing and deployment. Credentials are currently placeholder values.

## Architecture

### Core Components

**AspectJ Layer** (`TimeTrackingAspect.aj`)
- Native AspectJ syntax (not @AspectJ annotation style)
- Two pointcuts: methods and constructors annotated with `@TimerNinjaTracker`
- Uses around advice to intercept execution, measure time, and manage tracking context
- Leverages `thisJoinPoint` and `thisJoinPointStaticPart` for reflection
- Located in `src/main/aspectj` (custom source set, NOT `src/main/java`)

**Context Management** (`TimerNinjaContextManager`)
- Centralized ThreadLocal management for per-thread tracking state
- Replaces direct ThreadLocal access in newer code
- Both annotation-based and block-based tracking share the same context

**Thread Context** (`TimerNinjaThreadContext`)
- Per-thread tracking state stored in ThreadLocal
- Tracks: `traceContextId` (UUID), `pointerDepth` (call stack depth), `itemContextMap` (LinkedHashMap preserving execution order)
- Context lifecycle: created on first tracked method, cleaned up when depth returns to 0

**Tracker Data** (`TrackerItemContext`)
- Immutable data holder for each tracked method/block
- Stores: method signature, arguments, execution time, time unit, threshold

**Utilities** (`TimerNinjaUtil`)
- Static utility methods for signature formatting, argument extraction, time conversion, tree formatting
- SLF4J logger (`INFO` level default, DEBUG on `TimerNinjaThreadContext` for troubleshooting)

### Tracking Mechanisms

**Annotation-Based Tracking**
```java
@TimerNinjaTracker(timeUnit = ChronoUnit.MILLIS, includeArgs = true, threshold = 200)
public void myMethod() { }
```

**Programmatic Block Tracking** (NEW - in development)
```java
TimerNinjaBlock.measure(() -> {
    // Code to measure
}, BlockTrackerConfig.builder().timeUnit(ChronoUnit.MILLIS).build());
```

Both mechanisms integrate seamlessly through the shared `TimerNinjaContextManager`.

### Execution Flow

1. Aspect intercepts method call OR `TimerNinjaBlock.measure()` is invoked
2. Check if ThreadLocal context exists; create if null
3. Create `TrackerItemContext`, increment `pointerDepth`, start timer
4. Execute actual method/block via `proceed()` (AspectJ) or `Runnable.run()` (block)
5. Calculate duration, update context, decrement `pointerDepth`
6. When `pointerDepth` returns to 0: log full trace tree, cleanup ThreadLocal

### Key Design Patterns

- **ThreadLocal Pattern**: Ensures thread-safe context isolation
- **LinkedHashMap**: Preserves insertion order for trace visualization
- **Pointer Depth Tracking**: Simple integer counter for tree indentation
- **Builder Pattern**: `BlockTrackerConfig` for fluent block configuration

## Testing

### Test Structure

- **Integration Tests**: `TimerNinjaIntegrationTest` - full tracking scenarios with nested calls
- **Unit Tests**: Individual component tests (Utils, Context, ThreadContext)
- **Test Fixtures**: `servicesample/` package with realistic domain scenarios (BankService, UserService, etc.)
- **Custom JUnit Extension**: `LogCaptureExtension` captures SLF4J output via Logback `ListAppender`

### Running Tests

Tests use JUnit 5 with Mockito for mocking. Log capture is done via `@ExtendWith(LogCaptureExtension.class)`.

```bash
# Run all tests
./gradlew test

# Run specific test
./gradlew test --tests TimerNinjaUtilTest.testGetExecutionTimeString
```

Note: Tests require `testAspect` configuration for AspectJ weaving in test classes.

## AspectJ-Specific Details

### Source Sets
- Main code: `src/main/aspectj/` (NOT `src/main/java/`)
- Test code: `src/test/aspectj/`

### Weaving Requirements
Consumers of this library MUST add an AspectJ plugin to their build:

**Gradle**:
```groovy
plugins {
    id "io.freefair.aspectj.post-compile-weaving" version "9.1.0"
}
dependencies {
    aspect 'io.github.thanglequoc:timer-ninja:1.2.0'
    testAspect("io.github.thanglequoc:timer-ninja:1.2.0") // for test weaving
}
```

**Maven**:
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
</plugin>
```

### Pointcut Expressions

- Methods: `execution(@TimerNinjaTracker * *(..))`
- Constructors: `execution(@TimerNinjaTracker *.new(..))`

### JoinPoint Reflection

The aspect uses:
- `MethodSignature` / `ConstructorSignature` for signature introspection
- `CodeSignature.getParameterNames()` for parameter name extraction
- `JoinPoint.getArgs()` for argument values
- `Signature.getAnnotation()` for reading annotation options

## Current Development

**Branch: `26_InlineCodeBlockTracking`**

New features in development:
- `TimerNinjaBlock` - Programmatic code block tracking API
- `BlockTrackerConfig` - Builder-style configuration for blocks
- `TimerNinjaContextManager` - Refactored centralized ThreadLocal management
- Integration of annotation and block tracking in shared context

## Important Notes

- Java 17 target/source compatibility
- SLF4J for logging (users need Logback/Log4J implementation)
- Optional System.out fallback via `TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true)`
- Apache License 2.0
- LinkedHashMap preserves execution order for trace visualization
- ThreadLocal cleanup is critical (happens when depth returns to 0)
