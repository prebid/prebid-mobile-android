package com.openx.apollo.views.video;

import androidx.annotation.NonNull;

import com.openx.apollo.bidding.display.VideoView;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.AdDetails;

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
