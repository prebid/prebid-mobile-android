package org.prebid.mobile.eventhandlers.global;

public class Constants {
    /**
     * For every winning bid, a GAM SDK gives callback with below key via AppEventListener (from
     * GAM SDK). This key can be changed at GAM's line item.
     */
    public static final String APP_EVENT = "PrebidAppEvent";

    public static final int ERROR_CODE_INTERNAL_ERROR = 0;
    public static final int ERROR_CODE_INVALID_REQUEST = 1;
    public static final int ERROR_CODE_NETWORK_ERROR = 2;
    public static final int ERROR_CODE_NO_FILL = 3;
}
