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
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.NativeFetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.bidding.listeners.OnNativeFetchCompleteListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.utils.logger.OXLog;

public class NativeAdUnit extends BaseAdUnit {
    private static final String TAG = NativeAdUnit.class.getSimpleName();

    private OnNativeFetchCompleteListener mNativeFetchCompleteListener;
    private final OnFetchCompleteListener mOnFetchCompleteListener = result -> {
        if (mNativeFetchCompleteListener != null) {
            mNativeFetchCompleteListener.onComplete(new NativeFetchDemandResult(result));
        }
    };

    public NativeAdUnit(Context context, String configId,
                        @NonNull
                                NativeAdConfiguration nativeAdConfiguration) {
        super(context, configId, null);
        mAdUnitConfig.setNativeAdConfiguration(nativeAdConfiguration);
    }

    @Override
    protected void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.NATIVE);
    }

    @Override
    protected boolean isAdObjectSupported(
        @Nullable
            Object adObject) {
        return true;
    }

    @Override
    protected void onResponseReceived(BidResponse response) {
        if (mNativeFetchCompleteListener == null) {
            OXLog.error(TAG, "Failed to pass callback. Ad object or OnFetchCompleteListener is null");
            return;
        }

        final NativeFetchDemandResult result = new NativeFetchDemandResult(FetchDemandResult.SUCCESS);
        BidResponseCache.getInstance().putBidResponse(response);
        result.setKeyWordsMap(response.getTargetingWithCacheId());

        mNativeFetchCompleteListener.onComplete(result);
    }

    @Override
    protected void onErrorReceived(AdException exception) {
        if (mNativeFetchCompleteListener == null) {
            OXLog.error(TAG, "Failed to pass callback. Ad object or OnFetchCompleteListener is null");
            return;
        }
        final FetchDemandResult fetchDemandResult = FetchDemandResult.parseErrorMessage(exception.getMessage());
        mNativeFetchCompleteListener.onComplete(new NativeFetchDemandResult(fetchDemandResult));
    }

    public void fetchDemand(
        @NonNull
            OnNativeFetchCompleteListener listener) {
        mNativeFetchCompleteListener = listener;
        super.fetchDemand(null, mOnFetchCompleteListener);
    }
}
