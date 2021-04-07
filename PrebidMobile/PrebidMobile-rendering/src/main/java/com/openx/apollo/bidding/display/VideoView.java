package com.openx.apollo.bidding.display;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import androidx.core.content.ContextCompat;

import com.openx.apollo.bidding.data.bid.BidResponse;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.AdDetails;
import com.openx.apollo.models.CreativeVisibilityTracker;
import com.openx.apollo.models.internal.InternalFriendlyObstruction;
import com.openx.apollo.models.internal.VisibilityTrackerOption;
import com.openx.apollo.models.internal.VisibilityTrackerResult;
import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.utils.helpers.Utils;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.video.VideoCreativeView;
import com.openx.apollo.views.AdViewManager;
import com.openx.apollo.views.AdViewManagerListener;
import com.openx.apollo.views.base.BaseAdView;
import com.openx.apollo.views.indicator.AdIndicatorView;
import com.openx.apollo.views.video.VideoViewListener;
import com.openx.apollo.views.webview.mraid.Views;

import static com.openx.apollo.utils.constants.IntentActions.ACTION_BROWSER_CLOSE;

public class VideoView extends BaseAdView {
    private final static String TAG = VideoView.class.getSimpleName();

    private VideoViewListener mListener;

    private CreativeVisibilityTracker mVisibilityTracker;
    private final CreativeVisibilityTracker.VisibilityTrackerListener mVisibilityTrackerListener = this::handleVisibilityChange;

    private State mVideoViewState = State.UNDEFINED;

    private boolean mEnableVideoPlayerClick;
    private boolean mEnableAutoPlay = true;

    //region ========== Listener Area

