package org.prebid.mobile.rendering.video;

import android.content.Context;
import android.net.Uri;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.VideoCreativeViewListener;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.video.vast.VASTErrorCodes;

public class ExoPlayerView extends PlayerView implements VideoPlayerView {
    private static final String TAG = "ExoPlayerView";
    public static final float DEFAULT_INITIAL_VIDEO_VOLUME = 1.0f;

    @NonNull
    private final VideoCreativeViewListener mVideoCreativeViewListener;
    private AdViewProgressUpdateTask mAdViewProgressUpdateTask;
    private SimpleExoPlayer mPlayer;

    private Uri mVideoUri;

    private long mVastVideoDuration = -1;

    public ExoPlayerView(Context context,
                         @NonNull
                             VideoCreativeViewListener videoCreativeViewListener) {
        super(context);
        mVideoCreativeViewListener = videoCreativeViewListener;
    }

    private final Player.EventListener mEventListener = new Player.EventListener() {

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            mVideoCreativeViewListener.onFailure(new AdException(AdException.INTERNAL_ERROR, VASTErrorCodes.MEDIA_DISPLAY_ERROR.toString()));
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (mPlayer == null) {
                OXLog.debug(TAG, "onPlayerStateChanged(): Skipping state handling. Player is null");
                return;
            }
            switch (playbackState) {
                case Player.STATE_READY:
                    mPlayer.setPlayWhenReady(true);
                    initUpdateTask();
                    break;
                case Player.STATE_ENDED:
                    mVideoCreativeViewListener.onDisplayCompleted();
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
        return mPlayer != null && mPlayer.getPlayWhenReady();
    }

    @Override
    public void unMute() {
        setVolume(DEFAULT_INITIAL_VIDEO_VOLUME);
    }

    @Override
    public void start(float initialVolume) {
        OXLog.debug(TAG, "start() called");
        initLayout();
        initPlayer(initialVolume);
        preparePlayer(true);
        trackInitialStartEvent();
    }

    @Override
    public void setVastVideoDuration(long duration) {
        mVastVideoDuration = duration;
    }

    @Override
    public long getCurrentPosition() {
        if (mPlayer == null) {
            return -1;
        }
        return mPlayer.getContentPosition();
    }

    @Override
    public void setVideoUri(Uri uri) {
        mVideoUri = uri;
    }

    @Override
    public int getDuration() {
        return (int) mPlayer.getDuration();
    }

    @Override
    public float getVolume() {
        return mPlayer.getVolume();
    }

    @Override
    public void resume() {
        OXLog.debug(TAG, "resume() called");
        preparePlayer(false);
        mVideoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_RESUME);
    }

    @Override
    public void pause() {
        OXLog.debug(TAG, "pause() called");
        if (mPlayer != null) {
            mPlayer.stop();
            mVideoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_PAUSE);
        }
    }

    @Override
    public void forceStop() {
        destroy();
        mVideoCreativeViewListener.onDisplayCompleted();
    }

    @Override
    public void destroy() {
        OXLog.debug(TAG, "destroy() called");
        killUpdateTask();
        if (mPlayer != null) {
            mPlayer.stop();
            mPlayer.removeListener(mEventListener);
            setPlayer(null);
            mPlayer.release();
            mPlayer = null;
        }
    }

    @VisibleForTesting
    void setVolume(float volume) {
        if (mPlayer != null && volume >= 0.0f) {
            mVideoCreativeViewListener.onVolumeChanged(volume);
            mPlayer.setVolume(volume);
        }
    }

    @Override
    public void stop() {
        if (mPlayer != null) {
            mPlayer.stop(true);
        }
    }

    private void initLayout() {
        RelativeLayout.LayoutParams playerLayoutParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                                         RelativeLayout.LayoutParams.MATCH_PARENT);
        playerLayoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        setLayoutParams(playerLayoutParams);
    }

    private void initPlayer(float initialVolume) {
        if (mPlayer != null) {
            OXLog.debug(TAG, "Skipping initPlayer(): Player is already initialized.");
            return;
        }
        mPlayer = ExoPlayerFactory.newSimpleInstance(getContext());
        mPlayer.addListener(mEventListener);
        setPlayer(mPlayer);
        setUseController(false);
        mPlayer.setVolume(initialVolume);
    }

    private void initUpdateTask() {
        if (mAdViewProgressUpdateTask != null) {
            OXLog.debug(TAG, "initUpdateTask: AdViewProgressUpdateTask is already initialized. Skipping.");
            return;
        }

        try {
            mAdViewProgressUpdateTask = new AdViewProgressUpdateTask(mVideoCreativeViewListener, (int) mPlayer.getDuration());
            mAdViewProgressUpdateTask.setVastVideoDuration(mVastVideoDuration);
            mAdViewProgressUpdateTask.execute();
        }
        catch (AdException e) {
            e.printStackTrace();
        }
    }

    @VisibleForTesting
    void preparePlayer(boolean resetPosition) {
        ExtractorMediaSource extractorMediaSource = buildMediaSource(mVideoUri);
        if (extractorMediaSource == null || mPlayer == null) {
            OXLog.debug(TAG, "preparePlayer(): ExtractorMediaSource or SimpleExoPlayer is null. Skipping prepare.");
            return;
        }
        mPlayer.prepare(extractorMediaSource, resetPosition, true);
    }

    private ExtractorMediaSource buildMediaSource(Uri uri) {
        if (uri == null) {
            return null;
        }
        return new ExtractorMediaSource.Factory(
            new DefaultDataSourceFactory(getContext(), Util.getUserAgent(getContext(), "OpenXSdk")))
            .createMediaSource(uri);
    }

    private void killUpdateTask() {
        OXLog.debug(TAG, "killUpdateTask() called");
        if (mAdViewProgressUpdateTask != null) {
            mAdViewProgressUpdateTask.cancel(true);
            mAdViewProgressUpdateTask = null;
        }
    }

    private void trackInitialStartEvent() {
        if (mVideoUri != null && mPlayer != null && mPlayer.getCurrentPosition() == 0) {
            mVideoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_CREATIVEVIEW);
            mVideoCreativeViewListener.onEvent(VideoAdEvent.Event.AD_START);
        }
    }
}
