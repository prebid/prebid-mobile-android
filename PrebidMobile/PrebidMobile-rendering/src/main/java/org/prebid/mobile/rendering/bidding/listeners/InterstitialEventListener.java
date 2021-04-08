package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.errors.AdException;

public interface InterstitialEventListener {
    void onOXBSdkWin();

    void onAdServerWin();

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();

    void onAdDisplayed();
}