    private final AdViewManagerListener mOnAdViewManagerListener = new AdViewManagerListener() {
        @Override
        public void adLoaded(final AdDetails adDetails) {
            mListener.onLoaded(VideoView.this, adDetails);
            changeState(State.PLAYBACK_NOT_STARTED);
            if (mEnableAutoPlay) {
                startVisibilityTracking();
            }
        }

        @Override
        public void viewReadyForImmediateDisplay(View view) {
            if (mAdViewManager.isNotShowingEndCard()) {
                mListener.onDisplayed(VideoView.this);
            }
            removeAllViews();
            if (mAdViewManager.hasEndCard()) {
                showEndCardCreative(view);
            }
            else {
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
            mListener.onPlayBackCompleted(VideoView.this);

            if (mAdViewManager.isNotShowingEndCard()) {
                showWatchAgain();
            }
        }

        @Override
        public void creativeClicked(String url) {
            mListener.onClickThroughOpened(VideoView.this);
        }

        @Override
        public void creativeMuted() {
            mListener.onVideoMuted();
        }

        @Override
        public void creativeUnMuted() {
            mListener.onVideoUnMuted();
        }

        @Override
        public void creativePaused() {
            mListener.onPlaybackPaused();
        }

        @Override
        public void creativeResumed() {
            mListener.onPlaybackResumed();
        }
    };
    //endregion ========== Listener Area

    protected VideoView(Context context) throws AdException {
        super(context);
        init();
    }

    public VideoView(Context context, AdConfiguration adUnitConfiguration) throws AdException {
        super(context);
        prepareAdConfiguration(adUnitConfiguration);
        init();
    }

    void loadAd(AdConfiguration adUnitConfiguration, BidResponse bidResponse) {
        mAdViewManager.loadBidTransaction(adUnitConfiguration, bidResponse);
    }

    public void loadAd(AdConfiguration adConfiguration, String vastXml) {
        stopVisibilityTracking();
        changeState(State.UNDEFINED);

        mAdViewManager.loadVideoTransaction(adConfiguration, vastXml);
    }

    @Override
    public void destroy() {
        super.destroy();
        stopVisibilityTracking();

        // if (mVideoDialog != null) {
        //     mVideoDialog.hide();
        //     mVideoDialog.cancel();
        //     mVideoDialog = null;
        // }
    }

    public void mute(boolean enabled) {
        if (enabled) {
            mAdViewManager.mute();
        }
        else {
            mAdViewManager.unmute();
        }
    }

    public void setVideoViewListener(VideoViewListener listener) {
        mListener = listener;
    }

    public void setVideoPlayerClick(boolean enable) {
        mEnableVideoPlayerClick = enable;
    }

    public void setAutoPlay(boolean enable) {
        mEnableAutoPlay = enable;

        if (!enable) {
            stopVisibilityTracking();
        }
    }

    public void pause() {
        if (!canPause()) {
            OXLog.debug(TAG, "pause() can't pause " + mVideoViewState);
            return;
        }

        changeState(State.PAUSED_BY_USER);
        mAdViewManager.pause();
    }

    public void resume() {
        if (!canResume()) {
            OXLog.debug(TAG, "resume() can't resume " + mVideoViewState);
            return;
        }

        changeState(State.PLAYING);
        mAdViewManager.resume();
    }

    public void play() {
        if (!canPlay()) {
            OXLog.debug(TAG, "play() can't play " + mVideoViewState);
            return;
        }

        changeState(State.PLAYING);
        mAdViewManager.show();
    }

    @Override
    protected void init() throws AdException {
        try {
            super.init();
            setAdViewManagerValues();
            setBackgroundColor(ContextCompat.getColor(getContext(), android.R.color.black));
            registerEventBroadcast();
        }
        catch (Exception e) {
            throw new AdException(AdException.INIT_ERROR, "VideoAdView initialization failed: " + Log.getStackTraceString(e));
        }
    }

    @Override
    protected void handleBroadcastAction(String action) {
        switch (action) {
            case ACTION_BROWSER_CLOSE:
                mListener.onClickThroughClosed(VideoView.this);
                break;
        }
    }

    protected void setAdViewManagerValues() throws AdException {
        mAdIndicatorView = new AdIndicatorView(getContext(), AdConfiguration.AdUnitIdentifierType.VAST);
        mAdViewManager = new AdViewManager(getContext(), mOnAdViewManagerListener, this, mInterstitialManager);
        mAdViewManager.setAdIndicatorView(mAdIndicatorView);
    }

    @Override
    protected void handleWindowFocusChange(boolean hasWindowFocus) {
        Log.d(TAG, "handleWindowFocusChange() called with: hasWindowFocus = [" + hasWindowFocus + "]");
        // visibility checker will handle resume
        if (mEnableAutoPlay) {
            return;
        }

        handlePlaybackBasedOnVisibility(hasWindowFocus);
    }

    @Override
    public void notifyErrorListeners(final AdException adException) {
        mListener.onLoadFailed(VideoView.this, adException);
    }

    private void prepareAdConfiguration(AdConfiguration adUnitConfiguration) {
        adUnitConfiguration.setAutoRefreshDelay(0);
        adUnitConfiguration.setBuiltInVideo(true);
        adUnitConfiguration.setVideoInitialVolume(0.0f);
    }

    private void showVideoCreative(View view) {
        VideoCreativeView videoCreativeView = (VideoCreativeView) view;

        if (mEnableVideoPlayerClick) {
            videoCreativeView.enableVideoPlayerClick();
        }

        videoCreativeView.showVolumeControls();
        addView(view);
        addView(getAdIndicatorView());
    }

    private void showEndCardCreative(View creativeView) {
        // if (isFullScreen()) {
        //     mVideoDialog.showBannerCreative(creativeView);
        // }
        // else {
        Views.removeFromParent(creativeView);
        FrameLayout.LayoutParams layoutParams = new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                                                                 FrameLayout.LayoutParams.MATCH_PARENT);
        creativeView.setLayoutParams(layoutParams);
        addView(creativeView);
        // }
    }

    private void showWatchAgain() {
        View watchAgainButton = Utils.createWatchAgainView(getContext());
        if (watchAgainButton == null) {
            OXLog.debug(TAG, "showWatchAgain: Failed. WatchAgainView is null");
            return;
        }

        InternalFriendlyObstruction watchAgainObstruction = new InternalFriendlyObstruction(watchAgainButton,
                                                                                            InternalFriendlyObstruction.Purpose.VIDEO_CONTROLS,
                                                                                            "WatchAgain button");
        mAdViewManager.addObstructions(watchAgainObstruction);

        Views.removeFromParent(watchAgainButton);
        watchAgainButton.setOnClickListener(v -> {
            changeState(State.PLAYBACK_NOT_STARTED);
            startVisibilityTracking();
        });
        // if (isFullScreen()) {
        //     mVideoDialog.dismiss();
        // }
        addView(watchAgainButton);
    }

