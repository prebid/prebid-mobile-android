package org.prebid.mobile.admob;

import com.google.android.gms.ads.AdError;

import org.prebid.mobile.LogUtil;

class AdErrors {

    static AdError emptyResponseId() {
        return createError(
                1,
                "Response id is null"
        );
    }

    static AdError notMatchedParameters() {
        return createError(
                2,
                "Parameters are different"
        );
    }

    static AdError noResponse(String responseId) {
        return createError(
                3,
                "There's no response for the current response id: " + responseId
        );
    }

    static AdError failedToLoadAd(String error) {
        String message = "Failed to load ad: " + error;
        LogUtil.error("PrebidAdapter", message);
        return createError(
                4,
                message
        );
    }

    static AdError emptyPrebidKeywords() {
        return createError(
                5,
                "Prebid keywords are empty"
        );
    }

    static AdError interstitialControllerError(String error) {
        String message = "Exception in Prebid interstitial controller: " + error;
        LogUtil.error("PrebidAdapter", message);
        return createError(
                6,
                message
        );
    }

    static AdError emptyNativeCacheId() {
        return createError(
                7,
                "Empty native ad unit cache id"
        );
    }

    static AdError prebidNativeAdIsNull() {
        return createError(
                8,
                "PrebidNativeAd is null"
        );
    }

    private static AdError createError(int code, String message) {
        return new AdError(code, message, "prebid");
    }

}