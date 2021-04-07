package com.openx.apollo.listeners;

import com.openx.apollo.bidding.data.ntv.MediaView;
import com.openx.apollo.errors.AdException;

public interface MediaViewListener {
    void onMediaPlaybackStarted(MediaView mediaView);

    void onMediaPlaybackFinished(MediaView mediaView);

    void onMediaPlaybackPaused(MediaView mediaView);

    void onMediaPlaybackResumed(MediaView mediaView);

    void onMediaPlaybackMuted(MediaView mediaView);

    void onMediaPlaybackUnMuted(MediaView mediaView);

    void onVideoLoadingFinished(MediaView mediaView);

    void onFailure(AdException adException);
}