    private void startVisibilityTracking() {
        stopVisibilityTracking();

        final VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);
        mVisibilityTracker = new CreativeVisibilityTracker(this,
                                                           visibilityTrackerOption,
                                                           true);
        mVisibilityTracker.setVisibilityTrackerListener(mVisibilityTrackerListener);
        mVisibilityTracker.startVisibilityCheck(getContext());
    }

    private void stopVisibilityTracking() {
        if (mVisibilityTracker != null) {
            mVisibilityTracker.stopVisibilityCheck();
        }
    }

    private void handleVisibilityChange(VisibilityTrackerResult result) {
        final boolean isVisible = result.isVisible();

        if (isVisible && canPlay()) {
            play();
            OXLog.debug(TAG, "handleVisibilityChange: auto show " + mVideoViewState);
            return;
        }

        handlePlaybackBasedOnVisibility(isVisible);
    }

    private void handlePlaybackBasedOnVisibility(boolean isVisible) {
        if (!isVisible && canPause()) {
            mAdViewManager.pause();
            changeState(State.PAUSED_AUTO);
            OXLog.debug(TAG, "handleVisibilityChange: auto pause " + mVideoViewState);
        }
        else if (isVisible && isInState(State.PAUSED_AUTO)) {
            mAdViewManager.resume();
            changeState(State.PLAYING);
            OXLog.debug(TAG, "handleVisibilityChange: auto resume " + mVideoViewState);
        }
    }

    private void changeState(State undefined) {
        mVideoViewState = undefined;
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
        return mVideoViewState == state;
    }

    enum State {
        UNDEFINED,
        PLAYBACK_NOT_STARTED,
        PLAYING,
        PAUSED_BY_USER,
        PAUSED_AUTO,
        PLAYBACK_FINISHED
    }

    // private VideoDialog mVideoDialog;
    // private ViewParent mViewParentBeforeExpand;
    // private ViewGroup.LayoutParams mLayoutParamsBeforeExpand;

    // private final VideoDialogListener mVideoDialogListener = new VideoDialogListener() {
    //     @Override
    //     public void onVideoDialogClosed() {
    //         if (mAdViewManager.isInterstitialClosed()) {
    //             mAdViewManager.trackCloseEvent();
    //             return;
    //         }
    //         mAdViewManager.resetTransactionState(); //?
    //
    //         mListener.adInterstitialDidClose(VideoView.this);
    //     }
    // };

    // private void resetViewToInitialState() {
    // mVideoDialog = null;
    // VideoView videoAdView = VideoView.this;
    //
    // mAdViewManager.returnFromVideo(videoAdView);
    //
    // removeView(findViewById(R.id.iv_close_interstitial));
    //
    // Views.removeFromParent(videoAdView);
    //
    // if (mLayoutParamsBeforeExpand != null) {
    //     setLayoutParams(mLayoutParamsBeforeExpand);
    // }
    //
    // if (mViewParentBeforeExpand instanceof ViewGroup) {
    //     ((ViewGroup) mViewParentBeforeExpand).addView(videoAdView);
    // }
    // }

    //
    // @VisibleForTesting
    // protected void createDialog(Context context, final VideoCreativeView view) {
    //     mViewParentBeforeExpand = getParent();
    //     mLayoutParamsBeforeExpand = getLayoutParams();
    //
    //     Views.removeFromParent(this);
    //
    //     mVideoDialog = new VideoDialog(context, view, mAdViewManager, mInterstitialManager, this);
    //     mVideoDialog.setOnDismissListener(new VideoView.VideoDialogDismissListener(this));
    //     mVideoDialog.setVideoDialogListener(mVideoDialogListener);
    //     mVideoDialog.show();
    // }

    // private void showFullScreen() {
    //     View currentView = getChildAt(0);
    //     if (mAdViewManager.canShowFullScreen() && mVideoDialog == null && currentView instanceof VideoCreativeView) {
    //         createDialog(getContext(), (VideoCreativeView) currentView);
    //     }
    // }
    //
    // private boolean checkForWatchAgain() {
    //     return findViewById(R.id.btn_watch_again) != null;
    // }
    //
    // private boolean isFullScreen() {
    //     return mVideoDialog != null && mVideoDialog.isShowing();
    // }

    // protected static class VideoDialogDismissListener implements DialogInterface.OnDismissListener {
    //     private final WeakReference<VideoView> mWeakVideoAdView;
    //
    //     public VideoDialogDismissListener(VideoView videoDialog) {
    //         mWeakVideoAdView = new WeakReference<>(videoDialog);
    //     }
    //
    //     @Override
    //     public void onDismiss(DialogInterface dialog) {
    //         VideoView videoAdView = mWeakVideoAdView.get();
    //         if (videoAdView == null) {
    //             OXLog.debug(TAG, "VideoDialog.onDismiss(): Unable to perform dismiss action. VideoAdView is null");
    //             return;
    //         }
    //         videoAdView.resetViewToInitialState();
    //     }
    // }
}
