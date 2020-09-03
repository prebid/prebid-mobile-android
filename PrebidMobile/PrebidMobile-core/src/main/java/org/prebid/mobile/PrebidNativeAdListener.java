package org.prebid.mobile;

public interface PrebidNativeAdListener {
    /**
     * A successful Prebid Native ad is returned
     *
     * @param ad use this instance for displaying
     */
    void onPrebidNativeLoaded(PrebidNativeAd ad);

    /**
     * Prebid Native was not found in the server returned response,
     * Please display the ad as regular ways
     */
    void onPrebidNativeNotFound();

    /**
     * Prebid Native ad was returned, however, the bid is not valid for displaying
     * Should be treated as on ad load failed
     */
    void onPrebidNativeNotValid();
}
