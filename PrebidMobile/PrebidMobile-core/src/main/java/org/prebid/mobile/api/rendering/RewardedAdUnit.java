/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.prebid.mobile.api.rendering;

import android.content.Context;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneRewardedVideoEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;
import org.prebid.mobile.rendering.interstitial.rewarded.Reward;
import org.prebid.mobile.rendering.interstitial.rewarded.RewardManager;

import java.util.EnumSet;

/**
 * Rewarded ad unit for rendering API.
 */
public class RewardedAdUnit extends BaseInterstitialAdUnit {

    private static final String TAG = RewardedAdUnit.class.getSimpleName();

    /**
     * Handler that is responsible for requesting, displaying and destroying of
     * primary ad (e.g. GAM). Also it tracks impression and sets listener.
     */
    private final RewardedEventHandler eventHandler;

    /**
     * Interstitial ad units events listener (like onAdLoaded, onAdFailed...)
     */
    @Nullable
    private RewardedAdUnitListener userListener;

    /**
     * Listener that must be applied to InterstitialEventHandler.
     * It is responsible for onAdServerWin or onPrebidSdkWin.
     */
    private final RewardedVideoEventListener eventListener = createRewardedListener();

    /**
     * Constructor that creates the instance with a {@link StandaloneRewardedVideoEventHandler}
     * for integration without any primary ad server.
     *
     * @param context  Android context
     * @param configId configuration id for on Prebid Server
     */
    public RewardedAdUnit(
            Context context,
            String configId
    ) {
        this(context, configId, new StandaloneRewardedVideoEventHandler());
    }

    /**
     * Constructor that initializes a RewardedAdUnit with a custom event handler.
     * It supports GAM integration (use {@code GamRewardedEventHandler})
     *
     * @param context      the Android context
     * @param configId     the configuration id on Prebid Server
     * @param eventHandler the event handler for primary ad server responsible for managing rewarded ad
     */
    public RewardedAdUnit(
        Context context,
        String configId,
        RewardedEventHandler eventHandler
    ) {
        super(context);
        this.eventHandler = eventHandler;
        this.eventHandler.setRewardedEventListener(eventListener);

        AdUnitConfiguration adUnitConfiguration = new AdUnitConfiguration();
        adUnitConfiguration.setConfigId(configId);
        adUnitConfiguration.setAdFormats(EnumSet.of(AdFormat.INTERSTITIAL, AdFormat.VAST));
        adUnitConfiguration.setRewarded(true);

        init(adUnitConfiguration);
    }

    /**
     * Executes ad loading if no request is running.
     */
    @Override
    public void loadAd() {
        super.loadAd();
        RewardManager rewardManager = config.getRewardManager();
        rewardManager.clear();
        rewardManager.setRewardListener(controllerListener::onUserEarnedReward);
    }

    /**
     * Cleans up resources when destroyed. It has to be called if the ad unit is no longer needed.
     */
    @Override
    public void destroy() {
        super.destroy();
        config.getRewardManager().clear();
        if (eventHandler != null) {
            eventHandler.destroy();
        }
    }

    /**
     * Sets the listener for the rewarded ad events.
     */
    public void setRewardedAdUnitListener(@Nullable RewardedAdUnitListener userListener) {
        this.userListener = userListener;
    }

    @Override
    void requestAdWithBid(
        @Nullable
            Bid bid
    ) {
        eventHandler.requestAdWithBid(bid);
    }

    @Override
    void showGamAd() {
        eventHandler.show();
    }

    @Override
    void notifyAdEventListener(AdListenerEvent event) {
        if (userListener == null) {
            LogUtil.debug(
                TAG,
                    "notifyAdEventListener: Failed. AdUnitListener is null. Passed listener event: " + event
            );
            return;
        }

        switch (event) {
            case AD_CLOSE:
                userListener.onAdClosed(RewardedAdUnit.this);
                break;
            case AD_LOADED:
                userListener.onAdLoaded(RewardedAdUnit.this);
                break;
            case AD_DISPLAYED:
                userListener.onAdDisplayed(RewardedAdUnit.this);
                break;
            case AD_CLICKED:
                userListener.onAdClicked(RewardedAdUnit.this);
                break;
            case USER_RECEIVED_PREBID_REWARD:
                Reward reward = getReward();
                LogUtil.debug(TAG, "Internal reward listener: " + reward);
                userListener.onUserEarnedReward(RewardedAdUnit.this, reward);
                break;
        }
    }

    @Override
    void notifyErrorListener(AdException exception) {
        if (userListener != null) {
            userListener.onAdFailed(RewardedAdUnit.this, exception);
        }
    }

    @Nullable
    private Reward getReward() {
        Reward reward = config.getRewardManager().getRewardedExt().getReward();
        if (reward != null) {
            return reward;
        }

        return eventHandler.getReward();
    }

    private RewardedVideoEventListener createRewardedListener() {
        return new RewardedVideoEventListener() {
            @Override
            public void onPrebidSdkWin() {
                if (isBidInvalid()) {
                    changeInterstitialAdUnitState(InterstitialAdUnitState.READY_FOR_LOAD);
                    notifyErrorListener(new AdException(
                            AdException.INTERNAL_ERROR,
                            "WinnerBid is null when executing onPrebidSdkWin."
                    ));
                    return;
                }

                loadPrebidAd();
            }

            @Override
            public void onAdServerWin(Object userReward) {
                changeInterstitialAdUnitState(InterstitialAdUnitState.READY_TO_DISPLAY_GAM);
                notifyAdEventListener(AdListenerEvent.AD_LOADED);
            }

            @Override
            public void onAdFailed(AdException exception) {
                if (isBidInvalid()) {
                    changeInterstitialAdUnitState(InterstitialAdUnitState.READY_FOR_LOAD);
                    notifyErrorListener(exception);
                    return;
                }

                onPrebidSdkWin();
            }

            @Override
            public void onAdClicked() {
                notifyAdEventListener(AdListenerEvent.AD_CLICKED);
            }

            @Override
            public void onAdClosed() {
                notifyAdEventListener(AdListenerEvent.AD_CLOSE);
            }

            @Override
            public void onAdDisplayed() {
                changeInterstitialAdUnitState(InterstitialAdUnitState.READY_FOR_LOAD);
                notifyAdEventListener(AdListenerEvent.AD_DISPLAYED);
            }

            @Override
            public void onUserEarnedReward() {
                notifyUserReward();
            }
        };
    }

}
