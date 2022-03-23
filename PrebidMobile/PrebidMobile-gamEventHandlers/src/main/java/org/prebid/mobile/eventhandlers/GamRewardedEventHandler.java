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

package org.prebid.mobile.eventhandlers;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Handler;
import android.os.Looper;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener;
import org.prebid.mobile.rendering.errors.AdException;

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

    //region ==================== EventHandler Implementation
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
    //endregion ==================== EventHandler Implementation

    private void initPublisherRewardedAd() {
        mRewardedAd = RewardedAdWrapper.newInstance(mActivityWeakReference.get(), mGamAdUnitId, this);
    }

    private void primaryAdReceived() {
        if (mIsExpectingAppEvent) {
            if (mAppEventHandler != null) {
                LogUtil.debug(TAG, "primaryAdReceived: AppEventTimer is not null. Skipping timer scheduling.");
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
            LogUtil.debug(TAG, "appEventDetected: Skipping event handling. App event is not expected");
            return;
        }

        cancelTimer();
        mIsExpectingAppEvent = false;
        mDidNotifiedBidWin = true;
        mListener.onPrebidSdkWin();
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
