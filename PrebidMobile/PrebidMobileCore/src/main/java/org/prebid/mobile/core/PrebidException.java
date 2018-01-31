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

public class PrebidException extends Exception {

    public enum PrebidError {
        NULL_CONTEXT("Null context passed in."),
        EMPTY_ADUNITS("Empty AdUnits passed in."),
        NULL_HOST("Null host passed in."),
        BANNER_AD_UNIT_NO_SIZE("BannerAdUnit requires size information to check the price for the impression."),
        UNABLE_TO_INITIALIZE_DEMAND_SOURCE("Unable to instantiating the adapter."),
        INVALID_ACCOUNT_ID("Invalid input of account id.");

        private String error;

        PrebidError(String error) {
            this.error = error;
        }

        String getDetailMessage() {
            return this.error;
        }
    }

    public PrebidException(PrebidError error) {
        super(error.getDetailMessage());
    }
}
