package org.prebid.mobile.drprebid.model;

import org.prebid.mobile.drprebid.Constants;

public enum PrebidServer {
    RUBICON(Constants.Settings.PrebidServerCodes.RUBICON),
    APPNEXUS(Constants.Settings.PrebidServerCodes.APPNEXUS),
    CUSTOM(Constants.Settings.PrebidServerCodes.CUSTOM);

    private int code;

    PrebidServer(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
