package io.github.thanglequoc.timerninja;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The singleton to store TimerNinja configuration
 */
public class TimerNinjaConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(TimerNinjaConfiguration.class);

    private static TimerNinjaConfiguration instance;

    private boolean enabledSystemOutLog;

    // Statistics configuration
    private boolean statisticsReportingEnabled = false;
    private int statisticsBufferSize = 1000;
    private boolean useFullMethodSignature = false;

    private TimerNinjaConfiguration() {
        enabledSystemOutLog = false;
    }

    /**
     * Returns the singleton instance of {@link TimerNinjaConfiguration}.
     *
     * @return the singleton configuration instance
     */
    public static TimerNinjaConfiguration getInstance() {
        if (instance == null) {
            instance = new TimerNinjaConfiguration();
        }
        return instance;
    }

    /**
     * By default, TimerNinja prints the result with Slf4 logging API.<br>
     * This option is for consumer that doesn't use any java logger provider.<br>
     * Toggles the option to print timing trace results to System.out print stream
     * in addition to the default logging using Slf4j.
     *
     * @param enabledSystemOutLogging true to enable printing to System.out, false
     *                                otherwise.
     */
    public synchronized void toggleSystemOutLog(boolean enabledSystemOutLogging) {
        this.enabledSystemOutLog = enabledSystemOutLogging;
    }

    /**
     * Check if TimerNinja will also print the log trace to System.out in addition
     * to the default logging using Slf4j
     * 
     * @return flag indicates if System.out output is enabled
     */
    public boolean isSystemOutLogEnabled() {
        return enabledSystemOutLog;
    }

    // ==================== Statistics Configuration ====================

    /**
     * Enable or disable statistics collection.
     * When enabled, execution times are recorded for later analysis.
     *
     * @param enabled true to enable statistics collection
     */
    public synchronized void enableStatisticsReporting(boolean enabled) {
        this.statisticsReportingEnabled = enabled;
        if (enabled) {
            LOGGER.info("Timer Ninja statistics reporting enabled");
        }
    }

    /**
     * Check if statistics reporting is enabled.
     *
     * @return true if statistics reporting is enabled
     */
    public boolean isStatisticsReportingEnabled() {
        return statisticsReportingEnabled;
    }

    /**
     * Set the maximum number of execution time samples to keep per method.
     * Older samples are discarded when the buffer is full (FIFO eviction).
     *
     * @param size maximum buffer size (default: 1000)
     */
    public synchronized void setStatisticsBufferSize(int size) {
        if (size < 1) {
            throw new IllegalArgumentException("Buffer size must be at least 1");
        }
        this.statisticsBufferSize = size;
        StatisticsCollector.getInstance().setMaxBufferSize(size);
    }

    /**
     * Get the maximum buffer size per method.
     *
     * @return the buffer size
     */
    public int getStatisticsBufferSize() {
        return statisticsBufferSize;
    }

    /**
     * Set whether to use full method signatures in reports.
     * Default is false (shortened signatures like "methodName(Param1, Param2)").
     *
     * @param useFull true to use full signatures, false for shortened
     */
    public synchronized void setUseFullMethodSignature(boolean useFull) {
        this.useFullMethodSignature = useFull;
    }

    /**
     * Check if full method signatures are used in reports.
     *
     * @return true if using full signatures
     */
    public boolean isUseFullMethodSignature() {
        return useFullMethodSignature;
    }

    /**
     * Get the StatisticsCollector instance.
     *
     * @return the statistics collector
     */
    public StatisticsCollector getStatisticsCollector() {
        return StatisticsCollector.getInstance();
    }

    /**
     * Print statistics report to the configured logger.
     */
    public void printStatisticsReport() {
        String report = getStatisticsReportAsString("text");
        LOGGER.info("\n{}", report);
        if (enabledSystemOutLog) {
            System.out.println(report);
        }
    }

    /**
     * Get statistics report as a formatted string.
     *
     * @param format output format: "text", "json", or "html"
     * @return formatted report string
     */
    public String getStatisticsReportAsString(String format) {
        return StatisticsReportGenerator.generateReport(
                StatisticsCollector.getInstance().getAllStatistics(), format);
    }

    /**
     * Get statistics report as bytes (for REST download).
     *
     * @param format output format: "text", "json", or "html"
     * @return report as byte array
     */
    public byte[] getStatisticsReportAsBytes(String format) {
        return StatisticsReportGenerator.generateReportAsBytes(
                StatisticsCollector.getInstance().getAllStatistics(), format);
    }

    /**
     * Reset all collected statistics.
     */
    public void resetStatistics() {
        StatisticsCollector.getInstance().reset();
        LOGGER.info("Timer Ninja statistics have been reset");
    }
}
