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

import androidx.annotation.NonNull;

import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdUnitFormat;
import org.prebid.mobile.api.data.FetchDemandResult;
import org.prebid.mobile.api.mediation.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.models.AdPosition;

import java.util.EnumSet;

/**
 * Mediation rewarded ad unit for Rendering API with AdMob or AppLovin MAX.
 */
public class MediationRewardedAdUnit extends MediationBaseFullScreenAdUnit {

    private static final String TAG = "MediationRewardedAdUnit";

    /**
     * Constructor to fetch demand for video rewarded ads by default.
     */
    public MediationRewardedAdUnit(
        Context context,
        String configId,
        PrebidMediationDelegate mediationDelegate
    ) {
        this(context, configId, EnumSet.of(AdUnitFormat.VIDEO), mediationDelegate);
    }

    /**
     * Constructor to fetch demand for the specified rewarded ad formats.
     * Use {@code EnumSet.of(AdUnitFormat.BANNER)} for display rewarded ads,
     * {@code EnumSet.of(AdUnitFormat.VIDEO)} for rewarded video ads, or both for multiformat rewarded inventory.
     */
    public MediationRewardedAdUnit(
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
    public void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        super.fetchDemand(listener);
    }

    @Override
    protected final void initAdConfig(
        String configId,
        AdSize adSize
    ) {
        adUnitConfig.setConfigId(configId);
        adUnitConfig.setRewarded(true);
        adUnitConfig.setAdPosition(AdPosition.FULLSCREEN);
        // Ad formats are set by constructors via setAdUnitFormats().
    }

    @Override
    protected final void onResponseReceived(BidResponse response) {
        if (onFetchCompleteListener != null) {
            LogUtil.debug(TAG, "On response received");
            BidResponseCache.getInstance().putBidResponse(response.getId(), response);
            mediationDelegate.setResponseToLocalExtras(response);
            mediationDelegate.handleKeywordsUpdate(response.getTargeting());
            onFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
        }
    }

}
