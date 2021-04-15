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

package org.prebid.mobile.rendering.bidding.parallel;

import android.content.Context;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler;
import org.prebid.mobile.rendering.bidding.interfaces.StandaloneRewardedVideoEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.RewardedAdUnitListener;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static org.prebid.mobile.rendering.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;

public class RewardedAdUnit extends BaseInterstitialAdUnit {
    private static final String TAG = RewardedAdUnit.class.getSimpleName();

    private final RewardedEventHandler mEventHandler;

    @Nullable
    private RewardedAdUnitListener mRewardedAdUnitListener;

    @Nullable
    private Object mUserReward;

    //region ==================== Listener implementation

    private final RewardedVideoEventListener mEventListener = new RewardedVideoEventListener() {
        @Override
        public void onOXBSdkWin() {
            if (isBidInvalid()) {
                changeOxbInterstitialAdUnitState(READY_FOR_LOAD);
                notifyErrorListener(new AdException(AdException.INTERNAL_ERROR, "WinnerBid is null when executing onOXBSdkWin."));
                return;
            }

            loadOxbAd();
        }

        @Override
        public void onAdServerWin(Object userReward) {
            mUserReward = userReward;
            changeOxbInterstitialAdUnitState(READY_TO_DISPLAY_GAM);
            notifyAdEventListener(AdListenerEvent.AD_LOADED);
        }

        @Override
        public void onAdFailed(AdException exception) {
            if (isBidInvalid()) {
                changeOxbInterstitialAdUnitState(READY_FOR_LOAD);
                notifyErrorListener(exception);
                return;
            }

            onOXBSdkWin();
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
            changeOxbInterstitialAdUnitState(READY_FOR_LOAD);
            notifyAdEventListener(AdListenerEvent.AD_DISPLAYED);
        }

        @Override
        public void onUserEarnedReward() {
            if (mRewardedAdUnitListener != null) {
                mRewardedAdUnitListener.onUserEarnedReward(RewardedAdUnit.this);
            }
        }
    };
    //endregion ==================== Listener implementation

    public RewardedAdUnit(Context context, String configId, RewardedEventHandler eventHandler) {
        super(context);
        mEventHandler = eventHandler;
        mEventHandler.setRewardedEventListener(mEventListener);

        AdConfiguration adUnitConfiguration = new AdConfiguration();
        adUnitConfiguration.setConfigId(configId);
        adUnitConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.VAST);
        adUnitConfiguration.setRewarded(true);

        init(adUnitConfiguration);
    }

    public RewardedAdUnit(Context context, String configId) {
        this(context, configId, new StandaloneRewardedVideoEventHandler());
    }

    @Override
    public void loadAd() {
        super.loadAd();
        mUserReward = null;
    }

    @Override
    public void destroy() {
        super.destroy();
        if (mEventHandler != null) {
            mEventHandler.destroy();
        }
    }

    //region ==================== getters and setters
    public void setRewardedAdUnitListener(
        @Nullable
            RewardedAdUnitListener rewardedAdUnitListener) {
        mRewardedAdUnitListener = rewardedAdUnitListener;
    }

    @Nullable
    public Object getUserReward() {
        return mUserReward;
    }
    //endregion ==================== getters and setters

    @Override
    void requestAdWithBid(
        @Nullable
            Bid bid) {
        mEventHandler.requestAdWithBid(bid);
    }

    @Override
    void showGamAd() {
        mEventHandler.show();
    }

    @Override
    void notifyAdEventListener(AdListenerEvent adListenerEvent) {
        if (mRewardedAdUnitListener == null) {
            OXLog.debug(TAG, "notifyAdEventListener: Failed. AdUnitListener is null. Passed listener event: " + adListenerEvent);
            return;
        }

        switch (adListenerEvent) {
            case AD_CLOSE:
                mRewardedAdUnitListener.onAdClosed(RewardedAdUnit.this);
                break;
            case AD_LOADED:
                mRewardedAdUnitListener.onAdLoaded(RewardedAdUnit.this);
                break;
            case AD_DISPLAYED:
                mRewardedAdUnitListener.onAdDisplayed(RewardedAdUnit.this);
                break;
            case AD_CLICKED:
                mRewardedAdUnitListener.onAdClicked(RewardedAdUnit.this);
                break;
            case USER_RECEIVED_PREBID_REWARD:
                mRewardedAdUnitListener.onUserEarnedReward(RewardedAdUnit.this);
                break;
        }
    }

    @Override
    void notifyErrorListener(AdException exception) {
        if (mRewardedAdUnitListener != null) {
            mRewardedAdUnitListener.onAdFailed(RewardedAdUnit.this, exception);
        }
    }
}
