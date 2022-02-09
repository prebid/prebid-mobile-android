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
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;

// TODO: Remove
@Deprecated
public class MediationNativeAdUnit_old extends MediationBaseAdUnit {
    private static final String TAG = MediationNativeAdUnit_old.class.getSimpleName();

    public MediationNativeAdUnit_old(Context context, String configId, NativeAdConfiguration nativeAdConfiguration, PrebidMediationDelegate mediationDelegate) {
        super(context, configId, null, mediationDelegate);
        mAdUnitConfig.setNativeAdConfiguration(nativeAdConfiguration);
    }

    public void fetchDemand(
            @NonNull
                    OnFetchCompleteListener listener) {
        super.fetchDemand(listener);
    }

    @Override
    protected void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.NATIVE);
    }

    @Override
    protected void onResponseReceived(BidResponse response) {
        if (mOnFetchCompleteListener != null) {
            BidResponseCache.getInstance().putBidResponse(response);
            mMediationDelegate.handleKeywordsUpdate(response.getTargetingWithCacheId());
            mMediationDelegate.setResponseToLocalExtras(response);
            mOnFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
        }
    }
}
