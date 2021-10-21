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

package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;

public class MoPubInterstitialAdUnit extends BaseAdUnit {
    private static final String TAG = MoPubInterstitialAdUnit.class.getSimpleName();

    /**
     * Constructor to fetch demand for a display interstitial ad with specified minHeightPercentage and minWidthPercentage
     */
    public MoPubInterstitialAdUnit(Context context, String configId, AdSize minSizePercentage) {
        super(context, configId, minSizePercentage);
    }

    /**
     * Constructor to fetch demand for either display or video interstitial ads
     */
    public MoPubInterstitialAdUnit(Context context, String configId,
                                   @NonNull
                                           AdUnitFormat adUnitFormat) {
        super(context, configId, null);
        setAdUnitType(adUnitFormat);
    }

    @Override
    public final void fetchDemand(
        @Nullable
            Object mopubView,
        @NonNull
            OnFetchCompleteListener listener) {
        super.fetchDemand(mopubView, listener);
    }

    @Override
    protected final void initAdConfig(String configId, AdSize minSizePercentage) {
        mAdUnitConfig.setMinSizePercentage(minSizePercentage);
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
        mAdUnitConfig.setAdPosition(AdPosition.FULLSCREEN);
    }

    @Override
    protected final boolean isAdObjectSupported(
        @Nullable
            Object adObject) {
        return ReflectionUtils.isMoPubInterstitialView(adObject);
    }

    private void setAdUnitType(AdUnitFormat adUnitFormat) {
        switch (adUnitFormat) {
            case DISPLAY:
                mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.INTERSTITIAL);
                break;
            case VIDEO:
                mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
                break;
        }
    }
}
