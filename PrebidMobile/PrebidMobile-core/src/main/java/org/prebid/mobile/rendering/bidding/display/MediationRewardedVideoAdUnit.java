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
import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

public class MediationRewardedVideoAdUnit extends MediationBaseAdUnit {

    private static final String TAG = "MediationRewardedVideoAdUnit";

    public MediationRewardedVideoAdUnit(Context context, String configId, PrebidMediationDelegate mediationDelegate) {
        super(context, configId, null, mediationDelegate);
    }

    public void fetchDemand(@NonNull OnFetchCompleteListener listener) {
        super.fetchDemand(listener);
    }

    @Override
    protected final void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdUnitConfiguration.AdUnitIdentifierType.VAST);
        mAdUnitConfig.setRewarded(true);
        mAdUnitConfig.setAdPosition(AdPosition.FULLSCREEN);
    }

    @Override
    protected final void onResponseReceived(BidResponse response) {
        if (mOnFetchCompleteListener != null) {
            LogUtil.debug(TAG, "On response received");
            BidResponseCache.getInstance().putBidResponse(response.getId(), response);
            mMediationDelegate.setResponseToLocalExtras(response);
            mMediationDelegate.handleKeywordsUpdate(response.getTargeting());
            mOnFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
        }
    }
}
