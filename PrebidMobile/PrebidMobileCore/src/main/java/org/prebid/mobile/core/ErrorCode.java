/*
 *    Copyright 2016 Prebid.org, Inc.
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
package org.prebid.mobile.core;

public enum ErrorCode {
    /**
     * The ad request failed due to an invalid configuration (for example, size
     * or placement ID not set).
     */
    INVALID_REQUEST,
    /**
     * The ad request failed due to a network error.
     */
    NETWORK_ERROR,
    /**
     * An internal error is detected in the interacting with the
     * third-party SDK.
     */
    INTERNAL_ERROR,
    /**
     * No bids available from demand source
     */
    NO_BIDS
}
