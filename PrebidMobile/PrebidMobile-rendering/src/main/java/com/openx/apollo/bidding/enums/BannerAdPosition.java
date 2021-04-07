package com.openx.apollo.bidding.enums;

import com.openx.apollo.models.AdPosition;

public enum BannerAdPosition {
    UNDEFINED(-1),
    UNKNOWN(0),
    HEADER(4),
    FOOTER(5),
    SIDEBAR(6);

    private final int mValue;

    BannerAdPosition(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }

    public static BannerAdPosition mapToDisplayAdPosition(int adPosition) {
        final BannerAdPosition[] values = BannerAdPosition.values();
        for (BannerAdPosition bannerAdPosition : values) {
            if (bannerAdPosition.getValue() == adPosition) {
                return bannerAdPosition;
            }
        }
        return BannerAdPosition.UNDEFINED;
    }

    public static AdPosition mapToAdPosition(BannerAdPosition bannerAdPosition) {
        if (bannerAdPosition == null) {
            return AdPosition.UNDEFINED;
        }

        for (AdPosition adPosition : AdPosition.values()) {
            if (adPosition.getValue() == bannerAdPosition.getValue()) {
                return adPosition;
            }
        }
        return AdPosition.UNDEFINED;
    }
}
