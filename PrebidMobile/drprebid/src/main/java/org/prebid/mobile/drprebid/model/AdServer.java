package org.prebid.mobile.drprebid.model;

public enum AdServer {
    GOOGLE_AD_MANAGER(1),
    MOPUB(2);

    private int code;

    AdServer(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
