package org.prebid.mobile.rendering.models;

public enum AdPosition {
    UNDEFINED(-1),
    UNKNOWN(0),
    HEADER(4),
    FOOTER(5),
    SIDEBAR(6),
    FULLSCREEN(7);

    private final int mValue;

    AdPosition(int value) {
        mValue = value;
    }

    public int getValue() {
        return mValue;
    }
}
