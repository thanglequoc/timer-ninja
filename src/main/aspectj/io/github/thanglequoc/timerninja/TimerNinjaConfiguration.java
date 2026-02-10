package io.github.thanglequoc.timerninja;

/**
 * Singleton configuration class for Timer Ninja runtime settings.
 * <p>
 * This class manages global configuration options that control Timer Ninja's behavior,
 * particularly related to logging output configuration. It uses the thread-safe
 * Initialization-on-Demand Holder Idiom for lazy initialization.
 * </p>
 * <p>
 * Currently supported configuration options:
 * </p>
 * <ul>
 *   <li><b>System.out logging:</b> Toggle whether to print timing traces to System.out
 *       in addition to the default SLF4J logging. This is useful for applications
 *       that don't use a logging framework.</li>
 * </ul>
 * <p>
 * Example usage:
 * <pre>
 * // Enable System.out output
 * TimerNinjaConfiguration.getInstance().toggleSystemOutLog(true);
 *
 * // Check if System.out output is enabled
 * boolean isEnabled = TimerNinjaConfiguration.getInstance().isSystemOutLogEnabled();
 * </pre>
 */
public class TimerNinjaConfiguration {

    private static TimerNinjaConfiguration instance;

    private boolean enabledSystemOutLog;

    private TimerNinjaConfiguration() {
        enabledSystemOutLog = false;
    }

    /**
     * Returns the singleton instance of {@link TimerNinjaConfiguration}.
     * Thread-safe using the Initialization-on-Demand Holder Idiom.
     *
     * @return the singleton configuration instance
     */
    public static TimerNinjaConfiguration getInstance() {
        return Holder.INSTANCE;
    }

    /**
     * Inner static holder class for lazy initialization.
     * The JVM guarantees thread-safe initialization of static final fields.
     */
    private static class Holder {
        static final TimerNinjaConfiguration INSTANCE = new TimerNinjaConfiguration();
    }

    /**
     * By default, TimerNinja prints the result with Slf4 logging API.<br>
     * This option is for consumer that doesn't use any java logger provider.<br>
     * Toggles the option to print timing trace results to System.out print stream in addition to the default logging using Slf4j.
     *
     * @param enabledSystemOutLogging true to enable printing to System.out, false otherwise.
     * */
    public synchronized void toggleSystemOutLog(boolean enabledSystemOutLogging) {
        this.enabledSystemOutLog = enabledSystemOutLogging;
    }

    /**
     * Check if TimerNinja will also print the log trace to System.out in addition to the default logging using Slf4j
     * @return flag indicates if System.out output is enabled
     * */
    public boolean isSystemOutLogEnabled() {
        return enabledSystemOutLog;
    }
}
