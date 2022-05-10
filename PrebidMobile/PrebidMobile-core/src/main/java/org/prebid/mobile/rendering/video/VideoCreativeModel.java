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

package org.prebid.mobile.rendering.video;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.video.vast.AdVerifications;

import java.util.ArrayList;
import java.util.HashMap;

public class VideoCreativeModel extends CreativeModel {

    private static String TAG = VideoCreativeModel.class.getSimpleName();

    private HashMap<VideoAdEvent.Event, ArrayList<String>> videoEventUrls = new HashMap<>();
    private String mediaUrl;

    //interstitial video: media duration
    private long mediaDuration;
    private String auid;
    private long skipOffset;

    // interstitial video: click-through URL
    private String vastClickthroughUrl;
    private AdVerifications adVerifications;


    public VideoCreativeModel(
            TrackingManager trackingManager,
            OmEventTracker omEventTracker,
            AdUnitConfiguration adConfiguration
    ) {
        super(trackingManager, omEventTracker, adConfiguration);
    }

    public void registerVideoEvent(
            VideoAdEvent.Event event,
            ArrayList<String> urls
    ) {
        videoEventUrls.put(event, urls);
    }

    public void trackVideoEvent(VideoAdEvent.Event videoEvent) {
        omEventTracker.trackOmVideoAdEvent(videoEvent);
        ArrayList<String> urls = videoEventUrls.get(videoEvent);
        if (urls == null) {
            LogUtil.debug(TAG, "Event" + videoEvent + " not found");
            return;
        }

        trackingManager.fireEventTrackingURLs(urls);

        LogUtil.info(TAG, "Video event '" + videoEvent.name() + "' was fired with urls: " + urls.toString());
    }

    public void trackPlayerStateChange(InternalPlayerState changedPlayerState) {
        omEventTracker.trackOmPlayerStateChange(changedPlayerState);
    }

    public void trackVideoAdStarted(float duration, float volume) {
        omEventTracker.trackVideoAdStarted(duration, volume);
    }

    public void trackNonSkippableStandaloneVideoLoaded(boolean isAutoPlay) {
        omEventTracker.trackNonSkippableStandaloneVideoLoaded(isAutoPlay);
    }

    public HashMap<VideoAdEvent.Event, ArrayList<String>> getVideoEventUrls() {
        return videoEventUrls;
    }

    public String getMediaUrl() {
        return mediaUrl;
    }

    public void setMediaUrl(String mediaUrl) {
        this.mediaUrl = mediaUrl;
    }

    public long getMediaDuration() {
        return mediaDuration;
    }

    public void setMediaDuration(long mediaDuration) {
        this.mediaDuration = mediaDuration;
    }

    public long getSkipOffset() {
        return skipOffset;
    }

    public void setSkipOffset(long skipOffset) {
        this.skipOffset = skipOffset;
    }

    public String getAuid() {
        return auid;
    }

    public void setAuid(String auid) {
        this.auid = auid;
    }

    public String getVastClickthroughUrl() {
        return vastClickthroughUrl;
    }

    public void setVastClickthroughUrl(String vastClickthroughUrl) {
        this.vastClickthroughUrl = vastClickthroughUrl;
    }

    public AdVerifications getAdVerifications() {
        return adVerifications;
    }

    public void setAdVerifications(AdVerifications adVerifications) {
        this.adVerifications = adVerifications;
    }
}
