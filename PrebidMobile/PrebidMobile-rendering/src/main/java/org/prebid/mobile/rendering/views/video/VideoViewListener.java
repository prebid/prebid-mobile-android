package org.prebid.mobile.rendering.views.video;

import androidx.annotation.NonNull;

import org.prebid.mobile.rendering.bidding.display.VideoView;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdDetails;

public abstract class VideoViewListener {
    public void onLoaded(
        @NonNull
            VideoView videoAdView, AdDetails adDetails) {}

    public void onLoadFailed(
        @NonNull
            VideoView videoAdView, AdException error) {}

    public void onDisplayed(
        @NonNull
            VideoView videoAdView) {}

    public void onPlayBackCompleted(
        @NonNull
            VideoView videoAdView) {}

    public void onClickThroughOpened(
        @NonNull
            VideoView videoAdView) {}

    public void onClickThroughClosed(
        @NonNull
            VideoView videoAdView) {}

    public void onPlaybackPaused() {}

    public void onPlaybackResumed() {}

    public void onVideoUnMuted() {}

    public void onVideoMuted() {}
}
