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

import org.prebid.mobile.eventhandlers.global.Constants;
import org.prebid.mobile.rendering.bidding.data.AdSize;
import org.prebid.mobile.rendering.bidding.data.bid.Bid;
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler;
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.utils.logger.OXLog;

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

    private final Context mApplicationContext;
    private final AdSize[] mAdSizes;
    private final String mGamAdUnitId;

    private PublisherAdViewWrapper mRequestBanner;
    private PublisherAdViewWrapper mOxbProxyBanner;
    private PublisherAdViewWrapper mEmbeddedBanner;
    private PublisherAdViewWrapper mRecycledBanner;

    private BannerEventListener mBannerEventListener;
    private Handler mAppEventHandler;

    private boolean mIsExpectingAppEvent;

    /**
     * @param context     activity or application context.
     * @param gamAdUnitId GAM AdUnitId.
     * @param adSizes     ad sizes for banner.
     */
    public GamBannerEventHandler(Context context, String gamAdUnitId, AdSize... adSizes) {
        mApplicationContext = context.getApplicationContext();
        mGamAdUnitId = gamAdUnitId;
        mAdSizes = adSizes;
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
                mBannerEventListener.onAdClosed();
                break;
            case FAILED:
                handleAdFailure(adEvent.getErrorCode());
                break;
            case CLICKED:
                mBannerEventListener.onAdClicked();
                break;
            case LOADED:
                primaryAdReceived();
                break;
        }
    }
    //endregion ==================== GAM AppEventsListener Implementation

    //region ==================== OX EventHandler Implementation
    @Override
    public AdSize[] getAdSizeArray() {
        if (mAdSizes == null) {
            return new AdSize[0];
        }

        return mAdSizes;
    }

    @Override
    public void setBannerEventListener(
        @NonNull
            BannerEventListener bannerViewListener) {
        mBannerEventListener = bannerViewListener;
    }

    @SuppressLint("MissingPermission")
    @Override
    public void requestAdWithBid(Bid bid) {
        mIsExpectingAppEvent = false;

        if (mRequestBanner != null) {
            OXLog.error(TAG, "requestAdWithBid: Failed. Request to primaryAdServer is in progress.");
            return;
        }

        if (mRecycledBanner != null) {
            mRequestBanner = mRecycledBanner;
            mRecycledBanner = null;
        }
        else {
            mRequestBanner = createPublisherAdView();
        }

        if (bid != null && bid.getPrice() > 0) {
            mIsExpectingAppEvent = true;
        }

        if (mRequestBanner == null) {
            handleAdFailure(Constants.ERROR_CODE_INTERNAL_ERROR);
            return;
        }

        mRequestBanner.setManualImpressionsEnabled(true);
        mRequestBanner.loadAd(bid);
    }

    @Override
    public void trackImpression() {
        if (mOxbProxyBanner != null) {
            mOxbProxyBanner.recordManualImpression();
        }
    }

    @Override
    public void destroy() {
        cancelTimer();
        destroyGamViews();
    }
    //endregion ==================== OX EventHandler Implementation

    private PublisherAdViewWrapper createPublisherAdView() {
        return PublisherAdViewWrapper.newInstance(mApplicationContext, mGamAdUnitId, this, mAdSizes);
    }

    private void primaryAdReceived() {
        if (mIsExpectingAppEvent) {
            if (mAppEventHandler != null) {
                OXLog.debug(TAG, "primaryAdReceived: AppEventTimer is not null. Skipping timer scheduling.");
                return;
            }

            scheduleTimer();
        }
        // RequestBanner is null when appEvent was handled before ad was loaded
        else if (mRequestBanner != null) {
            PublisherAdViewWrapper gamBannerView = mRequestBanner;
            mRequestBanner = null;
            recycleCurrentBanner();
            mEmbeddedBanner = gamBannerView;
            mBannerEventListener.onAdServerWin(getView(gamBannerView));
        }
    }

    private void handleAppEvent() {
        if (!mIsExpectingAppEvent) {
            OXLog.debug(TAG, "appEventDetected: Skipping event handling. App event is not expected");
            return;
        }

        cancelTimer();
        PublisherAdViewWrapper gamBannerView = mRequestBanner;
        mRequestBanner = null;
        mIsExpectingAppEvent = false;
        recycleCurrentBanner();
        mOxbProxyBanner = gamBannerView;
        mBannerEventListener.onOXBSdkWin();
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
        PublisherAdViewWrapper gamBannerView = mRequestBanner;
        mRequestBanner = null;
        recycleCurrentBanner();
        mEmbeddedBanner = gamBannerView;
        mIsExpectingAppEvent = false;
        mBannerEventListener.onAdServerWin(getView(gamBannerView));
    }

    private View getView(PublisherAdViewWrapper gamBannerView) {
        return gamBannerView != null ? gamBannerView.getView() : null;
    }

    private void handleAdFailure(int errorCode) {
        mRequestBanner = null;
        recycleCurrentBanner();

        switch (errorCode) {
            case Constants.ERROR_CODE_INTERNAL_ERROR:
                mBannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK encountered an internal error."));
                break;
            case Constants.ERROR_CODE_INVALID_REQUEST:
                mBannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - invalid request error."));
                break;
            case Constants.ERROR_CODE_NETWORK_ERROR:
                mBannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - network error."));
                break;
            case Constants.ERROR_CODE_NO_FILL:
                mBannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - no fill."));
                break;
            default:
                mBannerEventListener.onAdFailed(new AdException(AdException.THIRD_PARTY, "GAM SDK - failed with errorCode: " + errorCode));
        }
    }

    private void recycleCurrentBanner() {
        if (mEmbeddedBanner != null) {
            mRecycledBanner = mEmbeddedBanner;
            mEmbeddedBanner = null;
        }
        else if (mOxbProxyBanner != null) {
            mRecycledBanner = mOxbProxyBanner;
            mOxbProxyBanner = null;
            mRecycledBanner.setManualImpressionsEnabled(false);
        }
    }

    private void destroyGamViews() {
        if (mRequestBanner != null) {
            mRequestBanner.destroy();
        }
        if (mOxbProxyBanner != null) {
            mOxbProxyBanner.destroy();
        }
        if (mEmbeddedBanner != null) {
            mEmbeddedBanner.destroy();
        }
        if (mRecycledBanner != null) {
            mRecycledBanner.destroy();
        }
    }
}
