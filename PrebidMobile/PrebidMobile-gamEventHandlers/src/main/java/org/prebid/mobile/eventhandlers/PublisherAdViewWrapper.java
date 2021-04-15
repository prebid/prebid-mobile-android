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

import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.doubleclick.AppEventListener;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.display.ReflectionUtils;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.util.HashMap;
import java.util.Map;

/**
 * This class is responsible for wrapping usage of PublisherAdView from GAM SDK.
 * To achieve safe integration between various GAM SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherAdView / PublisherAdViewWrapper instance creation
 */
public class PublisherAdViewWrapper extends AdListener implements AppEventListener {

    private static final String TAG = PublisherAdViewWrapper.class.getSimpleName();

    private final PublisherAdView mPublisherAdView;
    private final GamAdEventListener mListener;

    private PublisherAdViewWrapper(Context context, String gamAdUnit,
                                   GamAdEventListener eventListener, AdSize... adSizes) {
        mListener = eventListener;

        mPublisherAdView = new PublisherAdView(context);
        mPublisherAdView.setAdSizes(mapToGamAdSizes(adSizes));
        mPublisherAdView.setAdUnitId(gamAdUnit);
        mPublisherAdView.setAdListener(this);
        mPublisherAdView.setAppEventListener(this);
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
    public void onAppEvent(String name, String info) {
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
    public void onAdFailedToLoad(int errorCode) {
        final AdEvent adEvent = AdEvent.FAILED;
        adEvent.setErrorCode(errorCode);

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
            PublisherAdRequest adRequest = new PublisherAdRequest.Builder().build();

            if (bid != null) {
                Map<String, String> targetingMap = new HashMap<>(bid.getPrebid().getTargeting());
                ReflectionUtils.handleGamCustomTargetingUpdate(adRequest, targetingMap);
            }

            mPublisherAdView.loadAd(adRequest);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void setManualImpressionsEnabled(boolean enabled) {
        try {
            mPublisherAdView.setManualImpressionsEnabled(enabled);
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void recordManualImpression() {
        try {
            mPublisherAdView.recordManualImpression();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public void destroy() {
        try {
            mPublisherAdView.destroy();
        }
        catch (Throwable throwable) {
            OXLog.error(TAG, Log.getStackTraceString(throwable));
        }
    }

    public View getView() {
        return mPublisherAdView;
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
