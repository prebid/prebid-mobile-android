/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.rendering.interstitial;

import android.text.TextUtils;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.models.InterstitialLayout;

/**
 * Util class to configure InterstitialDisplayProperties based on adConfiguration
 */
public class InterstitialLayoutConfigurator {

    public static void configureDisplayProperties(AdUnitConfiguration adConfiguration, InterstitialDisplayPropertiesInternal displayProperties) {
        String size = adConfiguration.getInterstitialSize();
        if (TextUtils.isEmpty(size) || InterstitialSizes.isPortrait(size)) {
            displayProperties.isRotationEnabled = false;
            displayProperties.orientation = InterstitialLayout.PORTRAIT.getOrientation();
        } else if (InterstitialSizes.isLandscape(size)) {
            displayProperties.isRotationEnabled = false;
            displayProperties.orientation = InterstitialLayout.LANDSCAPE.getOrientation();
        } else {
            displayProperties.isRotationEnabled = true;
        }

        displayProperties.isSoundButtonVisible = adConfiguration.isSoundButtonVisible();
        displayProperties.isMuted = adConfiguration.isMuted();
        displayProperties.closeButtonArea = adConfiguration.getCloseButtonArea();
        displayProperties.closeButtonPosition = adConfiguration.getCloseButtonPosition();
        displayProperties.skipDelay = adConfiguration.getSkipDelay();
        displayProperties.skipButtonArea = adConfiguration.getSkipButtonArea();
        displayProperties.skipButtonPosition = adConfiguration.getSkipButtonPosition();

        displayProperties.config = adConfiguration;
    }
}
