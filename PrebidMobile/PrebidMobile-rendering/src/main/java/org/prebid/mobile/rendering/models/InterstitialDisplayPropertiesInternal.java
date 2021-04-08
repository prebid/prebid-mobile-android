package org.prebid.mobile.rendering.models;

import android.content.pm.ActivityInfo;

public class InterstitialDisplayPropertiesInternal extends InterstitialDisplayPropertiesPublic {
    public int expandWidth;
    public int expandHeight;
    public int orientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;

    public boolean isRotationEnabled = false;

    public void resetExpandValues() {
        expandHeight = 0;
        expandWidth = 0;
    }
}
