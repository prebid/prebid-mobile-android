package org.prebid.mobile.rendering.listeners;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.video.VideoAdEvent;

public interface VideoCreativeViewListener {

    //error
    void onFailure(AdException error);

    void onReadyForDisplay();

    //complete
    void onDisplayCompleted();

    //start...thirdquartile
    void onEvent(VideoAdEvent.Event trackingEvent);

    void onVolumeChanged(float volume);

    void onPlayerStateChanged(InternalPlayerState state);
}
