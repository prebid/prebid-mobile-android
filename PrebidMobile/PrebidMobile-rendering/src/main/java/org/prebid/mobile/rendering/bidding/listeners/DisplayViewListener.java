package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.errors.AdException;

public interface DisplayViewListener {
    // Called every time an ad had loaded and is ready for display
    void onAdLoaded();

    // Called every time the ad is displayed on the screen
    void onAdDisplayed();

    // Called whenever the load process fails to produce a viable ad
    void onAdFailed(AdException exception);

    // Called when the banner view will launch a dialog on top of the current view
    void onAdClicked();

    // Called when the banner view has dismissed the modal on top of the current view
    void onAdClosed();
}
