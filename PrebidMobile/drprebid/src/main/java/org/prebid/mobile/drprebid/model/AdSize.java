package org.prebid.mobile.drprebid.model;

public enum AdSize {
    BANNER_300x250(1),
    BANNER_300x600(2),
    BANNER_320x50(3),
    BANNER_320x100(4),
    BANNER_320x480(5),
    BANNER_728x90(6);

    private int code;

    AdSize(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
