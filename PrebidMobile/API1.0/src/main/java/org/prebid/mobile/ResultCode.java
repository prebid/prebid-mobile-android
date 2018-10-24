package org.prebid.mobile;

public enum ResultCode {
    /**
     * The attaching keywords was successful, which means
     * there was demand and the demand was set on the ad object.
     */
    SUCCESS,
    /**
     * The ad request failed due to sizes not passed for banner ad unit
     */
    NO_SIZE_FOR_BANNER,
    /**
     * The ad request failed due to empty account id
     */
    INVALID_ACCOUNT_ID,
    /**
     * The ad request failed due to empty config id on the ad unit
     */
    INVALID_CONFIG_ID,
    /**
     * The ad request failed because a CUSTOM host used without providing host url
     */
    INVALID_HOST_URL,
    /**
     * For MoPub banner view, we don't support multi-size request
     */
    INVALID_SIZE,
    /**
     * The ad request failed due to a network error.
     */
    NETWORK_ERROR,
    /**
     * The ad request took longer than set time out
     */
    TIME_OUT,
    /**
     * No bids available from demand source
     */
    NO_BIDS
}
