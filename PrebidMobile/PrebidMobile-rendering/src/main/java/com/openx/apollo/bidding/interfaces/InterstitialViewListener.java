package com.openx.apollo.bidding.interfaces;

import com.openx.apollo.bidding.display.InterstitialView;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdDetails;

public interface InterstitialViewListener {
    /**
     * This is triggered whenever the AD is rendered on the screen.
     */
    void onAdLoaded(InterstitialView interstitialView, AdDetails adDetails);

    /**
     * When AdModel fails to load for whatever reason
     *
     * @param error The AdException received when trying to load the Ad
     */
    void onAdFailed(InterstitialView interstitialView, AdException error);

    /**
     * When a loaded ad is displayed
     */
    void onAdDisplayed(InterstitialView interstitialView);

    /**
     * When an ad has finished refreshing.
     */
    void onAdCompleted(InterstitialView interstitialView);

    /**
     * When an ad was clicked
     */
    void onAdClicked(InterstitialView interstitialView);

    /**
     * When an expanded banner ad was closed
     */
    void onAdClickThroughClosed(InterstitialView interstitialView);

    void onAdClosed(InterstitialView interstitialView);
}
