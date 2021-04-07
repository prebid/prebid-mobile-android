package com.openx.apollo.bidding.display;

import android.content.Context;
import android.view.View;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;

import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.bidding.listeners.DisplayViewListener;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.AdDetails;
import com.openx.apollo.networking.WinNotifier;
import com.openx.apollo.utils.broadcast.local.EventForwardingLocalBroadcastReceiver;
import com.openx.apollo.utils.constants.IntentActions;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.AdViewManager;
import com.openx.apollo.views.AdViewManagerListener;
import com.openx.apollo.views.indicator.AdIndicatorView;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.video.VideoViewListener;

public class DisplayView extends FrameLayout {
    private final static String TAG = DisplayView.class.getSimpleName();
    private static final String CONTENT_DESCRIPTION_AD_VIEW = "adView";

    private AdConfiguration mAdUnitConfiguration;
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
            AdConfiguration adUnitConfiguration,
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
            AdConfiguration adUnitConfiguration,
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
        AdIndicatorView adIndicatorView = new AdIndicatorView(getContext(), mAdUnitConfiguration.getAdUnitIdentifierType());
        mAdViewManager.setAdIndicatorView(adIndicatorView);
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
        OXLog.debug(TAG, "onAdFailed");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdFailed(e);
        }
    }

    private void notifyListenerClicked() {
        OXLog.debug(TAG, "onAdClicked");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdClicked();
        }
    }

    private void notifyListenerClose() {
        OXLog.debug(TAG, "onAdClosed");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdClosed();
        }
    }

    private void notifyListenerDisplayed() {
        OXLog.debug(TAG, "onAdDisplayed");
        if (mDisplayViewListener != null) {
            mDisplayViewListener.onAdDisplayed();
        }
    }

    private void notifyListenerLoaded() {
        OXLog.debug(TAG, "onAdLoaded");
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
