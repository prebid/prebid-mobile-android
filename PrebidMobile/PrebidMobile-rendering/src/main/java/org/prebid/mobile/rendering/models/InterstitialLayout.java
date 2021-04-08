package org.prebid.mobile.rendering.models;

import android.content.pm.ActivityInfo;

public enum InterstitialLayout {
    LANDSCAPE(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE),
    PORTRAIT(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    ROTATABLE(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT),
    UNDEFINED(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);

    private int mOrientation;

    InterstitialLayout(int orientation) {
        mOrientation = orientation;
    }

    public int getOrientation() {
        return mOrientation;
    }
}
