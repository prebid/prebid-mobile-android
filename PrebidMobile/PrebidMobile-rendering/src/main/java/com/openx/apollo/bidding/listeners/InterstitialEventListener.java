package com.openx.apollo.bidding.listeners;

import com.openx.apollo.errors.AdException;

public interface InterstitialEventListener {
    void onOXBSdkWin();

    void onAdServerWin();

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();

    void onAdDisplayed();
}
