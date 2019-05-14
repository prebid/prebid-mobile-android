package org.prebid.mobile.drprebid.model;

public enum PrebidServer {
    RUBICON(1),
    APPNEXUS(2),
    CUSTOM(3);

    private int code;

    PrebidServer(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
