package org.prebid.mobile.drprebid.model;

import org.prebid.mobile.drprebid.Constants;

public enum AdFormat {
    BANNER(Constants.Settings.AdFormatCodes.BANNER),
    INTERSTITIAL(Constants.Settings.AdFormatCodes.INTERSTITIAL);

    private int code;

    AdFormat(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
