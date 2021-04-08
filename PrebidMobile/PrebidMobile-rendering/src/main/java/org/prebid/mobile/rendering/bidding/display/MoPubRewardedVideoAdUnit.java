package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.OnFetchCompleteListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdPosition;

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
