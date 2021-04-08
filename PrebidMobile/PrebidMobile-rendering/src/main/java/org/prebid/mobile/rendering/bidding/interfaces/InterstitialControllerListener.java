package org.prebid.mobile.rendering.bidding.interfaces;

import org.prebid.mobile.rendering.errors.AdException;

public interface InterstitialControllerListener {
    void onInterstitialReadyForDisplay();

    void onInterstitialClicked();

    void onInterstitialFailedToLoad(AdException exception);

    void onInterstitialDisplayed();

    void onInterstitialClosed();
}
