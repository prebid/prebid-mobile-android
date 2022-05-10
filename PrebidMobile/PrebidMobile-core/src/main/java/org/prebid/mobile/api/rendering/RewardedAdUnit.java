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

public class RewardedAdUnit extends BaseInterstitialAdUnit {

    private static final String TAG = RewardedAdUnit.class.getSimpleName();

    private final RewardedEventHandler eventHandler;

    @Nullable private RewardedAdUnitListener rewardedAdUnitListener;

    @Nullable private Object userReward;

    //region ==================== Listener implementation

    private final RewardedVideoEventListener eventListener = new RewardedVideoEventListener() {
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
            RewardedAdUnit.this.userReward = userReward;
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
            if (rewardedAdUnitListener != null) {
                rewardedAdUnitListener.onUserEarnedReward(RewardedAdUnit.this);
            }
        }
    };
    //endregion ==================== Listener implementation

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
        adUnitConfiguration.setAdFormat(AdFormat.VAST);
        adUnitConfiguration.setRewarded(true);

        init(adUnitConfiguration);
    }

    public RewardedAdUnit(
        Context context,
        String configId
    ) {
        this(context, configId, new StandaloneRewardedVideoEventHandler());
    }

    @Override
    public void loadAd() {
        super.loadAd();
        userReward = null;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (eventHandler != null) {
            eventHandler.destroy();
        }
    }

    //region ==================== getters and setters
    public void setRewardedAdUnitListener(
        @Nullable
            RewardedAdUnitListener rewardedAdUnitListener
    ) {
        this.rewardedAdUnitListener = rewardedAdUnitListener;
    }

    @Nullable
    public Object getUserReward() {
        return userReward;
    }
    //endregion ==================== getters and setters

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
    void notifyAdEventListener(AdListenerEvent adListenerEvent) {
        if (rewardedAdUnitListener == null) {
            LogUtil.debug(
                TAG,
                "notifyAdEventListener: Failed. AdUnitListener is null. Passed listener event: " + adListenerEvent
            );
            return;
        }

        switch (adListenerEvent) {
            case AD_CLOSE:
                rewardedAdUnitListener.onAdClosed(RewardedAdUnit.this);
                break;
            case AD_LOADED:
                rewardedAdUnitListener.onAdLoaded(RewardedAdUnit.this);
                break;
            case AD_DISPLAYED:
                rewardedAdUnitListener.onAdDisplayed(RewardedAdUnit.this);
                break;
            case AD_CLICKED:
                rewardedAdUnitListener.onAdClicked(RewardedAdUnit.this);
                break;
            case USER_RECEIVED_PREBID_REWARD:
                rewardedAdUnitListener.onUserEarnedReward(RewardedAdUnit.this);
                break;
        }
    }

    @Override
    void notifyErrorListener(AdException exception) {
        if (rewardedAdUnitListener != null) {
            rewardedAdUnitListener.onAdFailed(RewardedAdUnit.this, exception);
        }
    }

}
