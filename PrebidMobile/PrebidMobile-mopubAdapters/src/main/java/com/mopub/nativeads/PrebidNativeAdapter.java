package com.mopub.nativeads;

import android.content.Context;

import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAdParser;

import java.util.Map;

public class PrebidNativeAdapter extends CustomEventNative {

    private static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";

    @Override
    protected void loadNativeAd(
        @NonNull
            Context context,
        @NonNull
            CustomEventNativeListener customEventNativeListener,
        @NonNull
            Map<String, Object> localExtras,
        @NonNull
            Map<String, String> serverExtras) {
        Object bidResponseObj = localExtras.get(KEY_BID_RESPONSE);
        if (!(bidResponseObj instanceof BidResponse)) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.NATIVE_ADAPTER_CONFIGURATION_ERROR);
            return;
        }
        NativeAdParser nativeAdParser = new NativeAdParser();
        NativeAd nativeAd = nativeAdParser.parse(((BidResponse) bidResponseObj).getWinningBid().getAdm());
        if (nativeAd == null) {
            customEventNativeListener.onNativeAdFailed(NativeErrorCode.SERVER_ERROR_RESPONSE_CODE);
            return;
        }
        PrebidNativeAdWrapper prebidNativeAdWrapper = new PrebidNativeAdWrapper(nativeAd);
        customEventNativeListener.onNativeAdLoaded(prebidNativeAdWrapper);
    }
}
