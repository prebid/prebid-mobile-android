package com.openx.apollo.interstitial;

import android.text.TextUtils;

import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.InterstitialDisplayPropertiesInternal;
import com.openx.apollo.models.InterstitialLayout;

/**
 * Util class to configure InterstitialDisplayProperties based on adConfiguration
 */
public class InterstitialLayoutConfigurator {

    public static void configureDisplayProperties(AdConfiguration adConfiguration, InterstitialDisplayPropertiesInternal displayProperties) {
        String size = adConfiguration.getInterstitialSize();
        if (TextUtils.isEmpty(size) || InterstitialSizes.isPortrait(size)) {
            displayProperties.isRotationEnabled = false;
            displayProperties.orientation = InterstitialLayout.PORTRAIT.getOrientation();
        }
        else if (InterstitialSizes.isLandscape(size)) {
            displayProperties.isRotationEnabled = false;
            displayProperties.orientation = InterstitialLayout.LANDSCAPE.getOrientation();
        }
        else {
            displayProperties.isRotationEnabled = true;
        }
    }
}
