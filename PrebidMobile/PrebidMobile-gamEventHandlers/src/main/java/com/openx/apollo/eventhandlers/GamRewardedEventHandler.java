package com.openx.apollo.eventhandlers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.openx.apollo.bidding.data.bid.Bid;
import com.openx.apollo.bidding.interfaces.RewardedEventHandler;
import com.openx.apollo.bidding.listeners.RewardedVideoEventListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.eventhandlers.global.Constants;
import com.openx.apollo.utils.logger.OXLog;

import java.lang.ref.WeakReference;

public class GamRewardedEventHandler implements RewardedEventHandler, GamAdEventListener {
    private static final String TAG = GamRewardedEventHandler.class.getSimpleName();
    private static final long TIMEOUT_APP_EVENT_MS = 600;

    private RewardedAdWrapper mRewardedAd;

    private final WeakReference<Activity> mActivityWeakReference;
    private final String mGamAdUnitId;

    private RewardedVideoEventListener mListener;
    private Handler mAppEventHandler;

    private boolean mIsExpectingAppEvent;
    private boolean mDidNotifiedBidWin;

    public GamRewardedEventHandler(Activity activity, String gamAdUnitId) {
        mActivityWeakReference = new WeakReference<>(activity);
        mGamAdUnitId = gamAdUnitId;
    }

    //region ==================== EventListener Implementation
    @Override
    public void onEvent(AdEvent adEvent) {
        switch (adEvent) {
            case APP_EVENT_RECEIVED:
                handleAppEvent();
                break;
            case LOADED:
                primaryAdReceived();
                break;
            case DISPLAYED:
                mListener.onAdDisplayed();
                break;
            case CLOSED:
                mListener.onAdClosed();
                break;
            case FAILED:
                notifyErrorListener(adEvent.getErrorCode());
                break;
            case REWARD_EARNED:
                mListener.onUserEarnedReward();
                break;
        }
    }
    //endregion ==================== EventListener Implementation

    //region ==================== OX EventHandler Implementation
    @Override
    public void setRewardedEventListener(
        @NonNull
            RewardedVideoEventListener listener) {
        mListener = listener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestAdWithBid(
        @Nullable
            Bid bid) {
        mIsExpectingAppEvent = false;
        mDidNotifiedBidWin = false;

        initPublisherRewardedAd();

        if (bid != null && bid.getPrice() > 0) {
            mIsExpectingAppEvent = true;
        }

        if (mRewardedAd == null) {
            notifyErrorListener(Constants.ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        mRewardedAd.loadAd(bid);
    }

    @Override
    public void show() {
        if (mRewardedAd != null && mRewardedAd.isLoaded()) {
            mRewardedAd.show(mActivityWeakReference.get());
        }
        else {
            mListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - failed to display ad."));
        }
    }

    @Override
    public void trackImpression() {

    }

    @Override
    public void destroy() {
        cancelTimer();
    }
    //endregion ==================== OX EventHandler Implementation

    private void initPublisherRewardedAd() {
        mRewardedAd = RewardedAdWrapper.newInstance(mActivityWeakReference.get(), mGamAdUnitId, this);
    }

    private void primaryAdReceived() {
        if (mIsExpectingAppEvent) {
            if (mAppEventHandler != null) {
                OXLog.debug(TAG, "primaryAdReceived: AppEventTimer is not null. Skipping timer scheduling.");
                return;
            }

            scheduleTimer();
        }
        else if (!mDidNotifiedBidWin) {
            mListener.onAdServerWin(getRewardItem());
        }
    }

    private void handleAppEvent() {
        if (!mIsExpectingAppEvent) {
            OXLog.debug(TAG, "appEventDetected: Skipping event handling. App event is not expected");
            return;
        }

        cancelTimer();
        mIsExpectingAppEvent = false;
        mDidNotifiedBidWin = true;
        mListener.onOXBSdkWin();
    }

    private void scheduleTimer() {
        cancelTimer();

        mAppEventHandler = new Handler(Looper.getMainLooper());
        mAppEventHandler.postDelayed(this::handleAppEventTimeout, TIMEOUT_APP_EVENT_MS);
    }

    private void cancelTimer() {
        if (mAppEventHandler != null) {
            mAppEventHandler.removeCallbacksAndMessages(null);
        }
        mAppEventHandler = null;
    }

    private void handleAppEventTimeout() {
        cancelTimer();
        mIsExpectingAppEvent = false;
        mListener.onAdServerWin(getRewardItem());
    }

    private Object getRewardItem() {
        return mRewardedAd != null ? mRewardedAd.getRewardItem() : null;
    }

    private void notifyErrorListener(int errorCode) {
        switch (errorCode) {
            case Constants.ERROR_CODE_INTERNAL_ERROR:
                mListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK encountered an internal error."));
                break;
            case Constants.ERROR_CODE_INVALID_REQUEST:
                mListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - invalid request error."));
                break;
            case Constants.ERROR_CODE_NETWORK_ERROR:
                mListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - network error."));
                break;
            case Constants.ERROR_CODE_NO_FILL:
                mListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - no fill."));
                break;
            default:
                mListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - failed with errorCode: " + errorCode));
        }
    }
}
