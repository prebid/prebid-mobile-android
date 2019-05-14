package org.prebid.mobile.drprebid.model;

public enum AdFormat {
    BANNER(1),
    INTERSTITIAL(2);

    private int code;

    AdFormat(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
