package org.prebid.demandsdkadapters.common;


import org.prebid.mobile.core.ErrorCode;

public interface AdListener {
    void onAdLoaded(Object adObj);

    void onAdFailed(Object adObj, ErrorCode errorCode);

    void onAdClicked(Object adObj);

    // the following two are for interstitial, won't be called by banner
    void onInterstitialShown(Object adObj);

    void onInterstitialClosed(Object adObj);
}
