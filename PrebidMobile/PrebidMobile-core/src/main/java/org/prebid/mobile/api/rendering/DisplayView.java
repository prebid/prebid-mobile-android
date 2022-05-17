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

package org.prebid.mobile.api.rendering;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;
import androidx.annotation.NonNull;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.networking.WinNotifier;
import org.prebid.mobile.rendering.utils.broadcast.local.EventForwardingLocalBroadcastReceiver;
import org.prebid.mobile.rendering.utils.constants.IntentActions;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.video.VideoViewListener;

public class DisplayView extends FrameLayout {

    private final static String TAG = DisplayView.class.getSimpleName();
    private static final String CONTENT_DESCRIPTION_AD_VIEW = "adView";

    private String impressionEventUrl;

    private AdUnitConfiguration adUnitConfiguration;
    private DisplayViewListener displayViewListener;
    private InterstitialManager interstitialManager;
    private AdViewManager adViewManager;
    private VideoView videoView;

    private EventForwardingLocalBroadcastReceiver eventForwardingReceiver;
    private final EventForwardingLocalBroadcastReceiver.EventForwardingBroadcastListener broadcastListener = this::handleBroadcastAction;

    private final AdViewManagerListener adViewManagerListener = new AdViewManagerListener() {
        @Override
        public void adLoaded(AdDetails adDetails) {
            // for banner adViewManager.show() will be called automatically
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

    private final VideoViewListener videoViewListener = new VideoViewListener() {
        @Override
        public void onLoaded(
            @NonNull
                VideoView videoAdView,
            AdDetails adDetails
        ) {
            videoAdView.setContentDescription(CONTENT_DESCRIPTION_AD_VIEW);
            notifyListenerLoaded();
        }

        @Override
        public void onLoadFailed(
            @NonNull
                VideoView videoAdView,
            AdException error
        ) {
            notifyListenerError(error);
        }

        @Override
        public void onDisplayed(
            @NonNull
                VideoView videoAdView
        ) {
            notifyListenerDisplayed();
        }

        @Override
        public void onClickThroughOpened(
            @NonNull
                VideoView videoAdView
        ) {
            notifyListenerClicked();
        }

        @Override
        public void onClickThroughClosed(
            @NonNull
                VideoView videoAdView
        ) {
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
            BidResponse response
    ) {
        super(context);
        interstitialManager = new InterstitialManager();
        this.adUnitConfiguration = adUnitConfiguration;
        displayViewListener = listener;

        WinNotifier winNotifier = new WinNotifier();
        winNotifier.notifyWin(response, () -> {
            try {
                adUnitConfiguration.modifyUsingBidResponse(response);
                if (response.isVideo()) {
                    displayVideoAd(response);
                } else {
                    displayHtmlAd(response);
                }
                impressionEventUrl = response.getImpressionEventUrl();
            } catch (AdException e) {
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
            String responseId
    ) throws AdException {
        this(context, listener, adUnitConfiguration, getBidResponseFromCache(responseId));
    }

    public void destroy() {
        adUnitConfiguration = null;
        displayViewListener = null;
        interstitialManager = null;
        if (videoView != null) {
            videoView.destroy();
        }
        if (adViewManager != null) {
            adViewManager.destroy();
            adViewManager = null;
        }

        if (eventForwardingReceiver != null) {
            eventForwardingReceiver.unregister(eventForwardingReceiver);
            eventForwardingReceiver = null;
        }
    }

    private void displayHtmlAd(BidResponse response) throws AdException {
        adViewManager = new AdViewManager(getContext(), adViewManagerListener, this, interstitialManager);
        adViewManager.loadBidTransaction(adUnitConfiguration, response);

        eventForwardingReceiver = new EventForwardingLocalBroadcastReceiver(
            adUnitConfiguration.getBroadcastId(),
            broadcastListener
        );
        eventForwardingReceiver.register(getContext(), eventForwardingReceiver);
    }

    private void displayVideoAd(BidResponse response) throws AdException {
        videoView = new VideoView(getContext(), adUnitConfiguration);
        videoView.setVideoViewListener(videoViewListener);
        videoView.setVideoPlayerClick(true);
        videoView.loadAd(adUnitConfiguration, response);
        addView(videoView);
    }

    private void notifyListenerError(AdException e) {
        LogUtil.debug(TAG, "onAdFailed");
        if (displayViewListener != null) {
            displayViewListener.onAdFailed(e);
        }
    }

    private void notifyListenerClicked() {
        LogUtil.debug(TAG, "onAdClicked");
        if (displayViewListener != null) {
            displayViewListener.onAdClicked();
        }
    }

    private void notifyListenerClose() {
        LogUtil.debug(TAG, "onAdClosed");
        if (displayViewListener != null) {
            displayViewListener.onAdClosed();
        }
    }

    private void notifyListenerDisplayed() {
        LogUtil.debug(TAG, "onAdDisplayed");
        if (displayViewListener != null) {
            displayViewListener.onAdDisplayed();
        }
    }

    private void notifyListenerLoaded() {
        LogUtil.debug(TAG, "onAdLoaded");
        if (displayViewListener != null) {
            displayViewListener.onAdLoaded();
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
