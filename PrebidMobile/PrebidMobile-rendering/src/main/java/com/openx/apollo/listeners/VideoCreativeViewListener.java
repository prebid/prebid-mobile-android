package com.openx.apollo.listeners;

import com.openx.apollo.errors.AdException;
import com.openx.apollo.models.internal.InternalPlayerState;
import com.openx.apollo.video.VideoAdEvent;

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
