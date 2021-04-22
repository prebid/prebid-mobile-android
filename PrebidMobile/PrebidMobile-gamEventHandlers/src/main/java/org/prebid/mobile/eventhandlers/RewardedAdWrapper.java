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

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import org.prebid.mobile.eventhandlers.utils.GamUtils;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of RewardedAd from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on RewardedAd / RewardedAdWrapper instance creation
 */
public class RewardedAdWrapper extends FullScreenContentCallback
    implements OnUserEarnedRewardListener {

    private static final String TAG = RewardedAdWrapper.class.getSimpleName();
    public static final String KEY_METADATA = "AdTitle";

    private final WeakReference<Context> mContextWeakReference;
    private final GamAdEventListener mListener;
    private final String mAdUnitId;

    private RewardedAd mRewardedAd;

    private final RewardedAdLoadCallback mRewardedAdLoadCallback = new RewardedAdLoadCallback() {
        @Override
        public void onAdLoaded(
            @NonNull
                RewardedAd rewardedAd) {
            mRewardedAd = rewardedAd;
            mRewardedAd.setFullScreenContentCallback(RewardedAdWrapper.this);
            mListener.onEvent(AdEvent.LOADED);

            if (metadataContainsAdEvent()) {
                mListener.onEvent(AdEvent.APP_EVENT_RECEIVED);
            }
        }

        @Override
        public void onAdFailedToLoad(
            @NonNull
                LoadAdError loadAdError) {
            mRewardedAd = null;
            notifyErrorListener(loadAdError.getCode());
        }
    };

    private RewardedAdWrapper(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        if (context == null) {
            throw new IllegalArgumentException("Context can't be null.");
        }

        mListener = eventListener;
        mContextWeakReference = new WeakReference<>(context);
        mAdUnitId = gamAdUnitId;
    }

    @Nullable
    static RewardedAdWrapper newInstance(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        try {
            return new RewardedAdWrapper(context, gamAdUnitId, eventListener);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return null;
    }

    //region ==================== GAM FullScreenContentCallback Implementation

    @Override
    public void onAdFailedToShowFullScreenContent(
        @NonNull
            AdError adError) {
        mRewardedAd = null;
        notifyErrorListener(adError.getCode());
    }

    @Override
    public void onAdShowedFullScreenContent() {
        mListener.onEvent(AdEvent.DISPLAYED);
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        mListener.onEvent(AdEvent.CLOSED);
    }

    @Override
    public void onUserEarnedReward(
        @NonNull
            RewardItem rewardItem) {
        mListener.onEvent(AdEvent.REWARD_EARNED);
        mListener.onEvent(AdEvent.CLOSED);
    }
    //endregion ==================== GAM FullScreenContentCallback Implementation

    public void loadAd(Bid bid) {
        try {
            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                GamUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            RewardedAd.load(mContextWeakReference.get(), mAdUnitId, adRequest, mRewardedAdLoadCallback);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public boolean isLoaded() {
        try {
            return mRewardedAd != null;
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }

        return false;
    }

    public void show(Activity activity) {
        if (mRewardedAd == null) {
            OXLog.error(TAG, "show: Failed! Rewarded ad is null.");
            return;
        }

        try {
            mRewardedAd.show(activity, this);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public Object getRewardItem() {
        return mRewardedAd != null ? mRewardedAd.getRewardItem() : null;
    }

    private void notifyErrorListener(int errorCode) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(errorCode);

        mListener.onEvent(adEvent);
    }

    private boolean metadataContainsAdEvent() {
        try {
            if (mRewardedAd == null) {
                OXLog.debug(TAG, "metadataContainsAdEvent: Failed to process. RewardedAd is null.");
                return false;
            }

            final Bundle adMetadata = mRewardedAd.getAdMetadata();
            return APP_EVENT.equals(adMetadata.getString(KEY_METADATA));
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return false;
    }
}
