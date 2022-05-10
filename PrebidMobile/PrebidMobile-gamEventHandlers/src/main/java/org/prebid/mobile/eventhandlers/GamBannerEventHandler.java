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
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import androidx.annotation.NonNull;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;

/**
 * This class is compatible with Prebid Rendering SDK v1.10.
 * This class implements the communication between the Prebid Rendering SDK and the GAM SDK for a given ad
 * unit. It implements the Prebid Rendering SDK EventHandler interface. Prebid Rendering SDK notifies (using EventHandler interface)
 * to make a request to GAM SDK and pass the targeting parameters. This class also creates the GAM's
 * PublisherAdViews, initializes them and listens for the callback methods. And pass the GAM ad event to
 * Prebid Rendering SDK via BannerEventListener.
 */
public class GamBannerEventHandler implements BannerEventHandler, GamAdEventListener {

    private static final String TAG = GamBannerEventHandler.class.getSimpleName();

    private static final long TIMEOUT_APP_EVENT_MS = 600;

    private final Context applicationContext;
    private final AdSize[] adSizes;
    private final String gamAdUnitId;

    private PublisherAdViewWrapper requestBanner;
    private PublisherAdViewWrapper proxyBanner;
    private PublisherAdViewWrapper embeddedBanner;
    private PublisherAdViewWrapper recycledBanner;

    private BannerEventListener bannerEventListener;
    private Handler appEventHandler;

    private boolean isExpectingAppEvent;

    /**
     * @param context     activity or application context.
     * @param gamAdUnitId GAM AdUnitId.
     * @param adSizes     ad sizes for banner.
     */
    public GamBannerEventHandler(
            Context context,
            String gamAdUnitId,
            AdSize... adSizes
    ) {
        applicationContext = context.getApplicationContext();
        this.gamAdUnitId = gamAdUnitId;
        this.adSizes = adSizes;
    }

    public static AdSize[] convertGamAdSize(com.google.android.gms.ads.AdSize... sizes) {
        if (sizes == null) {
            return new AdSize[0];
        }
        AdSize[] adSizes = new AdSize[sizes.length];
        for (int i = 0; i < sizes.length; i++) {
            com.google.android.gms.ads.AdSize gamAdSize = sizes[i];
            adSizes[i] = new AdSize(gamAdSize.getWidth(), gamAdSize.getHeight());
        }

        return adSizes;
    }

