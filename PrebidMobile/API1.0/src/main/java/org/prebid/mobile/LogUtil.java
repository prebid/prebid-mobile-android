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
    //region Private Constructor
    private LogUtil() {
    }
    //endregion

    //region Log Tag
    private static final String BASE_TAG = "PrebidMobile";

    /**
     * Helper method to get Prebid log tag that is shorter than 23 characters
     *
     * @param tagSuffix specific tag description
     * @return log tag with "Prebid" prefix
     */
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
    //endregion

    //region Verbose Log
    public static void v(String message) {
        v(BASE_TAG, message);
    }

    public static void v(final String tag, String message) {
        v(tag, message, null);
    }

    public static void v(final String tag, String message, Throwable cause) {
        if (TextUtils.isEmpty(tag)) {
            if (Log.isLoggable(BASE_TAG, Log.VERBOSE)) {
                Log.v(BASE_TAG, message, cause);
            }
        } else {
            if (Log.isLoggable(tag, Log.VERBOSE)) {
                Log.v(tag, message, cause);
            }
        }
    }
    //endregion

    //region Debug Log
    public static void d(String message) {
        d(BASE_TAG, message);
    }

    public static void d(final String tag, String message) {
        d(tag, message, null);
    }

    public static void d(final String tag, String message, Throwable cause) {
        try {
            if (Log.isLoggable(tag, Log.DEBUG)) {
                Log.d(tag, message, cause);
                return;
            }
        } catch (IllegalArgumentException e) {
            Log.e(BASE_TAG, "Tried to log a message with tag length >23: " + tag);
        }
        Log.d(BASE_TAG, message, cause);
    }
    //endregion

    //region Info Log

    public static void i(String message) {
        i(BASE_TAG, message);
    }

    public static void i(final String tag, String message) {
        i(tag, message, null);
    }

    public static void i(final String tag, String message, Throwable cause) {
        try {
            if (Log.isLoggable(tag, Log.INFO)) {
                Log.i(tag, message, cause);
                return;
            }
        } catch (IllegalArgumentException e) {
            Log.e(BASE_TAG, "Tried to log a message with tag length >23: " + tag);
        }
        Log.i(BASE_TAG, message, cause);
    }
    //endregion

    //region Warning Log

    public static void w(String message) {
        w(BASE_TAG, message);
    }

    public static void w(final String tag, String message) {
        w(tag, message, null);
    }

    public static void w(final String tag, String message, Throwable cause) {
        try {
            if (Log.isLoggable(tag, Log.WARN)) {
                Log.w(tag, message, cause);
                return;
            }
        } catch (IllegalArgumentException e) {
            Log.e(BASE_TAG, "Tried to log a message with tag length >23: " + tag);
        }
        Log.w(BASE_TAG, message, cause);
    }
    //endregion

    //region Error Log

    public static void e(String message) {
        e(BASE_TAG, message);
    }

    public static void e(final String tag, String message) {
        e(tag, message, null);
    }

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
    //endregion
}
