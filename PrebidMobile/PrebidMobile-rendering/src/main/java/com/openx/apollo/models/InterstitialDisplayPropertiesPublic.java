package com.openx.apollo.models;

import android.graphics.Color;

public class InterstitialDisplayPropertiesPublic {
    // this is the default system dim for dialogs
    private int dimColor = Color.argb(153, 0, 0, 0);

    /**
     * Returns the background opacity set for an interstitial ad
     *
     * @return
     */
    public int getPubBackGroundOpacity() {
        return dimColor;
    }

    /**
     * Sets the background opacity for an interstitial ad
     * Recommended is 1.0f
     *
     * @param dim
     */
    public void setPubBackGroundOpacity(float dim) {
        int alpha;

        float dimAmount;
        if (dim < 0.0f) {
            dimAmount = 0.0f;
        }
        else if (dim > 1.0f) {
            dimAmount = 1.0f;
        }
        else {
            dimAmount = dim;
        }

        alpha = (int) (255 * dimAmount);

        dimColor = Color.argb(alpha, 0, 0, 0);
    }
}
