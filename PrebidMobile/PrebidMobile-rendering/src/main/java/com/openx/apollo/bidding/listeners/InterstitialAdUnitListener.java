package com.openx.apollo.bidding.listeners;

import com.openx.apollo.bidding.parallel.InterstitialAdUnit;
import com.openx.apollo.errors.AdException;

/**
 * Listener interface representing OXBInterstitialAdUnit events.
 * All methods will be invoked on the main thread.
 */
public interface InterstitialAdUnitListener {
    /**
     * Executed when the ad is loaded and is ready for display.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdLoaded(InterstitialAdUnit interstitialAdUnit);

    /**
     * Executed when the ad is displayed on screen.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdDisplayed(InterstitialAdUnit interstitialAdUnit);

    /**
     * Executed when an error is encountered on initialization / loading or display step.
     *
     * @param interstitialAdUnit view of the corresponding event.
     * @param exception  exception containing detailed message and error type.
     */
    void onAdFailed(InterstitialAdUnit interstitialAdUnit, AdException exception);

    /**
     * Executed when interstitialAdUnit is clicked.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdClicked(InterstitialAdUnit interstitialAdUnit);

    /**
     * Executed when interstitialAdUnit is closed.
     *
     * @param interstitialAdUnit view of the corresponding event.
     */
    void onAdClosed(InterstitialAdUnit interstitialAdUnit);
}
