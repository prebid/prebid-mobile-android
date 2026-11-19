package org.prebid.mobile.testutils;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.reflection.Reflection;

import java.util.ArrayList;
import java.util.List;

/**
 * Records the messages logged at or above a priority. {@link #install()} makes it the SDK logger
 * and logs every level; {@link #uninstall()} restores the previous logger and log level.
 */
public final class RecordingLogger implements LogUtil.PrebidLogger {

    private final int minPriority;
    private final List<String> messages = new ArrayList<>();

    private LogUtil.PrebidLogger previousLogger;
    private int previousLogLevel;

    public RecordingLogger(int minPriority) {
        this.minPriority = minPriority;
    }

    public RecordingLogger install() {
        previousLogger = Reflection.getStaticFieldOf(LogUtil.class, "logger");
        previousLogLevel = LogUtil.getLogLevel();
        LogUtil.setLogLevel(LogUtil.VERBOSE);
        LogUtil.setLogger(this);
        return this;
    }

    public void uninstall() {
        if (previousLogger != null) {
            LogUtil.setLogger(previousLogger);
        }
        LogUtil.setLogLevel(previousLogLevel);
    }

    @Override
    public synchronized void println(int messagePriority, String tag, String message) {
        if (messagePriority >= minPriority) {
            messages.add(message);
        }
    }

    @Override
    public synchronized void e(String tag, String message, Throwable throwable) {
        if (LogUtil.ERROR >= minPriority) {
            messages.add(message);
        }
    }

    public synchronized List<String> getMessages() {
        return new ArrayList<>(messages);
    }
}
