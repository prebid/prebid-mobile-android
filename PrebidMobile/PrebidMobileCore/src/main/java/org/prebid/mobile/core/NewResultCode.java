package org.prebid.mobile.core;

public enum NewResultCode {
    /**
     * The attaching keywords was successful, which means
     * there was demand and the demand was set on the ad object.
     */
    SUCCESS,
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
