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
import android.view.View;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;
import com.google.android.gms.ads.admanager.AppEventListener;

import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.eventhandlers.utils.GamUtils;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.HashMap;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * This class is responsible for wrapping usage of PublisherAdView from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherAdView / PublisherAdViewWrapper instance creation
 */
public class PublisherAdViewWrapper extends AdListener implements AppEventListener {

    private static final String TAG = PublisherAdViewWrapper.class.getSimpleName();

    private final AdManagerAdView mAdView;
    private final GamAdEventListener mListener;

    private PublisherAdViewWrapper(Context context, String gamAdUnit,
                                   GamAdEventListener eventListener, AdSize... adSizes) {
        mListener = eventListener;

        mAdView = new AdManagerAdView(context);
        mAdView.setAdSizes(mapToGamAdSizes(adSizes));
        mAdView.setAdUnitId(gamAdUnit);
        mAdView.setAdListener(this);
        mAdView.setAppEventListener(this);
        mAdView.setAdListener(this);
    }

    @Nullable
    static PublisherAdViewWrapper newInstance(Context context, String gamAdUnitId,
                                              GamAdEventListener eventListener, AdSize... adSizes) {
        try {
            return new PublisherAdViewWrapper(context,
                                              gamAdUnitId,
                                              eventListener,
                                              adSizes);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
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
        if (Constants.APP_EVENT.equals(name)) {
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
    public void onAdFailedToLoad(
        @NonNull
            LoadAdError loadAdError) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(loadAdError.getCode());

        mListener.onEvent(adEvent);
    }

    @Override
    public void onAdOpened() {
        mListener.onEvent(AdEvent.CLICKED);
    }

    @Override
    public void onAdLoaded() {
        mListener.onEvent(AdEvent.LOADED);
    }
    //endregion ==================== GAM AdEventListener Implementation

    public void loadAd(Bid bid) {
        try {
            AdManagerAdRequest adRequest = new AdManagerAdRequest.Builder().build();

            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                GamUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            mAdView.loadAd(adRequest);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void setManualImpressionsEnabled(boolean enabled) {
        try {
            mAdView.setManualImpressionsEnabled(enabled);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void recordManualImpression() {
        try {
            mAdView.recordManualImpression();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void destroy() {
        try {
            mAdView.destroy();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public View getView() {
        return mAdView;
    }

    private com.google.android.gms.ads.AdSize[] mapToGamAdSizes(AdSize[] adSizes) {
        if (adSizes == null) {
            return new com.google.android.gms.ads.AdSize[0];
        }

        final com.google.android.gms.ads.AdSize[] gamAdSizeArray = new com.google.android.gms.ads.AdSize[adSizes.length];
        for (int i = 0; i < adSizes.length; i++) {
            final AdSize prebidAdSize = adSizes[i];
            gamAdSizeArray[i] = new com.google.android.gms.ads.AdSize(prebidAdSize.width, prebidAdSize.height);
        }

        return gamAdSizeArray;
    }
}
