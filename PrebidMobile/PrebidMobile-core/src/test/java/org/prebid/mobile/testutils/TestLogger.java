package org.prebid.mobile.testutils;

import static org.junit.Assert.assertNotNull;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.LogUtil;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;

import kotlin.jvm.internal.Intrinsics;


public final class TestLogger implements LogUtil.PrebidLogger {

    private final List<PrintLog> printLogs = new ArrayList<>();
    private final List<ErrorLog> errorLogs = new ArrayList<>();

    public void println(int messagePriority, @NotNull String tag, @NotNull String message) {
        printLogs.add(new PrintLog(messagePriority, tag, message));
    }

    public void e(@NotNull String tag, @NotNull String message, @NotNull Throwable throwable) {
        errorLogs.add(new ErrorLog(tag, message, throwable));
    }

    public void assertErrorLogged(@NotNull String tag, @NotNull String message, @NotNull Throwable throwable) {
        ErrorLog expectedLog = new ErrorLog(tag, message, throwable);
        Iterator<ErrorLog> iterator = errorLogs.iterator();

        ErrorLog actualLog = null;
        while (iterator.hasNext()) {
            actualLog = iterator.next();
            if (!Intrinsics.areEqual(expectedLog, actualLog)) {
                continue;
            }
            break;
        }

        assertNotNull("Expected error log not captured. Recorded logs: " + this.errorLogs, actualLog);
    }

    public void assertLogPrinted(int messagePriority, @NotNull String tag, @NotNull String message) {
        PrintLog expectedLog = new PrintLog(messagePriority, tag, message);
        Iterator<PrintLog> iterator = printLogs.iterator();

        PrintLog actualLog = null;
        while (iterator.hasNext()) {
            actualLog = iterator.next();
            if (!Intrinsics.areEqual(expectedLog, actualLog)) {
                continue;
            }
            break;
        }

        assertNotNull("Expected log not captured. Recorded logs: " + this.printLogs, actualLog);
    }

    private static final class ErrorLog {
        @NotNull
        private final String tag;
        @NotNull
        private final String message;
        @NotNull
        private final Throwable throwable;

        public ErrorLog(@NotNull String tag, @NotNull String message, @NotNull Throwable throwable) {
            this.tag = tag;
            this.message = message;
            this.throwable = throwable;
        }

        @NotNull
        public String toString() {
            return "ErrorLog(tag=" + this.tag + ", message=" + this.message + ", throwable=" + this.throwable + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ErrorLog errorLog = (ErrorLog) o;
            return tag.equals(errorLog.tag) && message.equals(errorLog.message) && throwable.equals(errorLog.throwable);
        }

        @Override
        public int hashCode() {
            return Objects.hash(tag, message, throwable);
        }
    }

    private static final class PrintLog {
        private final int messagePriority;
        @NotNull
        private final String tag;
        @NotNull
        private final String message;


        public PrintLog(int messagePriority, @NotNull String tag, @NotNull String message) {
            this.messagePriority = messagePriority;
            this.tag = tag;
            this.message = message;
        }

        @NotNull
        public String toString() {
            return "PrintLog(messagePriority=" + this.messagePriority + ", tag=" + this.tag + ", message=" + this.message + ")";
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PrintLog printLog = (PrintLog) o;
            return messagePriority == printLog.messagePriority && tag.equals(printLog.tag) && message.equals(printLog.message);
        }

        @Override
        public int hashCode() {
            return Objects.hash(messagePriority, tag, message);
        }
    }
}
