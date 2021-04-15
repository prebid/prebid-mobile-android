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

package org.prebid.mobile.rendering.video;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerVideoDelegate;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.loading.FileDownloadListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeVisibilityTracker;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerOption;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.helpers.AppInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

import java.io.File;
import java.lang.ref.WeakReference;

public class VideoCreative extends VideoCreativeProtocol
    implements VideoCreativeViewListener, InterstitialManagerVideoDelegate {
    private static final String TAG = VideoCreative.class.getSimpleName();

    @NonNull
    private final VideoCreativeModel mModel;

    @VisibleForTesting
    VideoCreativeView mVideoCreativeView;

    private AsyncTask mVideoDownloadTask;

    private String mPreloadedVideoFilePath;

    public VideoCreative(Context context,
                         @NonNull
                             VideoCreativeModel model, OmAdSessionManager omAdSessionManager, InterstitialManager interstitialManager)
    throws AdException {
        super(context, model, omAdSessionManager, interstitialManager);

        mModel = model;
        if (mInterstitialManager != null) {
            mInterstitialManager.setInterstitialVideoDelegate(this);
        }
    }

    @Override
    public void load() {
        //Use URLConnection to download a video file.
        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();

        params.url = mModel.getMediaUrl();
        params.userAgent = AppInfoManager.getUserAgent();
        params.requestType = "GET";
        params.name = BaseNetworkTask.DOWNLOAD_TASK;

        Context context = mContextReference.get();
        if (context != null) {
            AdConfiguration adConfiguration = mModel.getAdConfiguration();
            String shortenedPath = LruController.getShortenedPath(params.url);
            File file = new File(context.getFilesDir(), shortenedPath);
            VideoDownloadTask videoDownloadTask = new VideoDownloadTask(context, file,
                                                                        new VideoCreativeVideoPreloadListener(this), adConfiguration);
            mVideoDownloadTask = videoDownloadTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params);
        }
    }

    @Override
    public void display() {
        if (mVideoCreativeView != null) {
            mVideoCreativeView.start(mModel.getAdConfiguration().getVideoInitialVolume());
            mModel.trackPlayerStateChange(InternalPlayerState.NORMAL);
            startViewabilityTracker();
        }
    }

    @Override
    public void skip() {
        OXLog.debug(TAG, "Track 'skip' event");
        mModel.trackVideoEvent(VideoAdEvent.Event.AD_SKIP);
        // Send it to AdView
        getCreativeViewListener().creativeDidComplete(this);
    }

    @Override
    public void onReadyForDisplay() {
        getResolutionListener().creativeReady(this);
    }

    @Override
    public void onDisplayCompleted() {
        complete();
    }

    @Override
    public void onFailure(AdException error) {
        // ad -> inline -> error
        mModel.trackVideoEvent(VideoAdEvent.Event.AD_ERROR);
        getResolutionListener().creativeFailed(error);
    }

    @Override
    public void onEvent(VideoAdEvent.Event trackingEvent) {
        mModel.trackVideoEvent(trackingEvent);

        notifyCreativeViewListener(trackingEvent);
    }

    @Override
    public void trackAdLoaded() {
        mModel.trackNonSkippableStandaloneVideoLoaded(false);
    }

    @Override
    public void onVolumeChanged(float volume) {
        notifyVolumeChanged(volume);

        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            OXLog.error(TAG, "trackVolume failed, OmAdSessionManager is null");
            return;
        }
        omAdSessionManager.trackVolumeChange(volume);
    }

    @Override
    public void onPlayerStateChanged(InternalPlayerState state) {
        mModel.trackPlayerStateChange(state);
    }

    @Override
    public boolean isInterstitialClosed() {
        return mModel.hasEndCard();
    }

    @Override
    public long getMediaDuration() {
        return mModel.getMediaDuration();
    }

    @Override
    public void handleAdWindowFocus() {
        // Resume video view
        resume();
    }

    @Override
    public void resume() {
        if (mVideoCreativeView != null && mVideoCreativeView.hasVideoStarted()) {
            mVideoCreativeView.resume();
        }
    }

    @Override
    public boolean isPlaying() {
        return mVideoCreativeView != null && mVideoCreativeView.isPlaying();
    }

    @Override
    public void handleAdWindowNoFocus() {
        // Pause video view
        pause();
    }

    @Override
    public void pause() {
        if (mVideoCreativeView != null && mVideoCreativeView.isPlaying()) {
            mVideoCreativeView.pause();
        }
    }

    @Override
    public void mute() {
        if (mVideoCreativeView != null && mVideoCreativeView.getVolume() != 0) {
            mVideoCreativeView.mute();
        }
    }

    @Override
    public void unmute() {
        if (mVideoCreativeView != null && mVideoCreativeView.getVolume() == 0) {
            mVideoCreativeView.unMute();
        }
    }

    @Override
    public void createOmAdSession() {
        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();
        if (omAdSessionManager == null) {
            OXLog.error(TAG, "Error creating AdSession. OmAdSessionManager is null");
            return;
        }

        AdConfiguration adConfiguration = mModel.getAdConfiguration();
        omAdSessionManager.initVideoAdSession(mModel.getAdVerifications(), adConfiguration.getContentUrl());
        startOmSession();
    }

    @Override
    public void startViewabilityTracker() {
        VisibilityTrackerOption visibilityTrackerOption = new VisibilityTrackerOption(NativeEventTracker.EventType.IMPRESSION);

        mCreativeVisibilityTracker = new CreativeVisibilityTracker(getCreativeView(),
                                                                   visibilityTrackerOption);
        mCreativeVisibilityTracker.setVisibilityTrackerListener((result) -> {
            if (result.isVisible() && result.shouldFireImpression()) {
                mModel.trackVideoEvent(VideoAdEvent.Event.AD_IMPRESSION);
                mCreativeVisibilityTracker.stopVisibilityCheck();
                mCreativeVisibilityTracker = null;
            }
        });
        mCreativeVisibilityTracker.startVisibilityCheck(mContextReference.get());
    }

    @Override
    public void destroy() {
        super.destroy();

        if (mVideoCreativeView != null) {
            mVideoCreativeView.destroy();
        }

        if (mVideoDownloadTask != null) {
            mVideoDownloadTask.cancel(true);
        }
    }

    @Override
    public boolean isDisplay() {
        return false;
    }

    @Override
    public boolean isVideo() {
        return true;
    }

    /**
     * @return true if {@link #mPreloadedVideoFilePath} is not empty and file exists in filesDir, false otherwise.
     */
    @Override
    public boolean isResolved() {
        if (mContextReference.get() != null && !TextUtils.isEmpty(mPreloadedVideoFilePath)) {
            File file = new File(mContextReference.get().getFilesDir(), mPreloadedVideoFilePath);
            return file.exists();
        }
        return false;
    }

    @Override
    public boolean isEndCard() {
        return false;
    }

    @Override
    public void onVideoInterstitialClosed() {
        if (mVideoCreativeView != null) {
            mVideoCreativeView.destroy();
        }
        if (getCreativeViewListener() != null) {
            getCreativeViewListener().creativeDidComplete(this);
        }
    }

    public long getVideoSkipOffset() {
        return mModel.getSkipOffset();
    }

    @Override
    public void trackVideoEvent(VideoAdEvent.Event event) {
        mModel.trackVideoEvent(event);
    }

    @Override
    @NonNull
    public VideoCreativeModel getCreativeModel() {
        return mModel;
    }

    private void loadContinued() {
        try {
            createCreativeView();
        }
        catch (AdException e) {
            getResolutionListener().creativeFailed(e);
            return;
        }
        setCreativeView(mVideoCreativeView);
        //VideoView has been created. Send adDidLoad() to pubs
        onReadyForDisplay();
    }

    // Helper method to reduce duplicate code in the subclass RewardedVideoCreative
    private void createCreativeView() throws AdException {
        Uri videoUri = null;

        final Context context = mContextReference.get();
        if (context != null) {
            final AdConfiguration adConfiguration = mModel.getAdConfiguration();
            mVideoCreativeView = new VideoCreativeView(context, this);
            mVideoCreativeView.setBroadcastId(adConfiguration.getBroadcastId());

            // Get the preloaded video from device file storage
            videoUri = Uri.parse(context.getFilesDir() + (mModel.getMediaUrl()));
        }

        // Show call-to-action overlay right away if click through url is available & end card is not available
        showCallToAction();

        mVideoCreativeView.setCallToActionUrl(mModel.getVastClickthroughUrl());
        mVideoCreativeView.setVastVideoDuration(getMediaDuration());
        mVideoCreativeView.setVideoUri(videoUri);
    }

    private void startOmSession() {
        OmAdSessionManager omAdSessionManager = mWeakOmAdSessionManager.get();

        if (omAdSessionManager == null) {
            OXLog.error(TAG, "startOmSession: Failed. omAdSessionManager is null");
            return;
        }

        if (mVideoCreativeView == null) {
            OXLog.error(TAG, "startOmSession: Failed. VideoCreativeView is null");
            return;
        }

        startOmSession(omAdSessionManager, (View) mVideoCreativeView.getVideoPlayerView());
        mModel.registerActiveOmAdSession(omAdSessionManager);
    }

    private void trackVideoAdStart() {
        if (mVideoCreativeView == null || mVideoCreativeView.getVideoPlayerView() == null) {
            OXLog.error(TAG, "trackVideoAdStart error. mVideoCreativeView or VideoPlayerView is null.");
            return;
        }

        VideoPlayerView plugPlayVideoView = mVideoCreativeView.getVideoPlayerView();
        int duration = plugPlayVideoView.getDuration();
        float volume = plugPlayVideoView.getVolume();

        mModel.trackVideoAdStarted(duration, volume);
    }

    protected void complete() {
        OXLog.debug(TAG, "track 'complete' event");

        mModel.trackVideoEvent(VideoAdEvent.Event.AD_COMPLETE);

        if (mVideoCreativeView != null) {
            mVideoCreativeView.hideVolumeControls();
        }

        // Send it to AdView
        getCreativeViewListener().creativeDidComplete(this);
    }

    protected void showCallToAction() {
        if (!mModel.getAdConfiguration().isBuiltInVideo()
            && Utils.isNotBlank(mModel.getVastClickthroughUrl())
            && !mModel.hasEndCard()) {
            mVideoCreativeView.showCallToAction();
        }
    }

    private void notifyCreativeViewListener(VideoAdEvent.Event trackingEvent) {
        final CreativeViewListener creativeViewListener = getCreativeViewListener();

        switch (trackingEvent) {
            case AD_START:
                trackVideoAdStart();
                break;
            case AD_CLICK:
                creativeViewListener.creativeWasClicked(this, mVideoCreativeView.getCallToActionUrl());
                break;
            case AD_RESUME:
                creativeViewListener.creativeResumed(this);
                break;
            case AD_PAUSE:
                creativeViewListener.creativePaused(this);
                break;
        }
    }

    private void notifyVolumeChanged(float volume) {
        final CreativeViewListener creativeViewListener = getCreativeViewListener();

        if (volume == 0) {
            creativeViewListener.creativeMuted(this);
        }
        else {
            creativeViewListener.creativeUnMuted(this);
        }
    }

    private static class VideoCreativeVideoPreloadListener implements FileDownloadListener {

        private WeakReference<VideoCreative> mWeakVideoCreative;

        VideoCreativeVideoPreloadListener(VideoCreative videoCreative) {
            mWeakVideoCreative = new WeakReference<>(videoCreative);
        }

        @Override
        public void onFileDownloaded(String shortenedPath) {
            VideoCreative videoCreative = mWeakVideoCreative.get();
            if (videoCreative == null) {
                OXLog.warn(TAG, "VideoCreative is null");
                return;
            }

            videoCreative.mPreloadedVideoFilePath = shortenedPath;
            videoCreative.mModel.setMediaUrl(shortenedPath);
            videoCreative.loadContinued();
        }

        @Override
        public void onFileDownloadError(String error) {
            VideoCreative videoCreative = mWeakVideoCreative.get();
            if (videoCreative == null) {
                OXLog.warn(TAG, "VideoCreative is null");
                return;
            }

            videoCreative.getResolutionListener().creativeFailed(new AdException(AdException.INTERNAL_ERROR, "Preloading failed: " + error));
        }
    }
}
