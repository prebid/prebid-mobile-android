package org.prebid.mobile.drprebid.model;

import org.prebid.mobile.drprebid.Constants;

public enum AdSize {
    BANNER_300x250(Constants.Settings.AdSizeCodes.SIZE_300x250),
    BANNER_300x600(Constants.Settings.AdSizeCodes.SIZE_300x600),
    BANNER_320x50(Constants.Settings.AdSizeCodes.SIZE_320x50),
    BANNER_320x100(Constants.Settings.AdSizeCodes.SIZE_320x100),
    BANNER_320x480(Constants.Settings.AdSizeCodes.SIZE_320x480),
    BANNER_728x90(Constants.Settings.AdSizeCodes.SIZE_728x90);

    private int code;

    AdSize(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
