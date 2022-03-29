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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd;
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback;
import com.google.android.gms.ads.admanager.AppEventListener;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.eventhandlers.utils.GamUtils;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

import static org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of PublisherInterstitialAd from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherInterstitialAd / PublisherInterstitialAdWrapper instance creation
 */
public class PublisherInterstitialAdWrapper extends FullScreenContentCallback implements AppEventListener {

    private static final String TAG = PublisherInterstitialAdWrapper.class.getSimpleName();

    private AdManagerInterstitialAd interstitialAd;

    private final WeakReference<Activity> activityWeakReference;
    private final String adUnitId;
    private final GamAdEventListener listener;

    private final AdManagerInterstitialAdLoadCallback adLoadCallback = new AdManagerInterstitialAdLoadCallback() {
        @Override
        public void onAdLoaded(
                @NonNull AdManagerInterstitialAd adManagerInterstitialAd
        ) {
            listener.onEvent(AdEvent.LOADED);

            interstitialAd = adManagerInterstitialAd;
            interstitialAd.setFullScreenContentCallback(PublisherInterstitialAdWrapper.this);
            interstitialAd.setAppEventListener(PublisherInterstitialAdWrapper.this);
        }

        @Override
        public void onAdFailedToLoad(
            @NonNull
                LoadAdError loadAdError) {
            interstitialAd = null;
            notifyErrorListener(loadAdError.getCode());
        }
    };

    private PublisherInterstitialAdWrapper(Activity activity, String gamAdUnitId, GamAdEventListener eventListener) {
        if (activity == null) {
            throw new IllegalArgumentException("Activity can't be null.");
        }

        listener = eventListener;
        activityWeakReference = new WeakReference<>(activity);

        adUnitId = gamAdUnitId;
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
            listener.onEvent(AdEvent.APP_EVENT_RECEIVED);
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== GAM FullScreenContentCallback Implementation

    @Override
    public void onAdFailedToShowFullScreenContent(
        @NonNull
            AdError adError) {

        interstitialAd = null;
        notifyErrorListener(adError.getCode());
    }

    @Override
    public void onAdShowedFullScreenContent() {
        listener.onEvent(AdEvent.DISPLAYED);
    }

    @Override
    public void onAdDismissedFullScreenContent() {
        listener.onEvent(AdEvent.CLOSED);
    }

    //endregion ==================== GAM FullScreenContentCallback Implementation

    public boolean isLoaded() {
        try {
            return interstitialAd != null;
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
        return false;
    }

    public void show() {
        final Activity activity = activityWeakReference.get();

        if (activity == null) {
            LogUtil.error(TAG, "show: Failed. Activity is null.");
            return;
        }

        if (interstitialAd == null) {
            LogUtil.error(TAG, "show: Failure. Interstitial ad is null.");
            return;
        }

        try {
            interstitialAd.show(activity);
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void loadAd(Bid bid) {
        interstitialAd = null;
        try {
            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();
            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                GamUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            AdManagerInterstitialAd.load(activityWeakReference.get(), adUnitId, adRequest, adLoadCallback);
        }
        catch (Throwable throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    private void notifyErrorListener(int code) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(code);

        listener.onEvent(adEvent);
    }
}
