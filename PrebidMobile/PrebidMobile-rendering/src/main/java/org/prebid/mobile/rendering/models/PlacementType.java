package org.prebid.mobile.rendering.models;

public enum PlacementType {
    UNDEFINED(-1),
    IN_BANNER(2),
    IN_ARTICLE(3),
    IN_FEED(4),
    INTERSTITIAL(5);

    private final int mValue;

    PlacementType(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
