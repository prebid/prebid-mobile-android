package com.openx.apollo.bidding.parallel;

import android.content.Context;

import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.interfaces.RewardedEventHandler;
import com.openx.apollo.bidding.interfaces.StandaloneRewardedVideoEventHandler;
import com.openx.apollo.bidding.listeners.RewardedAdUnitListener;
import com.openx.apollo.bidding.listeners.RewardedVideoEventListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.utils.logger.OXLog;

import static com.openx.apollo.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_FOR_LOAD;
import static com.openx.apollo.bidding.parallel.BaseInterstitialAdUnit.InterstitialAdUnitState.READY_TO_DISPLAY_GAM;

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
            case USER_RECEIVED_OPENX_REWARD:
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
