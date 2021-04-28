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

package org.prebid.mobile.rendering.networking.parameters;

/**
 * Container class for advertisement call parameters.
 */
public class UserParameters {
    public static final String TAG = UserParameters.class.getSimpleName();

    public static final String GENDER_MALE = "M";
    public static final String GENDER_FEMALE = "F";
    public static final String GENDER_OTHER = "O";

    private UserParameters() {

    }

    /**
     * User gender.
     */
    public enum Gender {
        /**
         * User is male.
         */
        MALE,

        /**
         * User is female.
         */
        FEMALE,

        /**
         * Other.
         */
        OTHER
    }

    public static String getGenderDescription(Gender gender) {
        String desc = null;
        switch (gender) {
            case MALE:
                desc = GENDER_MALE;
                break;
            case FEMALE:
                desc = GENDER_FEMALE;
                break;
            case OTHER:
                desc = GENDER_OTHER;
                break;
        }
        return desc;
    }

    /**
     * Device connection type.
     */
    public enum ConnectionType {
        /**
         * Device is off-line.
         */
        OFFLINE,

        /**
         * Device connected via WiFi.
         */
        WIFI,

        /**
         * Device connected via mobile technology, such as 3G, GPRS, CDMA etc.
         */
        CELL
    }
}