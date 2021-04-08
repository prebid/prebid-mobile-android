package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.bidding.parallel.BannerView;
import org.prebid.mobile.rendering.errors.AdException;

/**
 * Listener interface representing OXBBannerView events.
 * All methods will be invoked on the main thread.
 */
public interface BannerViewListener {
    /**
     * Executed when the ad is loaded and is ready for display.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdLoaded(BannerView bannerView);

    /**
     * Executed when the ad is displayed on screen.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdDisplayed(BannerView bannerView);

    /**
     * Executed when an error is encountered on initialization / loading or display step.
     *
     * @param bannerView view of the corresponding event.
     * @param exception  exception containing detailed message and error type.
     */
    void onAdFailed(BannerView bannerView, AdException exception);

    /**
     * Executed when bannerView is clicked.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdClicked(BannerView bannerView);

    /**
     * Executed when modal window (e.g. browser) on top of bannerView is closed.
     *
     * @param bannerView view of the corresponding event.
     */
    void onAdClosed(BannerView bannerView);
}
