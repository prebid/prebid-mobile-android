/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;

import java.util.EnumSet;

/**
 * Original API interstitial ad unit.
 */
public class InterstitialAdUnit extends BannerBaseAdUnit {

    /**
     * Default constructor.
     */
    public InterstitialAdUnit(@NonNull String configId) {
        super(configId, EnumSet.of(AdFormat.INTERSTITIAL));
        configuration.setAdPosition(AdPosition.FULLSCREEN);
    }

    /**
     * Constructor with min percentage width and height.
     */
    public InterstitialAdUnit(@NonNull String configId, @IntRange(from = 0, to = 100) int minWidthPerc, @IntRange(from = 0, to = 100) int minHeightPerc) {
        this(configId);
        configuration.setMinSizePercentage(new AdSize(minWidthPerc, minHeightPerc));
    }

    /**
     * Constructor for multi-format request.
     *
     * @param adUnitFormats for example `EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO);`
     */
    public InterstitialAdUnit(@NonNull String configId, EnumSet<AdUnitFormat> adUnitFormats) {
        super(configId, AdFormat.fromSet(adUnitFormats, true));

        configuration.addAdFormat(AdFormat.INTERSTITIAL);
        if (adUnitFormats.contains(AdUnitFormat.VIDEO)) {
            configuration.setAdPosition(AdPosition.FULLSCREEN);
            configuration.setPlacementType(PlacementType.INTERSTITIAL);
        }
    }

    public void setMinSizePercentage(
            @IntRange(from = 0, to = 100) int width,
            @IntRange(from = 0, to = 100) int height
    ) {
        configuration.setMinSizePercentage(new AdSize(width, height));
    }

    /**
     * Applies the interstitial native visibility tracker for tracking `burl` url.
     */
    public void activateInterstitialPrebidImpressionTracker() {
        this.activateInterstitialPrebidImpressionTracker = true;
    }

}
