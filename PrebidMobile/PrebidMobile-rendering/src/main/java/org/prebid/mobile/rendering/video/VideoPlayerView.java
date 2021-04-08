package org.prebid.mobile.rendering.video;

import android.net.Uri;

public interface VideoPlayerView {
    void forceStop();

    void stop();

    void resume();

    void pause();

    void start(float initialVolume);

    void setVastVideoDuration(long duration);

    long getCurrentPosition();

    void destroy();

    void setVideoUri(Uri videoUri);

    int getDuration();

    float getVolume();

    void mute();

    void unMute();

    boolean isPlaying();
}
