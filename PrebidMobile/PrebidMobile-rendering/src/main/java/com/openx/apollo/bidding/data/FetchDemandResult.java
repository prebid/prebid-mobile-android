package com.openx.apollo.bidding.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
     * GAM and MoPub views supported only
     */
    INVALID_AD_OBJECT,
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

    public static FetchDemandResult parseErrorMessage(String msg) {
        Pattern storedRequestNotFound = Pattern.compile("^Invalid request: Stored Request with ID=\".*\" not found.");
        Pattern storedImpNotFound = Pattern.compile("^Invalid request: Stored Imp with ID=\".*\" not found.");
        Pattern invalidBannerSize = Pattern.compile("^Invalid request: Request imp\\[\\d\\].banner.format\\[\\d\\] must define non-zero \"h\" and \"w\" properties.");
        Pattern invalidInterstitialSize = Pattern.compile("Invalid request: Unable to set interstitial size list");
        Matcher requestMatcher = storedRequestNotFound.matcher(msg);
        Matcher bannerSizeMatcher = invalidBannerSize.matcher(msg);
        Matcher interstitialSizeMatcher = invalidInterstitialSize.matcher(msg);
        Matcher impMatcher = storedImpNotFound.matcher(msg);

        if (msg.contains("No bids")) {
            return NO_BIDS;
        }
        if (msg.contains("Timeout")) {
            return TIMEOUT;
        }
        if (msg.contains("Network Error")) {
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
