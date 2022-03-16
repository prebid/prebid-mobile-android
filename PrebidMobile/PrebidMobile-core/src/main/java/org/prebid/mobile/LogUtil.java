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

import android.text.TextUtils;
import android.util.Log;

public class LogUtil {
    private static final String BASE_TAG = "PrebidMobile";

    private static final int NONE = -1;
    private static final int VERBOSE = android.util.Log.VERBOSE; //2
    private static final int DEBUG = android.util.Log.DEBUG; //3
    private static final int INFO = android.util.Log.INFO; //4
    private static final int WARN = android.util.Log.WARN; //5
    private static final int ERROR = android.util.Log.ERROR; //6
    private static final int ASSERT = android.util.Log.ASSERT; //7

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
     * Prints a message with VERBOSE priority.
     */
    public static void verbose(String tag, String msg) {
        v(tag, msg);
    }

    /**
     * Prints a message with DEBUG priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void debug(String tag, String msg) {
        d(tag, msg);
    }

    /**
     * Prints a message with INFO priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void info(String tag, String msg) {
        i(tag, msg);
    }

    /**
     * Prints a message with WARN priority.
     */
    public static void warn(String tag, String msg) {
        w(tag, msg);
    }

    /**
     * Prints a message with ERROR priority.
     */
    public static void error(String tag, String msg) {
        e(tag, msg);
    }

    /**
     * Prints a message with ASSERT priority.
     */
    public static void assertLog(String tag, String msg) {
        wtf(tag, msg);
    }


    /**
     * Prints a message with VERBOSE priority and default BASE_TAG
     */
    public static void v(String message) {
        v(BASE_TAG, message);
    }

    /**
     * Prints a message with DEBUG priority and default BASE_TAG
     */
    public static void d(String message) {
        d(BASE_TAG, message);
    }

    /**
     * Prints a message with INFO priority and default BASE_TAG
     */
    public static void i(String message) {
        i(BASE_TAG, message);
    }

    /**
     * Prints a message with WARNING priority and default BASE_TAG
     */
    public static void w(String message) {
        w(BASE_TAG, message);
    }

    /**
     * Prints a message with ERROR priority and default BASE_TAG
     */
    public static void e(String message) {
        e(BASE_TAG, message);
    }

    /**
     * Prints a message with VERBOSE priority.
     */
    public static void v(String tag, String msg) {
        print(VERBOSE, tag, msg);
    }

    /**
     * Prints a message with DEBUG priority.
     */
    public static void d(String tag, String msg) {
        print(DEBUG, tag, msg);
    }

    /**
     * Prints a message with INFO priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void i(String tag, String msg) {
        print(INFO, tag, msg);
    }

    /**
     * Prints a message with WARN priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void w(String tag, String msg) {
        print(WARN, tag, msg);
    }

    /**
     * Prints a message with ERROR priority.
     */
    public static void e(String tag, String msg) {
        print(ERROR, tag, msg);
    }

    /**
     * Prints a message with ASSERT priority.
     */
    public static void wtf(String tag, String msg) {
        print(ASSERT, tag, msg);
    }

    /**
     * Prints a message with ERROR priority and exception.
     */
    public static void e(final String tag, String message, Throwable cause) {
        try {
            if (Log.isLoggable(tag, Log.ERROR)) {
                Log.e(tag, message, cause);
                return;
            }
        } catch (IllegalArgumentException e) {
            Log.e(BASE_TAG, "Tried to log a message with tag length >23: " + tag);
        }
        Log.e(BASE_TAG, message, cause);
    }

    /**
     * Prints information with set priority.
     */
    private static void print(int messagePriority, String tag, String msg) {
        String useMsg = msg;
        if (useMsg == null) {
            useMsg = "";
        }

        if (messagePriority >= getLogLevel()) {
            Log.println(messagePriority, tag, useMsg);
        }
    }

    /**
     * Helper method to get Prebid log tag that is shorter than 23 characters
     */
    @Deprecated
    public static String getTagWithBase(String tagSuffix) {
        StringBuilder sb = new StringBuilder().append(BASE_TAG);
        if (!TextUtils.isEmpty(tagSuffix)) {
            sb.append("-").append(tagSuffix);
        }
        if (sb.length() > 23) {
            return sb.substring(0, 22); // guarantee that tag length <=23
        } else {
            return sb.toString();
        }
    }

}
