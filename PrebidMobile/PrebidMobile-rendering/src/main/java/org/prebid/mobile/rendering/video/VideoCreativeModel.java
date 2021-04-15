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

import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.internal.InternalPlayerState;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.video.vast.AdVerifications;

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
