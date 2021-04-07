package com.openx.apollo.bidding.display;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.OnFetchCompleteListener;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.ntv.NativeAdConfiguration;

import java.util.HashMap;

public class MoPubNativeAdUnit extends BaseAdUnit {
    private static final String TAG = MoPubNativeAdUnit.class.getSimpleName();
    private HashMap<String, String> mKeywordsMap;

    public MoPubNativeAdUnit(Context context, String configId, NativeAdConfiguration nativeAdConfiguration) {
        super(context, configId, null);
        mAdUnitConfig.setNativeAdConfiguration(nativeAdConfiguration);
    }

    public void fetchDemand(
        @NonNull
            HashMap<String, String> keywords,
        @NonNull
            Object mopubNative,
        @NonNull
            OnFetchCompleteListener listener) {
        mKeywordsMap = keywords;
        super.fetchDemand(mopubNative, listener);
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
        return ReflectionUtils.isMoPubNative(adObject);
    }

    @Override
    protected void onResponseReceived(BidResponse response) {
        if (mOnFetchCompleteListener != null && mAdViewReference != null && mAdViewReference.get() != null) {
            BidResponseCache.getInstance().putBidResponse(response);
            ReflectionUtils.handleMoPubKeywordsUpdate(mKeywordsMap, response.getTargetingWithCacheId());
            ReflectionUtils.setResponseToMoPubLocalExtras(mAdViewReference.get(), response);
            mOnFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
        }
    }
}