    //region ==================== GAM AppEventsListener Implementation
    @Override
    public void onEvent(AdEvent adEvent) {
        switch (adEvent) {
            case APP_EVENT_RECEIVED:
                handleAppEvent();
                break;
            case CLOSED:
                bannerEventListener.onAdClosed();
                break;
            case FAILED:
                handleAdFailure(adEvent.getErrorCode());
                break;
            case CLICKED:
                bannerEventListener.onAdClicked();
                break;
            case LOADED:
                primaryAdReceived();
                break;
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== EventHandler Implementation
    @Override
    public AdSize[] getAdSizeArray() {
        if (adSizes == null) {
            return new AdSize[0];
        }

        return adSizes;
    }

    @Override
    public void setBannerEventListener(
        @NonNull
            BannerEventListener bannerViewListener) {
        bannerEventListener = bannerViewListener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestAdWithBid(Bid bid) {
        isExpectingAppEvent = false;

        if (requestBanner != null) {
            LogUtil.error(TAG, "requestAdWithBid: Failed. Request to primaryAdServer is in progress.");
            return;
        }

        if (recycledBanner != null) {
            requestBanner = recycledBanner;
            recycledBanner = null;
        } else {
            requestBanner = createPublisherAdView();
        }

        if (bid != null && bid.getPrice() > 0) {
            isExpectingAppEvent = true;
        }

        if (requestBanner == null) {
            handleAdFailure(Constants.ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        requestBanner.setManualImpressionsEnabled(true);
        requestBanner.loadAd(bid);
    }

    @Override
    public void trackImpression() {
        if (proxyBanner != null) {
            proxyBanner.recordManualImpression();
        }
    }

    @Override
    public void destroy() {
        cancelTimer();
        destroyGamViews();
    }
    //endregion ==================== EventHandler Implementation

    private PublisherAdViewWrapper createPublisherAdView() {
        return PublisherAdViewWrapper.newInstance(applicationContext, gamAdUnitId, this, adSizes);
    }

    private void primaryAdReceived() {
        if (isExpectingAppEvent) {
            if (appEventHandler != null) {
                LogUtil.debug(TAG, "primaryAdReceived: AppEventTimer is not null. Skipping timer scheduling.");
                return;
            }

            scheduleTimer();
        }
        // RequestBanner is null when appEvent was handled before ad was loaded
        else if (requestBanner != null) {
            PublisherAdViewWrapper gamBannerView = requestBanner;
            requestBanner = null;
            recycleCurrentBanner();
            embeddedBanner = gamBannerView;
            bannerEventListener.onAdServerWin(getView(gamBannerView));
        }
    }

    private void handleAppEvent() {
        if (!isExpectingAppEvent) {
            LogUtil.debug(TAG, "appEventDetected: Skipping event handling. App event is not expected");
            return;
        }

        cancelTimer();
        PublisherAdViewWrapper gamBannerView = requestBanner;
        requestBanner = null;
        isExpectingAppEvent = false;
        recycleCurrentBanner();
        proxyBanner = gamBannerView;
        bannerEventListener.onPrebidSdkWin();
    }

    private void scheduleTimer() {
        cancelTimer();

        appEventHandler = new Handler(Looper.getMainLooper());
        appEventHandler.postDelayed(this::handleAppEventTimeout, TIMEOUT_APP_EVENT_MS);
    }

    private void cancelTimer() {
        if (appEventHandler != null) {
            appEventHandler.removeCallbacksAndMessages(null);
        }
        appEventHandler = null;
    }

    private void handleAppEventTimeout() {
        cancelTimer();
        PublisherAdViewWrapper gamBannerView = requestBanner;
        requestBanner = null;
        recycleCurrentBanner();
        embeddedBanner = gamBannerView;
        isExpectingAppEvent = false;
        bannerEventListener.onAdServerWin(getView(gamBannerView));
    }

    private View getView(PublisherAdViewWrapper gamBannerView) {
        return gamBannerView != null ? gamBannerView.getView() : null;
    }

    private void handleAdFailure(int errorCode) {
        requestBanner = null;
        recycleCurrentBanner();

        switch (errorCode) {
            case Constants.ERROR_CODE_INTERNAL_ERROR:
                bannerEventListener.onAdFailed(new AdException(
                        AdException.THIRD_PARTY,
                        "GAM SDK encountered an internal error."
                ));
                break;
            case Constants.ERROR_CODE_INVALID_REQUEST:
                bannerEventListener.onAdFailed(new AdException(
                        AdException.THIRD_PARTY,
                        "GAM SDK - invalid request error."
                ));
                break;
            case Constants.ERROR_CODE_NETWORK_ERROR:
                bannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - network error."));
                break;
            case Constants.ERROR_CODE_NO_FILL:
                bannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - no fill."));
                break;
            default:
                bannerEventListener.onAdFailed(new AdException(
                        AdException.THIRD_PARTY,
                        "GAM SDK - failed with errorCode: " + errorCode
                ));
        }
    }

    private void recycleCurrentBanner() {
        if (embeddedBanner != null) {
            recycledBanner = embeddedBanner;
            embeddedBanner = null;
        } else if (proxyBanner != null) {
            recycledBanner = proxyBanner;
            proxyBanner = null;
            recycledBanner.setManualImpressionsEnabled(false);
        }
    }

    private void destroyGamViews() {
        if (requestBanner != null) {
            requestBanner.destroy();
        }
        if (proxyBanner != null) {
            proxyBanner.destroy();
        }
        if (embeddedBanner != null) {
            embeddedBanner.destroy();
        }
        if (recycledBanner != null) {
            recycledBanner.destroy();
        }
    }
}
