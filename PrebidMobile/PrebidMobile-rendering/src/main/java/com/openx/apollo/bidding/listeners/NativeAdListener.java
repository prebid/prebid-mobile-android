package com.openx.apollo.bidding.listeners;

import com.openx.apollo.bidding.data.ntv.NativeAd;
import com.openx.apollo.models.ntv.NativeEventTracker;

public interface NativeAdListener {

    void onAdClicked(NativeAd nativeAd);

    void onAdEvent(NativeAd nativeAd, NativeEventTracker.EventType eventType);
}