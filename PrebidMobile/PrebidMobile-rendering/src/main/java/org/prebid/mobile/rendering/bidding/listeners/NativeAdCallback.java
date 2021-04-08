package org.prebid.mobile.rendering.bidding.listeners;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;

public interface NativeAdCallback {
    void onNativeAdReceived(
        @Nullable
            NativeAd nativeAd);
}
