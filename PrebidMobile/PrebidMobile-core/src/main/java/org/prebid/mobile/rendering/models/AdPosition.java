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

package org.prebid.mobile.rendering.models;

/**
 * The ad position for additional targeting.
 */
public enum AdPosition {

    UNDEFINED(-1),
    UNKNOWN(0),
    ABOVE_THE_FOLD(1),
    LOCKED(2),
    BELOW_THE_FOLD(3),
    HEADER(4),
    FOOTER(5),
    SIDEBAR(6),
    FULLSCREEN(7);

    private final int value;

    AdPosition(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

}
