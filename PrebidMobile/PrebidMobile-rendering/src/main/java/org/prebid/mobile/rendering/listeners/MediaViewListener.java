package org.prebid.mobile.rendering.listeners;

import org.prebid.mobile.rendering.bidding.data.ntv.MediaView;
import org.prebid.mobile.rendering.errors.AdException;

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
