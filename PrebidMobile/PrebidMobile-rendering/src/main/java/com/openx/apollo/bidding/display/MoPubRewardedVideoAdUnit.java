package com.openx.apollo.bidding.display;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.AdSize;
import com.openx.apollo.bidding.data.FetchDemandResult;
import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.OnFetchCompleteListener;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.AdPosition;

import java.util.HashMap;

public class MoPubRewardedVideoAdUnit extends BaseAdUnit {

    private final String mMopubAdUnitId;

    public MoPubRewardedVideoAdUnit(Context context,
                                    @NonNull
                                        String mopubAdUnitId, String configId) {
        super(context, configId, null);
        mMopubAdUnitId = mopubAdUnitId;
    }

    public final void fetchDemand(
        @Nullable
            HashMap<String, String> hashMap,
        @NonNull
            OnFetchCompleteListener listener) {
        super.fetchDemand(hashMap, listener);
    }

    @Override
    protected final void initAdConfig(String configId, AdSize adSize) {
        mAdUnitConfig.setConfigId(configId);
        mAdUnitConfig.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
        mAdUnitConfig.setRewarded(true);
        mAdUnitConfig.setAdPosition(AdPosition.FULLSCREEN);
    }

    @Override
    protected final boolean isAdObjectSupported(
        @Nullable
            Object adObject) {
        return adObject instanceof HashMap;
    }

    @Override
    protected final void onResponseReceived(BidResponse response) {
        if (mOnFetchCompleteListener != null && mAdViewReference != null && mAdViewReference.get() != null) {
            BidResponseCache.getInstance().putBidResponse(mMopubAdUnitId, response);
            ReflectionUtils.handleMoPubKeywordsUpdate(mAdViewReference.get(), response.getTargeting());
            mOnFetchCompleteListener.onComplete(FetchDemandResult.SUCCESS);
        }
    }
}
