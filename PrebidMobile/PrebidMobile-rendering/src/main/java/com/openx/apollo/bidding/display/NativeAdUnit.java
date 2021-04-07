package com.openx.apollo.bidding.display;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.NativeFetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.OnFetchCompleteListener;
import com.openx.apollo.bidding.listeners.OnNativeFetchCompleteListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.ntv.NativeAdConfiguration;
import com.openx.apollo.utils.logger.OXLog;

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
