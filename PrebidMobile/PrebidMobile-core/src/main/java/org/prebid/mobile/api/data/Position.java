package org.prebid.mobile.api.data;

import androidx.annotation.Nullable;

/**
 * Control item position. It's used for controlling rendering API button positions.
 */
public enum Position {
    TOP_LEFT,
    TOP,
    TOP_RIGHT,
    RIGHT,
    BOTTOM_RIGHT,
    BOTTOM,
    BOTTOM_LEFT,
    LEFT;

    @Nullable
    public static Position fromString(String string) {
        switch (string.toLowerCase()) {
            case "topleft":
                return TOP_LEFT;
            case "top":
                return TOP;
            case "topright":
                return TOP_RIGHT;
            case "right":
                return RIGHT;
            case "bottomright":
                return BOTTOM_RIGHT;
            case "bottom":
                return BOTTOM;
            case "bottomleft":
                return BOTTOM_LEFT;
            case "left":
                return LEFT;
            default:
                return null;
        }
    }
}
