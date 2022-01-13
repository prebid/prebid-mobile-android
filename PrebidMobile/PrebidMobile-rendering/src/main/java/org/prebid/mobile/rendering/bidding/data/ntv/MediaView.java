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

package org.prebid.mobile.rendering.bidding.data.ntv;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.bidding.display.VideoView;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.MediaViewListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.AdDetails;
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration;
import org.prebid.mobile.rendering.views.video.VideoViewListener;

public class MediaView extends FrameLayout {
    private static final String TAG = MediaView.class.getSimpleName();

    private final AdConfiguration mAdUnitConfiguration = new AdConfiguration();

    private VideoView mVideoView;
    @Nullable
    private MediaViewListener mMediaViewListener;

    private final VideoViewListener mVideoViewListener = new VideoViewListener() {
        @Override
        public void onLoaded(
            @NonNull
                VideoView videoAdView, AdDetails adDetails) {
            if (mMediaViewListener != null) {
                mMediaViewListener.onVideoLoadingFinished(MediaView.this);
            }
        }

        @Override
        public void onLoadFailed(
            @NonNull
                VideoView videoAdView, AdException error) {
            if (mMediaViewListener != null) {
                mMediaViewListener.onFailure(error);
            }
        }

        @Override
        public void onDisplayed(
            @NonNull
                VideoView videoAdView) {
            if (mMediaViewListener != null) {
                mMediaViewListener.onMediaPlaybackStarted(MediaView.this);
            }
        }

        @Override
        public void onPlayBackCompleted(
            @NonNull
                VideoView videoAdView) {
            if (mMediaViewListener != null) {
                mMediaViewListener.onMediaPlaybackFinished(MediaView.this);
            }
        }

        @Override
        public void onPlaybackPaused() {
            if (mMediaViewListener != null) {
                mMediaViewListener.onMediaPlaybackPaused(MediaView.this);
            }
        }

        @Override
        public void onPlaybackResumed() {
            if (mMediaViewListener != null) {
                mMediaViewListener.onMediaPlaybackResumed(MediaView.this);
            }
        }

        @Override
        public void onVideoUnMuted() {
            if (mMediaViewListener != null) {
                mMediaViewListener.onMediaPlaybackUnMuted(MediaView.this);
            }
        }

        @Override
        public void onVideoMuted() {
            if (mMediaViewListener != null) {
                mMediaViewListener.onMediaPlaybackMuted(MediaView.this);
            }
        }
    };

    public MediaView(
        @NonNull
            Context context) {
        super(context);
        init();
    }

    public MediaView(
        @NonNull
            Context context,
        @Nullable
            AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MediaView(
        @NonNull
            Context context,
        @Nullable
            AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    public void setMediaViewListener(
        @Nullable
            MediaViewListener mediaViewListener) {
        mMediaViewListener = mediaViewListener;
    }

    public void loadMedia(
        @NonNull
            MediaData mediaData) {

        mVideoView.loadAd(mAdUnitConfiguration, mediaData.getVastTag());
    }

    public void setAutoPlay(boolean enabled) {
        mVideoView.setAutoPlay(enabled);
    }

    public void play() {
        mVideoView.play();
    }

    public void pause() {
        mVideoView.pause();
    }

    public void resume() {
        mVideoView.resume();
    }

    public void mute() {
        mVideoView.mute(true);
    }

    public void unMute() {
        mVideoView.mute(false);
    }

    public void destroy() {
        if (mVideoView != null) {
            mVideoView.destroy();
        }
    }

    private void init() {
        try {
            mAdUnitConfiguration.setNativeAdConfiguration(new NativeAdConfiguration());

            mVideoView = new VideoView(getContext(), mAdUnitConfiguration);
            mVideoView.setVideoViewListener(mVideoViewListener);
            addView(mVideoView, LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        }
        catch (AdException exception) {
            exception.printStackTrace();
        }
    }
}
