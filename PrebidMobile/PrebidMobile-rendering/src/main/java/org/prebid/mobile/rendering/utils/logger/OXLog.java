/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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
package org.prebid.mobile.rendering.utils.logger;

import android.content.Context;
import android.util.Log;

/**
 * Helper class for a list (or tree) of LoggerNodes.
 * <p>
 * <p>
 * When this is set as the head of the list, an instance of it can function as a drop-in replacement for {@link android.util.Log}. Most of the methods in this class server only to map a method call in Log to its equivalent in LogNode.
 * </p>
 */
public class OXLog {
    private static final String TAG = OXLog.class.getSimpleName();

    // Grabbing the native values from Android's native logging facilities,
    // to make for easy migration and interop.
    private static final int NONE = -1;
    private static final int VERBOSE = android.util.Log.VERBOSE;//2
    private static final int DEBUG = android.util.Log.DEBUG;//3
    private static final int INFO = android.util.Log.INFO;//4
    private static final int WARN = android.util.Log.WARN;//5
    private static final int ERROR = android.util.Log.ERROR;//6
    private static final int ASSERT = android.util.Log.ASSERT;//7

    private static int sLogLevel;

    public static void setLogLevel(int level) {
        sLogLevel = level;
    }

    public static int getLogLevel() {
        return sLogLevel;
    }

    /**
     * Prints a message at VERBOSE priority.
     *  @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    private static void v(String tag, String msg) {
        print(VERBOSE, tag, msg);
    }

    /**
     * Prints a message at VERBOSE priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void verbose(String tag, String msg) {
        v(tag, msg);
    }

    /**
     * Prints a message at DEBUG priority.
     *  @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    private static void d(String tag, String msg) {
        print(DEBUG, tag, msg);
    }

    /**
     * Prints a message at DEBUG priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void debug(String tag, String msg) {
        d(tag, msg);
    }

    /**
     * Prints a message at INFO priority.
     *  @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    private static void i(String tag, String msg) {
        print(INFO, tag, msg);
    }

    /**
     * Prints a message at INFO priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void info(String tag, String msg) {
        i(tag, msg);
    }

    /**
     * Prints a message at WARN priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    private static void w(String tag, String msg) {
        print(WARN, tag, msg);
    }

    /**
     * Prints a message at WARN priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void warn(String tag, String msg) {
        w(tag, msg);
    }

    /**
     * Prints a message at ERROR priority.
     *  @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    private static void e(String tag, String msg) {
        print(ERROR, tag, msg);
    }

    /**
     * Prints a message at ERROR priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void error(String tag, String msg) {
        e(tag, msg);
    }

    /**
     * Prints a message at ASSERT priority.
     *  @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    private static void wtf(String tag, String msg) {
        print(ASSERT, tag, msg);
    }

    /**
     * Prints a message at ASSERT priority.
     *
     * @param tag Tag for for the log data. Can be used to organize log statements.
     * @param msg The actual message to be logged.
     */
    public static void assertLog(String tag, String msg) {
        wtf(tag, msg);
    }

    /**
     * Will no longer be supported. Currently is calling the {@link #error(String, String)}
     *
     * @param context Application/Activity context. NOT USED ANYMORE
     * @param tag     Name of the class sending a message
     * @param msg     Message to log
     */
    @Deprecated
    public static void phoneHome(Context context, String tag, String msg) {
        error(tag, msg);
    }

    private static void print(int messagePriority, String tag, String msg) {
        // There actually are log methods that don't take a msg parameter.  For now,
        // if that's the case, just convert null to the empty string and move on.
        String useMsg = msg;
        if (useMsg == null) {
            useMsg = "";
        }

        if (messagePriority >= getLogLevel()) {
            // This is functionally identical to Log.x(tag, useMsg);
            // For instance, if priority were Log.VERBOSE, this would be the same as Log.v(tag, useMsg)
            Log.println(messagePriority, tag, useMsg);
        }
    }
}
