package org.prebid.mobile.drprebid.model;

import org.prebid.mobile.drprebid.Constants;

public enum AdServer {
    GOOGLE_AD_MANAGER(Constants.Settings.AdServerCodes.GOOGLE_AD_MANAGER),
    MOPUB(Constants.Settings.AdServerCodes.MOPUB);

    private int code;

    AdServer(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
