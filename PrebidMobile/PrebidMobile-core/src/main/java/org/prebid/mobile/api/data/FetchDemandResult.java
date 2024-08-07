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

package org.prebid.mobile.api.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Enum fetch demand status for all ads.
 */
public enum FetchDemandResult {

    /**
     * The attaching keywords was successful, which means
     * there was demand and the demand was set on the ad object.
     */
    SUCCESS,
    /**
     * The ad request failed due to empty account id
     */
    INVALID_ACCOUNT_ID,
    /**
     * The ad request failed due to empty config id on the ad unit
     */
    INVALID_CONFIG_ID,
    /**
     * Size is invalid or missing
     */
    INVALID_SIZE,
    /**
     * Invalid context passed
     */
    INVALID_CONTEXT,
    /**
     * GAM views supported only
     */
    INVALID_AD_OBJECT,
    /**
     * The ad request failed because a CUSTOM host used without providing host url
     */
    INVALID_HOST_URL,
    /**
     * The ad request failed due to a network error.
     */
    NETWORK_ERROR,
    /**
     * The ad request took longer than set time out
     */
    TIMEOUT,
    /**
     * No bids available from demand source
     */
    NO_BIDS,
    /**
     * Server responded with some error messages
     */
    SERVER_ERROR;

    public static final String NO_BIDS_MESSAGE = "Failed to parse bids. No winning bids were found.";

    public static FetchDemandResult parseErrorMessage(String msg) {
        Pattern storedRequestNotFound = Pattern.compile("^Invalid request: Stored Request with ID=\".*\" not found.");
        Pattern storedImpNotFound = Pattern.compile("^Invalid request: Stored Imp with ID=\".*\" not found.");
        Pattern invalidBannerSize = Pattern.compile("^Invalid request: Request imp\\[\\d\\].banner.format\\[\\d\\] must define non-zero \"h\" and \"w\" properties.");
        Pattern invalidInterstitialSize = Pattern.compile("Invalid request: Unable to set interstitial size list");
        Matcher requestMatcher = storedRequestNotFound.matcher(msg);
        Matcher bannerSizeMatcher = invalidBannerSize.matcher(msg);
        Matcher interstitialSizeMatcher = invalidInterstitialSize.matcher(msg);
        Matcher impMatcher = storedImpNotFound.matcher(msg);

        if (msg.contains("No bids") || msg.equals(NO_BIDS_MESSAGE)) {
            return NO_BIDS;
        }
        if (msg.contains("Timeout")) {
            return TIMEOUT;
        }
        if (msg.contains("Network Error") || msg.contains("No internet")) {
            return NETWORK_ERROR;
        }
        if (requestMatcher.find() || msg.contains("No stored request")) {
            return INVALID_ACCOUNT_ID;
        }
        if (impMatcher.find() || msg.contains("Stored Imp with ID")) {
            return INVALID_CONFIG_ID;
        }
        if (bannerSizeMatcher.find()
            || interstitialSizeMatcher.find()
            || msg.contains("Request imp[0].banner.format")) {
            return INVALID_SIZE;
        }

        return SERVER_ERROR;
    }
}
