package com.openx.apollo.video;

import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.CreativeModel;
import com.openx.apollo.models.internal.InternalPlayerState;
import com.openx.apollo.networking.tracking.TrackingManager;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.video.vast.AdVerifications;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoCreativeModel extends CreativeModel {

    private static String TAG = VideoCreativeModel.class.getSimpleName();

    private HashMap<VideoAdEvent.Event, ArrayList<String>> mVideoEventUrls = new HashMap<>();
    private String mMediaUrl;

    //interstitial video: media duration
    private long mMediaDuration;
    private String mAuid;
    private long mSkipOffset;

    // interstitial video: click-through URL
    private String mVastClickthroughUrl;
    private AdVerifications mAdVerifications;


    public VideoCreativeModel(TrackingManager trackingManager,
                              OmEventTracker omEventTracker,
                              AdConfiguration adConfiguration) {
        super(trackingManager, omEventTracker, adConfiguration);
    }

    public void registerVideoEvent(VideoAdEvent.Event event, ArrayList<String> urls) {
        mVideoEventUrls.put(event, urls);
    }

    public void trackVideoEvent(VideoAdEvent.Event videoEvent) {
        mOmEventTracker.trackOmVideoAdEvent(videoEvent);
        ArrayList<String> urls = mVideoEventUrls.get(videoEvent);
        if (urls == null) {
            OXLog.debug(TAG, "Event" + videoEvent + " not found");
            return;
        }

        mTrackingManager.fireEventTrackingURLs(urls);

        OXLog.info(TAG, "Video event '" + videoEvent.name() + "' was fired with urls: " + urls.toString());
    }

    public void trackPlayerStateChange(InternalPlayerState changedPlayerState) {
        mOmEventTracker.trackOmPlayerStateChange(changedPlayerState);
    }

    public void trackVideoAdStarted(float duration, float volume) {
        mOmEventTracker.trackVideoAdStarted(duration, volume);
    }

    public void trackNonSkippableStandaloneVideoLoaded(boolean isAutoPlay) {
        mOmEventTracker.trackNonSkippableStandaloneVideoLoaded(isAutoPlay);
    }

    public HashMap<VideoAdEvent.Event, ArrayList<String>> getVideoEventUrls() {
        return mVideoEventUrls;
    }

    public String getMediaUrl() {
        return mMediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        mMediaUrl = mediaUrl;
    }

    public long getMediaDuration() {
        return mMediaDuration;
    }

    public void setMediaDuration(long mediaDuration) {
        mMediaDuration = mediaDuration;
    }

    public long getSkipOffset() {
        return mSkipOffset;
    }

    public void setSkipOffset(long skipOffset) {
        mSkipOffset = skipOffset;
    }

    public String getAuid() {
        return mAuid;
    }

    public void setAuid(String auid) {
        mAuid = auid;
    }

    public String getVastClickthroughUrl() {
        return mVastClickthroughUrl;
    }

    public void setVastClickthroughUrl(String vastClickthroughUrl) {
        mVastClickthroughUrl = vastClickthroughUrl;
    }

    public AdVerifications getAdVerifications() {
        return mAdVerifications;
    }

    public void setAdVerifications(AdVerifications adVerifications) {
        mAdVerifications = adVerifications;
    }
}
