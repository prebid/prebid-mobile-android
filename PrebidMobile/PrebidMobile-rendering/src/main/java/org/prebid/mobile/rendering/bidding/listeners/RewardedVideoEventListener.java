package org.prebid.mobile.rendering.bidding.listeners;

import org.prebid.mobile.rendering.errors.AdException;

public interface RewardedVideoEventListener {
    void onOXBSdkWin();

    void onAdServerWin(Object userReward);

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();

    void onAdDisplayed();

    void onUserEarnedReward();
}
