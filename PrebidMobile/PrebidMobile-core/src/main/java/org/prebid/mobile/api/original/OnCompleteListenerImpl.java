package org.prebid.mobile.api.original;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.CacheManager;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.OnCompleteListener2;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.Map;

class OnCompleteListenerImpl implements OnCompleteListener, OnCompleteListener2 {

    @Nullable
    private Object adObject;
    @NonNull
    private PrebidRequest request;
    @NonNull
    private ConfigurableAdUnit adUnit;
    @NonNull
    private OnFetchDemandResult listener;

    OnCompleteListenerImpl(
            @NonNull ConfigurableAdUnit adUnit,
            @NonNull PrebidRequest request,
            @Nullable Object adObject,
            @NonNull OnFetchDemandResult listener
    ) {
        this.adObject = adObject;
        this.adUnit = adUnit;
        this.request = request;
        this.listener = listener;
    }

    @Override
    public void onComplete(ResultCode resultCode) {
        notifyListener(resultCode);
    }

    @Override
    public void onComplete(ResultCode resultCode, @Nullable Map<String, String> unmodifiableMap) {
        notifyListener(resultCode);
    }


    private void notifyListener(ResultCode resultCode) {
        BidResponse bidResponse = adUnit.getBidResponse();

        if (bidResponse == null) {
            listener.onComplete(new BidInfo(resultCode, null));
            return;
        }

        BidInfo bidInfo = new BidInfo(resultCode, bidResponse.getTargeting());
        saveCacheForNativeIfNeeded(bidResponse, bidInfo, resultCode);
        listener.onComplete(bidInfo);
    }

    private void saveCacheForNativeIfNeeded(
            BidResponse bidResponse,
            BidInfo bidInfo,
            ResultCode resultCode
    ) {
        if (resultCode == ResultCode.SUCCESS) {
            boolean isNative = request.getNativeParameters() != null;
            if (isNative) {
                String cacheId = CacheManager.save(bidResponse.getWinningBidJson());
                Util.saveCacheId(cacheId, adObject);
                bidInfo.setNativeResult(cacheId, bidResponse.getExpirationTimeSeconds());
            }
        }
    }

}