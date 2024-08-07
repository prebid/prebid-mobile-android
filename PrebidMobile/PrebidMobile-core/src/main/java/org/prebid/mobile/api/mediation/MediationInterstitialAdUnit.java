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

package org.prebid.mobile.api.mediation;

import android.content.Context;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.models.AdPosition;

import java.util.EnumSet;

/**
 * Mediation interstitial ad unit for Rendering API with AdMob or AppLovin MAX.
 */
public class MediationInterstitialAdUnit extends MediationBaseFullScreenAdUnit {

    private static final String TAG = MediationInterstitialAdUnit.class.getSimpleName();

    /**
     * Constructor to fetch demand for a display interstitial ad with specified minHeightPercentage and minWidthPercentage
     */
    public MediationInterstitialAdUnit(
        Context context,
        String configId,
        AdSize minSizePercentage,
        PrebidMediationDelegate mediationDelegate
    ) {
        super(context, configId, minSizePercentage, mediationDelegate);
        adUnitConfig.setAdUnitFormats(EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO));
    }

    /**
     * Constructor to fetch demand for either display or video interstitial ads
     */
    public MediationInterstitialAdUnit(
        Context context,
        String configId,
        @NonNull EnumSet<AdUnitFormat> adUnitFormats,
        PrebidMediationDelegate mediationDelegate
    ) {
        super(context, configId, null, mediationDelegate);
        adUnitConfig.setAdUnitFormats(adUnitFormats);
    }

    /**
     * Loads ad and applies mediation delegate.
     *
     * @param listener callback when operation is completed (success or fail)
     */
    @Override
    public final void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        super.fetchDemand(listener);
    }

    @Override
    protected final void initAdConfig(
        String configId,
        AdSize minSizePercentage
    ) {
        adUnitConfig.setMinSizePercentage(minSizePercentage);
        adUnitConfig.setConfigId(configId);
        adUnitConfig.setAdPosition(AdPosition.FULLSCREEN);
    }

    /**
     * Sets min width and height in percentage. Range from 0 to 100.
     */
    public void setMinSizePercentage(
        @IntRange(from = 0, to = 100) int width,
        @IntRange(from = 0, to = 100) int height
    ) {
        adUnitConfig.setMinSizePercentage(new AdSize(width, height));
    }

}
