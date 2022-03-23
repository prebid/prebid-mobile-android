/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile;

import android.util.Log;
import androidx.annotation.Size;

public class LogUtil {
    private static final String BASE_TAG = "PrebidMobile";

    public static final int NONE = -1;
    public static final int VERBOSE = android.util.Log.VERBOSE; // 2
    public static final int DEBUG = android.util.Log.DEBUG; // 3
    public static final int INFO = android.util.Log.INFO; // 4
    public static final int WARN = android.util.Log.WARN; // 5
    public static final int ERROR = android.util.Log.ERROR; // 6
    public static final int ASSERT = android.util.Log.ASSERT; // 7

    private static int logLevel;

    private LogUtil() {
    }


    public static void setLogLevel(int level) {
        logLevel = level;
    }

    public static int getLogLevel() {
        return logLevel;
    }

    /**
     * Prints a message with VERBOSE priority and default BASE_TAG
     */
    public static void verbose(String message) {
        verbose(BASE_TAG, message);
    }

    /**
     * Prints a message with DEBUG priority and default BASE_TAG
     */
    public static void debug(String message) {
        debug(BASE_TAG, message);
    }

    /**
     * Prints a message with INFO priority and default BASE_TAG
     */
    public static void info(String message) {
        info(BASE_TAG, message);
    }

    /**
     * Prints a message with WARNING priority and default BASE_TAG
     */
    public static void warning(String message) {
        warning(BASE_TAG, message);
    }

    /**
     * Prints a message with ERROR priority and default BASE_TAG
     */
    public static void error(String message) {
        error(BASE_TAG, message);
    }

    /**
     * Prints a message with VERBOSE priority.
     */
    public static void verbose(@Size(max = 23) String tag, String msg) {
        print(VERBOSE, tag, msg);
    }

    /**
     * Prints a message with DEBUG priority.
     */
    public static void debug(@Size(max = 23) String tag, String msg) {
        print(DEBUG, tag, msg);
    }

    /**
     * Prints a message with INFO priority.
     */
    public static void info(@Size(max = 23) String tag, String msg) {
        print(INFO, tag, msg);
    }

    /**
     * Prints a message with WARN priority.
     */
    public static void warning(@Size(max = 23) String tag, String msg) {
        print(WARN, tag, msg);
    }

    /**
     * Prints a message with ERROR priority.
     */
    public static void error(@Size(max = 23) String tag, String msg) {
        print(ERROR, tag, msg);
    }

    /**
     * Prints a message with ASSERT priority.
     */
    public static void wtf(@Size(max = 23) String tag, String msg) {
        print(ASSERT, tag, msg);
    }

    /**
     * Prints a message with ERROR priority and exception.
     */
    public static void error(final String tag, String message, Throwable throwable) {
        if (tag == null || message == null) {
            return;
        }

        if (ERROR >= getLogLevel()) {
            Log.e(getTagWithBase(tag), message, throwable);
        }
    }

    /**
     * Prints information with set priority. Every tag
     */
    private static void print(int messagePriority, String tag, String message) {
        if (tag == null || message == null) {
            return;
        }

        if (messagePriority >= getLogLevel()) {
            Log.println(messagePriority, getTagWithBase(tag), message);
        }
    }

    /**
     * Helper method to add Prebid tag to logging messages.
     */
    private static String getTagWithBase(String tag) {
        StringBuilder result = new StringBuilder();

        String prefix = "Prebid";
        if (tag.startsWith(prefix)) {
            result.append(tag);
        } else {
            result.append(prefix).append(tag);
        }

        if (result.length() > 23) {
            return result.substring(0, 22);
        } else {
            return result.toString();
        }
    }

}
