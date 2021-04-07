package com.openx.apollo.bidding.listeners;

import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.ntv.NativeAd;

public interface NativeAdCallback {
    void onNativeAdReceived(
        @Nullable
            NativeAd nativeAd);
}
