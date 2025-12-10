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

package org.prebid.mobile.api.exceptions;

import androidx.annotation.NonNull;

/**
 * Base error. Maintaining error description.
 */
public class AdException extends Exception {

    public static final String INIT_ERROR = "Initialization failed";
    public static final String FAILED_TO_LOAD_BIDS = "Failed to load bids";
    public static final String FAILED_TO_PARSE_RESPONSE = "Failed to parse response";
    public static final String NO_BIDS = "No bids";
    public static final String SERVER_ERROR = "Server error";
    public static final String THIRD_PARTY = "Third Party SDK";
    public static final String INTERNAL_ERROR = "SDK internal error";

    private String msg;


    public AdException(String type, String message) {
        msg = type + ": " + message;
    }

    /**
     * Root error message.
     */
    @Override
    public String getMessage() {
        return msg;
    }

    @NonNull
    @Override
    public String toString() {
        return "PrebidException: " + msg;
    }
}
