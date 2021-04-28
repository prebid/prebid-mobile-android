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
import android.util.Log;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.admanager.AppEventListener;

import org.prebid.mobile.eventhandlers.utils.GamUtils;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import static org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of PublisherInterstitialAd from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherInterstitialAd / PublisherInterstitialAdWrapper instance creation
 */
public class PublisherInterstitialAdWrapper extends FullScreenContentCallback
    implements AppEventListener {

    private static final String TAG = PublisherInterstitialAdWrapper.class.getSimpleName();

    private AdManagerInterstitialAd mInterstitialAd;

    private final WeakReference<Activity> mActivityWeakReference;
    private final String mAdUnitId;
    private final GamAdEventListener mListener;

    private final AdManagerInterstitialAdLoadCallback mAdLoadCallback = new AdManagerInterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(
            @NonNull
                AdManagerInterstitialAd adManagerInterstitialAd) {
            mListener.onEvent(AdEvent.LOADED);

            mInterstitialAd = adManagerInterstitialAd;
            mInterstitialAd.setFullScreenContentCallback(PublisherInterstitialAdWrapper.this);
            mInterstitialAd.setAppEventListener(PublisherInterstitialAdWrapper.this);
        }

        @Override
        public void onAdFailedToLoad(
            @NonNull
                LoadAdError loadAdError) {
            mInterstitialAd = null;
            notifyErrorListener(loadAdError.getCode());
        }
    };

    private PublisherInterstitialAdWrapper(Activity activity, String gamAdUnitId, GamAdEventListener eventListener) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity can't be null.");
        }

        mListener = eventListener;
        mActivityWeakReference = new WeakReference<>(activity);

        mAdUnitId = gamAdUnitId;
    }

    @Nullable
    static PublisherInterstitialAdWrapper newInstance(Activity activity, String gamAdUnitId, GamAdEventListener eventListener) {
        try {
            return new PublisherInterstitialAdWrapper(activity, gamAdUnitId, eventListener);
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
        return null;
    }

    //region ==================== GAM AppEventsListener Implementation
    @Override
    public void onAppEvent(
        @NonNull
            String name,
        @NonNull
            String info) {
        if (APP_EVENT.equals(name)) {
            mListener.onEvent(AdEvent.APP_EVENT_RECEIVED);
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== GAM FullScreenContentCallback Implementation

    @Override
    public void onAdFailedToShowFullScreenContent(
        @NonNull
            AdError adError) {

        mInterstitialAd = null;
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

    //endregion ==================== GAM FullScreenContentCallback Implementation

    public boolean isLoaded() {
        try {
            return mInterstitialAd != null;
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
        return false;
    }

    public void show() {
        final Activity activity = mActivityWeakReference.get();

        if (activity == null) {
            LogUtil.error(TAG, "show: Failed. Activity is null.");
            return;
        }

        if (mInterstitialAd == null) {
            LogUtil.error(TAG, "show: Failure. Interstitial ad is null.");
            return;
        }

        try {
            mInterstitialAd.show(activity);
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void loadAd(Bid bid) {
        mInterstitialAd = null;
        try {
            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                GamUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            AdManagerInterstitialAd.load(mActivityWeakReference.get(), mAdUnitId, adRequest, mAdLoadCallback);
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    private void notifyErrorListener(int code) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(code);

        mListener.onEvent(adEvent);
    }
}
