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
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import com.google.android.exoplayer2.*;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.video.vast.VASTErrorCodes;

public class ExoPlayerView extends PlayerView implements VideoPlayerView {

    private static final String TAG = "ExoPlayerView";
    public static final float DEFAULT_INITIAL_VIDEO_VOLUME = 1.0f;

    @NonNull private final VideoCreativeViewListener videoCreativeViewListener;
    private AdViewProgressUpdateTask adViewProgressUpdateTask;
    private AdUnitConfiguration config;
    private ExoPlayer player;

    private Uri videoUri;

    private long vastVideoDuration = -1;

    public ExoPlayerView(
            Context context,
            @NonNull VideoCreativeViewListener videoCreativeViewListener
    ) {
        super(context);
        this.videoCreativeViewListener = videoCreativeViewListener;
    }

    private final Player.Listener eventListener = new Player.Listener() {

        @Override
        public void onPlayerError(PlaybackException error) {
            videoCreativeViewListener.onFailure(new AdException(
                    AdException.INTERNAL_ERROR,
                    VASTErrorCodes.MEDIA_DISPLAY_ERROR.toString()
            ));
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (player == null) {
                LogUtil.debug(TAG, "onPlayerStateChanged(): Skipping state handling. Player is null");
                return;
            }
            switch (playbackState) {
                case Player.STATE_READY:
                    player.setPlayWhenReady(true);
                    initUpdateTask();
                    break;
                case Player.STATE_ENDED:
                    videoCreativeViewListener.onDisplayCompleted();
                    break;
            }
        }
    };

    @Override
    public void mute() {
        setVolume(0);
    }

    @Override
    public boolean isPlaying() {
        return player != null && player.getPlayWhenReady();
    }

    @Override
    public void unMute() {
        setVolume(DEFAULT_INITIAL_VIDEO_VOLUME);
    }

    @Override
    public void start(float initialVolume) {
        LogUtil.debug(TAG, "start() called");
        initLayout();
        initPlayer(initialVolume);
        preparePlayer(true);
        trackInitialStartEvent();
    }

    @Override
    public void setVastVideoDuration(long duration) {
        vastVideoDuration = duration;
    }

    @Override
    public long getCurrentPosition() {
        if (player == null) {
            return -1;
        }
        return player.getContentPosition();
    }

    @Override
    public void setVideoUri(Uri uri) {
        videoUri = uri;
    }

    @Override
    public int getDuration() {
        return (int) player.getDuration();
    }

    @Override
    public float getVolume() {
        return player.getVolume();
    }

    void setAdUnitConfiguration(AdUnitConfiguration config) {
        this.config = config;
    }

    @Override
    public void resume() {
        LogUtil.debug(TAG, "resume() called");
        preparePlayer(false);
        videoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_RESUME);
    }

    @Override
    public void pause() {
        LogUtil.debug(TAG, "pause() called");
        if (player != null) {
            player.stop();
            videoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_PAUSE);
        }
    }

    @Override
    public void forceStop() {
        destroy();
        videoCreativeViewListener.onDisplayCompleted();
    }

    @Override
    public void destroy() {
        LogUtil.debug(TAG, "destroy() called");
        killUpdateTask();
        if (player != null) {
            player.stop();
            player.removeListener(eventListener);
            setPlayer(null);
            player.release();
            player = null;
        }
    }

    @VisibleForTesting
    void setVolume(float volume) {
        if (player != null && volume >= 0.0f) {
            videoCreativeViewListener.onVolumeChanged(volume);
            player.setVolume(volume);
        }
    }

    @Override
    public void stop() {
        if (player != null) {
            player.stop();
            player.clearMediaItems();
        }
    }

    private void initLayout() {
        RelativeLayout.LayoutParams playerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                RelativeLayout.LayoutParams.MATCH_PARENT);
        playerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(playerLayoutParams);
    }

    private void initPlayer(float initialVolume) {
        if (player != null) {
            LogUtil.debug(TAG, "Skipping initPlayer(): Player is already initialized.");
            return;
        }
        player = new SimpleExoPlayer.Builder(getContext()).build();
        player.addListener(eventListener);
        setPlayer(this.player);
        setUseController(false);
        player.setVolume(initialVolume);
    }

    private void initUpdateTask() {
        if (adViewProgressUpdateTask != null) {
            LogUtil.debug(TAG, "initUpdateTask: AdViewProgressUpdateTask is already initialized. Skipping.");
            return;
        }

        try {
            adViewProgressUpdateTask = new AdViewProgressUpdateTask(
                    videoCreativeViewListener,
                    (int) player.getDuration(),
                    config
            );
            adViewProgressUpdateTask.setVastVideoDuration(vastVideoDuration);
            adViewProgressUpdateTask.execute();
        }
        catch (AdException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    void preparePlayer(boolean resetPosition) {
        ProgressiveMediaSource extractorMediaSource = buildMediaSource(videoUri);
        if (extractorMediaSource == null || player == null) {
            LogUtil.debug(TAG, "preparePlayer(): ExtractorMediaSource or SimpleExoPlayer is null. Skipping prepare.");
            return;
        }
        player.setMediaSource(extractorMediaSource, resetPosition);
        player.prepare();
    }

    private ProgressiveMediaSource buildMediaSource(Uri uri) {
        if (uri == null) {
            return null;
        }
        MediaItem mediaItem = new MediaItem.Builder().setUri(uri).build();
        return new ProgressiveMediaSource.Factory(
                new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "PrebidRenderingSDK")))
                .createMediaSource(mediaItem);
    }

    private void killUpdateTask() {
        LogUtil.debug(TAG, "killUpdateTask() called");
        if (adViewProgressUpdateTask != null) {
            adViewProgressUpdateTask.cancel(true);
            adViewProgressUpdateTask = null;
        }
    }

    private void trackInitialStartEvent() {
        if (videoUri != null && player != null && player.getCurrentPosition() == 0) {
            videoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_CREATIVEVIEW);
            videoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_START);
        }
    }
}
