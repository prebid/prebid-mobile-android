package org.prebid.mobile;

public interface PrebidNativeAdEventListener {
    void onAdClicked();
    void onAdImpression();
    void onAdExpired();
}
