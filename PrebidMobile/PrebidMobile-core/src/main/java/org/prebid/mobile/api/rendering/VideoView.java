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
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.utils.constants.IntentActions;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.video.VideoCreativeView;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.AdViewManagerListener;
import org.prebid.mobile.rendering.views.base.BaseAdView;
import org.prebid.mobile.rendering.views.video.VideoViewListener;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

/**
 * Internal video player view for displaying banner video ads.
 */
public class VideoView extends BaseAdView {

    private final static String TAG = VideoView.class.getSimpleName();

    private VideoViewListener listener;

    private CreativeVisibilityTracker visibilityTracker;
    private final CreativeVisibilityTracker.VisibilityTrackerListener visibilityTrackerListener = this::handleVisibilityChange;

    private State videoViewState = State.UNDEFINED;

    private boolean enableVideoPlayerClick;
    private boolean enableAutoPlay = true;

    //region ========== Listener Area

    private final AdViewManagerListener onAdViewManagerListener = new AdViewManagerListener() {
        @Override
        public void adLoaded(final AdDetails adDetails) {
            listener.onLoaded(VideoView.this, adDetails);
            changeState(State.PLAYBACK_NOT_STARTED);
            if (enableAutoPlay) {
                startVisibilityTracking();
            }
        }

        @Override
        public void viewReadyForImmediateDisplay(View view) {
            if (adViewManager.isNotShowingEndCard()) {
                listener.onDisplayed(VideoView.this);
            }
            removeAllViews();
            if (adViewManager.hasEndCard()) {
                showEndCardCreative(view);
            } else {
                showVideoCreative(view);
            }
        }

        @Override
        public void failedToLoad(AdException error) {
            notifyErrorListeners(error);
        }

        @Override
        public void videoCreativePlaybackFinished() {
            stopVisibilityTracking();
            changeState(State.PLAYBACK_FINISHED);
            listener.onPlayBackCompleted(VideoView.this);

            if (adViewManager.isNotShowingEndCard()) {
                showWatchAgain();
            }
        }

        @Override
        public void creativeClicked(String url) {
            listener.onClickThroughOpened(VideoView.this);
        }

        @Override
        public void creativeMuted() {
            listener.onVideoMuted();
        }

        @Override
        public void creativeUnMuted() {
            listener.onVideoUnMuted();
        }

        @Override
        public void creativePaused() {
            listener.onPlaybackPaused();
        }

        @Override
        public void creativeResumed() {
            listener.onPlaybackResumed();
        }
    };
    //endregion ========== Listener Area

    protected VideoView(Context context) throws AdException {
        super(context);
        init();
    }

    public VideoView(
        Context context,
        AdUnitConfiguration adUnitConfiguration
    ) throws AdException {
        super(context);
        prepareAdConfiguration(adUnitConfiguration);
        init();
    }

    void loadAd(
        AdUnitConfiguration adUnitConfiguration,
        BidResponse bidResponse
    ) {
        adViewManager.loadBidTransaction(adUnitConfiguration, bidResponse);
    }

    public void loadAd(
        AdUnitConfiguration adConfiguration,
        String vastXml
    ) {
        stopVisibilityTracking();
        changeState(State.UNDEFINED);

        adViewManager.loadVideoTransaction(adConfiguration, vastXml);
    }

    @Override
    public void destroy() {
        super.destroy();
        stopVisibilityTracking();

        // if (videoDialog != null) {
        //     videoDialog.hide();
        //     videoDialog.cancel();
        //     videoDialog = null;
        // }
    }

    public void mute(boolean enabled) {
        if (enabled) {
            adViewManager.mute();
        } else {
            adViewManager.unmute();
        }
    }

    public void setVideoViewListener(VideoViewListener listener) {
        this.listener = listener;
    }

    public void setVideoPlayerClick(boolean enable) {
        enableVideoPlayerClick = enable;
    }

    public void setAutoPlay(boolean enable) {
        enableAutoPlay = enable;

        if (!enable) {
            stopVisibilityTracking();
        }
    }

    public void pause() {
        if (!canPause()) {
            LogUtil.debug(TAG, "pause() can't pause " + videoViewState);
            return;
        }

        changeState(State.PAUSED_BY_USER);
        adViewManager.pause();
    }

