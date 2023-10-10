package org.prebid.mobile.api.original;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.OnCompleteListener2;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.api.data.BidInfo;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.Map;

/**
 * Listener implementation for multiformat PrebidAdUnit.
 */
class OnCompleteListenerImpl implements OnCompleteListener, OnCompleteListener2 {

    @Nullable
    private Object adObject;
    @NonNull
    private PrebidRequest request;
    @NonNull
    private MultiformatAdUnitFacade adUnit;
    @NonNull
    private OnFetchDemandResult listener;

    OnCompleteListenerImpl(
            @NonNull MultiformatAdUnitFacade adUnit,
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
        BidInfo bidInfo = BidInfo.create(resultCode, bidResponse);
        boolean isNative = request.getNativeParameters() != null;
        if (isNative) {
            BidInfo.saveNativeResult(bidInfo, bidResponse, adObject);
        }
        listener.onComplete(bidInfo);
    }

}