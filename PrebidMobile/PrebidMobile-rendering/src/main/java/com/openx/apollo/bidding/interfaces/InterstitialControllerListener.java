package com.openx.apollo.bidding.interfaces;

import com.openx.apollo.errors.AdException;

public interface InterstitialControllerListener {
    void onInterstitialReadyForDisplay();

    void onInterstitialClicked();

    void onInterstitialFailedToLoad(AdException exception);

    void onInterstitialDisplayed();

    void onInterstitialClosed();
}
