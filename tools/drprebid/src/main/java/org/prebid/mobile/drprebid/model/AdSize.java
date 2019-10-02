package org.prebid.mobile.drprebid.model;

import org.prebid.mobile.drprebid.Constants;

public enum AdSize {
    BANNER_300x250(Constants.Settings.AdSizeCodes.SIZE_300x250, 300, 250),
    BANNER_300x600(Constants.Settings.AdSizeCodes.SIZE_300x600, 300, 600),
    BANNER_320x50(Constants.Settings.AdSizeCodes.SIZE_320x50, 320, 50),
    BANNER_320x100(Constants.Settings.AdSizeCodes.SIZE_320x100, 320, 100),
    BANNER_320x480(Constants.Settings.AdSizeCodes.SIZE_320x480, 320, 480),
    BANNER_728x90(Constants.Settings.AdSizeCodes.SIZE_728x90, 728, 90);

    private int code;
    private int width;
    private int height;

    AdSize(int code, int width, int height) {
        this.code = code;
        this.width = width;
        this.height = height;
    }

    public int getCode() {
        return code;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }
}
