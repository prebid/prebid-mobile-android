package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;

public interface NativeAdListener {

    void onAdClicked(NativeAd nativeAd);

    void onAdEvent(NativeAd nativeAd, NativeEventTracker.EventType eventType);
}