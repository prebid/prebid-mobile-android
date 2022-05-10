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

/**
 * Base error. Maintaining error description.
 */
public class AdException extends Exception {
    public static final String INVALID_REQUEST = "Invalid request";

    public static final String INTERNAL_ERROR = "SDK internal error";
    public static final String INIT_ERROR = "Initialization failed";
    public static final String SERVER_ERROR = "Server error";
    public static final String THIRD_PARTY = "Third Party SDK";

    private String message;

    public void setMessage(String msg) {
        message = msg;
    }

    /**
     * Error description.
     *
     * @return description
     */
    @Override
    public String getMessage() {
        return message;
    }

    public AdException(String type, String message) {
        setMessage(type + ": " + message);
    }
}
