package com.openx.apollo.bidding.listeners;

import com.openx.apollo.errors.AdException;

public interface RewardedVideoEventListener {
    void onOXBSdkWin();

    void onAdServerWin(Object userReward);

    void onAdFailed(AdException exception);

    void onAdClicked();

    void onAdClosed();

    void onAdDisplayed();

    void onUserEarnedReward();
}
