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

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;

import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.display.ReflectionUtils;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.HashMap;
import java.util.Map;

import static org.prebid.mobile.eventhandlers.global.Constants.APP_EVENT;

/**
 * This class is responsible for wrapping usage of PublisherInterstitialAd from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherInterstitialAd / PublisherInterstitialAdWrapper instance creation
 */
public class PublisherInterstitialAdWrapper extends AdListener implements AppEventListener {

    private static final String TAG = PublisherInterstitialAdWrapper.class.getSimpleName();

    private final PublisherInterstitialAd mRequestInterstitial;
    private final GamAdEventListener mListener;

    private PublisherInterstitialAdWrapper(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        mListener = eventListener;

        mRequestInterstitial = new PublisherInterstitialAd(context.getApplicationContext());
        mRequestInterstitial.setAdUnitId(gamAdUnitId);
        mRequestInterstitial.setAdListener(this);
        mRequestInterstitial.setAppEventListener(this);
    }

    @Nullable
    static PublisherInterstitialAdWrapper newInstance(Context context, String gamAdUnitId, GamAdEventListener eventListener) {
        try {
            return new PublisherInterstitialAdWrapper(context, gamAdUnitId, eventListener);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return null;
    }

    //region ==================== GAM AppEventsListener Implementation
    @Override
    public void onAppEvent(String name, String info) {
        if (APP_EVENT.equals(name)) {
            mListener.onEvent(AdEvent.APP_EVENT_RECEIVED);
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== GAM AdEventListener Implementation
    @Override
    public void onAdClosed() {
        mListener.onEvent(AdEvent.CLOSED);
    }

    @Override
    public void onAdFailedToLoad(int errorCode) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(errorCode);

        mListener.onEvent(adEvent);
    }

    @Override
    public void onAdOpened() {
        mListener.onEvent(AdEvent.DISPLAYED);
    }

    @Override
    public void onAdClicked() {
        mListener.onEvent(AdEvent.CLICKED);
    }

    @Override
    public void onAdLoaded() {
        mListener.onEvent(AdEvent.LOADED);
    }
    //endregion ==================== GAM AdEventListener Implementation

    public boolean isLoaded() {
        try {
            return mRequestInterstitial.isLoaded();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
        return false;
    }

    public void show() {
        try {
            mRequestInterstitial.show();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void loadAd(Bid bid) {
        try {
            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();
            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                ReflectionUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            mRequestInterstitial.loadAd(adRequest);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }
}
