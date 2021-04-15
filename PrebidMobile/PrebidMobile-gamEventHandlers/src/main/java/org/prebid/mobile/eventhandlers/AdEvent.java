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

package org.prebid.mobile.eventhandlers;

public enum AdEvent {
    APP_EVENT_RECEIVED,
    LOADED,
    CLOSED,
    CLICKED,
    DISPLAYED,
    REWARD_EARNED,
    FAILED;

    private int errorCode = -1;

    void setErrorCode(int errorCode) {
        this.errorCode = errorCode;
    }

    int getErrorCode() {
        return errorCode;
    }
}
