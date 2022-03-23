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

package org.prebid.mobile.rendering.bidding.display;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.networking.WinNotifier;
import org.prebid.mobile.rendering.utils.broadcast.local.EventForwardingLocalBroadcastReceiver;
import org.prebid.mobile.rendering.utils.constants.IntentActions;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.video.VideoViewListener;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

public class DisplayView extends FrameLayout {
    private final static String TAG = DisplayView.class.getSimpleName();
    private static final String CONTENT_DESCRIPTION_AD_VIEW = "adView";

    private AdUnitConfiguration mAdUnitConfiguration;
    private DisplayViewListener mDisplayViewListener;
    private InterstitialManager mInterstitialManager;
    private AdViewManager mAdViewManager;
    private VideoView mVideoView;

    private EventForwardingLocalBroadcastReceiver mEventForwardingReceiver;
    private final EventForwardingLocalBroadcastReceiver.EventForwardingBroadcastListener mBroadcastListener = this::handleBroadcastAction;

    private final AdViewManagerListener mAdViewManagerListener = new AdViewManagerListener() {
        @Override
        public void adLoaded(AdDetails adDetails) {
            // for banner mAdViewManager.show() will be called automatically
            notifyListenerLoaded();
        }

        @Override
        public void viewReadyForImmediateDisplay(View creative) {
            removeAllViews();
            creative.setContentDescription(CONTENT_DESCRIPTION_AD_VIEW);
            addView(creative);
            notifyListenerDisplayed();
        }

        @Override
        public void failedToLoad(AdException error) {
            notifyListenerError(error);
        }

        @Override
        public void creativeClicked(String url) {
            notifyListenerClicked();
        }

        @Override
        public void creativeInterstitialClosed() {
            notifyListenerClose();
        }

        @Override
        public void creativeCollapsed() {
            notifyListenerClose();
        }
    };

    private final VideoViewListener mVideoViewListener = new VideoViewListener() {
        @Override
        public void onLoaded(
            @NonNull
                VideoView videoAdView, AdDetails adDetails) {
            videoAdView.setContentDescription(CONTENT_DESCRIPTION_AD_VIEW);
            notifyListenerLoaded();
        }

        @Override
        public void onLoadFailed(
            @NonNull
                VideoView videoAdView, AdException error) {
            notifyListenerError(error);
        }

        @Override
        public void onDisplayed(
            @NonNull
                VideoView videoAdView) {
            notifyListenerDisplayed();
        }

        @Override
        public void onClickThroughOpened(
            @NonNull
                VideoView videoAdView) {
            notifyListenerClicked();
        }

        @Override
        public void onClickThroughClosed(
            @NonNull
                VideoView videoAdView) {
            notifyListenerClose();
        }
    };

    public DisplayView(
            @NonNull
                    Context context,
            DisplayViewListener listener,
            @NonNull
                    AdUnitConfiguration adUnitConfiguration,
            @NonNull
                    BidResponse response) {
        super(context);
        mInterstitialManager = new InterstitialManager();
        mAdUnitConfiguration = adUnitConfiguration;
        mDisplayViewListener = listener;

        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(response, () -> {
            try {
                if (response.isVideo()) {
                    displayVideoAd(response);
                }
                else {
                    displayHtmlAd(response);
                }
            }
            catch (AdException e) {
                notifyListenerError(e);
            }
        });
    }

    public DisplayView(
            @NonNull
                    Context context,
            DisplayViewListener listener,
            @NonNull
                    AdUnitConfiguration adUnitConfiguration,
            @NonNull
                    String responseId) throws AdException {
        this(context, listener, adUnitConfiguration, getBidResponseFromCache(responseId));
    }

    public void destroy() {
        mAdUnitConfiguration = null;
        mDisplayViewListener = null;
        mInterstitialManager = null;
        if (mVideoView != null) {
            mVideoView.destroy();
        }
        if (mAdViewManager != null) {
            mAdViewManager.destroy();
            mAdViewManager = null;
        }

        if (mEventForwardingReceiver != null) {
            mEventForwardingReceiver.unregister(mEventForwardingReceiver);
            mEventForwardingReceiver = null;
        }
    }

    private void displayHtmlAd(BidResponse response) throws AdException {
        mAdViewManager = new AdViewManager(getContext(), mAdViewManagerListener, this, mInterstitialManager);
        mAdViewManager.loadBidTransaction(mAdUnitConfiguration, response);

        mEventForwardingReceiver = new EventForwardingLocalBroadcastReceiver(mAdUnitConfiguration.getBroadcastId(),
                                                                             mBroadcastListener);
        mEventForwardingReceiver.register(getContext(), mEventForwardingReceiver);
    }

    private void displayVideoAd(BidResponse response) throws AdException {
        mVideoView = new VideoView(getContext(), mAdUnitConfiguration);
        mVideoView.setVideoViewListener(mVideoViewListener);
        mVideoView.setVideoPlayerClick(true);
        mVideoView.loadAd(mAdUnitConfiguration, response);
        addView(mVideoView);
    }

    private void notifyListenerError(AdException e) {
        LogUtil.debug(TAG, "onAdFailed");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdFailed(e);
        }
    }

    private void notifyListenerClicked() {
        LogUtil.debug(TAG, "onAdClicked");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdClicked();
        }
    }

    private void notifyListenerClose() {
        LogUtil.debug(TAG, "onAdClosed");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdClosed();
        }
    }

    private void notifyListenerDisplayed() {
        LogUtil.debug(TAG, "onAdDisplayed");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdDisplayed();
        }
    }

    private void notifyListenerLoaded() {
        LogUtil.debug(TAG, "onAdLoaded");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdLoaded();
        }
    }

    private void handleBroadcastAction(String action) {
        if (IntentActions.ACTION_BROWSER_CLOSE.equals(action)) {
            notifyListenerClose();
        }
    }

    private static BidResponse getBidResponseFromCache(String id) throws AdException {
        BidResponse cachedResponse = BidResponseCache.getInstance().popBidResponse(id);
        if (cachedResponse == null) {
            throw new AdException(AdException.INTERNAL_ERROR, "No cached bid response found");
        }
        return cachedResponse;
    }
}