    public void resume() {
        if (!canResume()) {
            LogUtil.debug(TAG, "resume() can't resume " + videoViewState);
            return;
        }

        changeState(State.PLAYING);
        adViewManager.resume();
    }

    public void play() {
        if (!canPlay()) {
            LogUtil.debug(TAG, "play() can't play " + videoViewState);
            return;
        }

        changeState(State.PLAYING);
        adViewManager.show();
    }

    @Override
    protected void init() throws AdException {
        try {
            super.init();
            setAdViewManagerValues();
            setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.black));
            registerEventBroadcast();
        } catch (Exception e) {
            throw new AdException(AdException.INIT_ERROR, "VideoAdView initialization failed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void handleBroadcastAction(String action) {
        switch (action) {
            case IntentActions.ACTION_BROWSER_CLOSE:
                listener.onClickThroughClosed(VideoView.this);
                break;
        }
    }

    protected void setAdViewManagerValues() throws AdException {
        adViewManager = new AdViewManager(getContext(), onAdViewManagerListener, this, interstitialManager);
    }

    @Override
    protected void handleWindowFocusChange(boolean hasWindowFocus) {
        LogUtil.debug(TAG, "handleWindowFocusChange() called with: hasWindowFocus = [" + hasWindowFocus + "]");
        // visibility checker will handle resume
        if (enableAutoPlay) {
            return;
        }

        handlePlaybackBasedOnVisibility(hasWindowFocus);
    }

    @Override
    public void notifyErrorListeners(final AdException adException) {
        listener.onLoadFailed(VideoView.this, adException);
    }

    private void prepareAdConfiguration(AdUnitConfiguration adUnitConfiguration) {
        adUnitConfiguration.setAutoRefreshDelay(0);
        adUnitConfiguration.setBuiltInVideo(true);
        adUnitConfiguration.setVideoInitialVolume(0.0f);
    }

    private void showVideoCreative(View view) {
        VideoCreativeView videoCreativeView = (VideoCreativeView) view;

        if (enableVideoPlayerClick) {
            videoCreativeView.enableVideoPlayerClick();
        }
        videoCreativeView.showVolumeControls();
        addVideoControlObstruction(videoCreativeView.getVolumeControlView(), "Volume button");

        addView(view);
    }

    private void showEndCardCreative(View creativeView) {
        // if (isFullScreen()) {
        //     videoDialog.showBannerCreative(creativeView);
        // }
        // else {
        Views.removeFromParent(creativeView);
        FrameLayout.LayoutParams layoutParams = new LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.MATCH_PARENT
        );
        creativeView.setLayoutParams(layoutParams);
        addView(creativeView);
        // }
    }

    private void showWatchAgain() {
        View watchAgainButton = Utils.createWatchAgainView(getContext());
        if (watchAgainButton == null) {
            LogUtil.debug(TAG, "showWatchAgain: Failed. WatchAgainView is null");
            return;
        }

        addVideoControlObstruction(watchAgainButton, "WatchAgain button");

        Views.removeFromParent(watchAgainButton);
        watchAgainButton.setOnClickListener(v -> {
            changeState(State.PLAYBACK_NOT_STARTED);
            startVisibilityTracking();
        });
        // if (isFullScreen()) {
        //     videoDialog.dismiss();
        // }
        addView(watchAgainButton);
    }

    private void addVideoControlObstruction(
        View view,
        String description
    ) {
        if (view == null) {
            return;
        }

        InternalFriendlyObstruction obstruction = new InternalFriendlyObstruction(
            view,
            InternalFriendlyObstruction.Purpose.VIDEO_CONTROLS,
            description
        );
        adViewManager.addObstructions(obstruction);
    }

    private void startVisibilityTracking() {
        stopVisibilityTracking();

        final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        visibilityTracker = new CreativeVisibilityTracker(
            this,
            visibilityTrackerOption,
            true
        );
        visibilityTracker.setVisibilityTrackerListener(visibilityTrackerListener);
        visibilityTracker.startVisibilityCheck(getContext());
    }

    private void stopVisibilityTracking() {
        if (visibilityTracker != null) {
            visibilityTracker.stopVisibilityCheck();
        }
    }

    private void handleVisibilityChange(VisibilityTrackerResult result) {
        final boolean isVisible = result.isVisible();

        if (isVisible && canPlay()) {
            play();
            LogUtil.debug(TAG, "handleVisibilityChange: auto show " + videoViewState);
            return;
        }

        handlePlaybackBasedOnVisibility(isVisible);
    }

    private void handlePlaybackBasedOnVisibility(boolean isVisible) {
        if (!isVisible && canPause()) {
            adViewManager.pause();
            changeState(State.PAUSED_AUTO);
            LogUtil.debug(TAG, "handleVisibilityChange: auto pause " + videoViewState);
        } else if (isVisible && isInState(State.PAUSED_AUTO)) {
            adViewManager.resume();
            changeState(State.PLAYING);
            LogUtil.debug(TAG, "handleVisibilityChange: auto resume " + videoViewState);
        }
    }

    private void changeState(State undefined) {
        videoViewState = undefined;
    }

    private boolean canPlay() {
        return isInState(State.PLAYBACK_NOT_STARTED);
    }

    private boolean canPause() {
        return isInState(State.PLAYING);
    }

    private boolean canResume() {
        return isInState(State.PAUSED_AUTO) || isInState(State.PAUSED_BY_USER);
    }

    private boolean isInState(State state) {
        return videoViewState == state;
    }

    enum State {
        UNDEFINED,
        PLAYBACK_NOT_STARTED,
        PLAYING,
        PAUSED_BY_USER,
        PAUSED_AUTO,
        PLAYBACK_FINISHED
    }

    // private VideoDialog videoDialog;
    // private ViewParent viewParentBeforeExpand;
    // private ViewGroup.LayoutParams layoutParamsBeforeExpand;

    // private final VideoDialogListener videoDialogListener = new VideoDialogListener() {
    //     @Override
    //     public void onVideoDialogClosed() {
    //         if (adViewManager.isInterstitialClosed()) {
    //             adViewManager.trackCloseEvent();
    //             return;
    //         }
    //         adViewManager.resetTransactionState(); //?
    //
    //         listener.adInterstitialDidClose(VideoView.this);
    //     }
    // };

    // private void resetViewToInitialState() {
    // videoDialog = null;
    // VideoView videoAdView = VideoView.this;
    //
    // adViewManager.returnFromVideo(videoAdView);
    //
    // removeView(findViewById(R.id.iv_close_interstitial));
    //
    // Views.removeFromParent(videoAdView);
    //
    // if (layoutParamsBeforeExpand != null) {
    //     setLayoutParams(layoutParamsBeforeExpand);
    // }
    //
    // if (viewParentBeforeExpand instanceof ViewGroup) {
    //     ((ViewGroup) viewParentBeforeExpand).addView(videoAdView);
    // }
    // }

    //
    // @VisibleForTesting
    // protected void createDialog(Context context, final VideoCreativeView view) {
    //     viewParentBeforeExpand = getParent();
    //     layoutParamsBeforeExpand = getLayoutParams();
    //
    //     Views.removeFromParent(this);
    //
    //     videoDialog = new VideoDialog(context, view, adViewManager, interstitialManager, this);
    //     videoDialog.setOnDismissListener(new VideoView.VideoDialogDismissListener(this));
    //     videoDialog.setVideoDialogListener(videoDialogListener);
    //     videoDialog.show();
    // }

    // private void showFullScreen() {
    //     View currentView = getChildAt(0);
    //     if (adViewManager.canShowFullScreen() && videoDialog == null && currentView instanceof VideoCreativeView) {
    //         createDialog(getContext(), (VideoCreativeView) currentView);
    //     }
    // }
    //
    // private boolean checkForWatchAgain() {
    //     return findViewById(R.id.btn_watch_again) != null;
    // }
    //
    // private boolean isFullScreen() {
    //     return videoDialog != null && videoDialog.isShowing();
    // }

    // protected static class VideoDialogDismissListener implements DialogInterface.OnDismissListener {
    //     private final WeakReference<VideoView> weakVideoAdView;
    //
    //     public VideoDialogDismissListener(VideoView videoDialog) {
    //         weakVideoAdView = new WeakReference<>(videoDialog);
    //     }
    //
    //     @Override
    //     public void onDismiss(DialogInterface dialog) {
    //         VideoView videoAdView = weakVideoAdView.get();
    //         if (videoAdView == null) {
    //             Log.debug(TAG, "VideoDialog.onDismiss(): Unable to perform dismiss action. VideoAdView is null");
    //             return;
    //         }
    //         videoAdView.resetViewToInitialState();
    //     }
    // }
}
