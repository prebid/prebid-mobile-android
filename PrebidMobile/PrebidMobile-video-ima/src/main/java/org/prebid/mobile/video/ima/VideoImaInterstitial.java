/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile.video.ima;

import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import org.prebid.mobile.VideoInterstitialAdUnit;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class VideoImaInterstitial implements ImaAdapter.VideoImaDelegate, PbVideoAdDelegate {

    public static final String VIDEO_IMA_VIEW_ID_KEY = "videoImaViewId";
    private static Map<String, VideoImaView> videoImaViewMap = new HashMap<>(1);
    private VideoImaView videoImaView;

    private final Context context;
    @Nullable
    private PbVideoAdInterstitialDelegate videoAdInterstitialDelegate;
    private boolean isReady = false;
    private final String videoImaViewId;

    @Nullable
    static VideoImaView getVideoImaView(String id) {
        return videoImaViewMap.get(id);
    }

    static VideoImaView removeVideoImaView(String id) {
        return videoImaViewMap.remove(id);
    }

    public VideoImaInterstitial(Context context) {
        this.context = context;

        videoImaView = new VideoImaView(context);
        videoImaView.addVideoImaDelegate(this);
        videoImaView.setVideoAdDelegate(this);

        videoImaViewId = UUID.randomUUID().toString();
        videoImaViewMap.put(videoImaViewId, videoImaView);

    }

    public void loadAd(VideoInterstitialAdUnit videoInterstitialAdUnit, String adUnitId) {
        isReady = false;
        videoImaView.loadAdWithoutAutoPlay(videoInterstitialAdUnit, adUnitId);
    }

    public void show() {
        if (!isReady) {
            if (videoAdInterstitialDelegate != null) {
                videoAdInterstitialDelegate.videoAdInterstitialFailed();
            }
            return;
        }

        Intent intent = new Intent(context, PbVideoImaInterstitialActivity.class);
        intent.putExtra(VIDEO_IMA_VIEW_ID_KEY, videoImaViewId);
        this.context.startActivity(intent);
    }

    public void setInterstitialDelegate(PbVideoAdInterstitialDelegate videoAdInterstitialDelegate) {
        this.videoAdInterstitialDelegate = videoAdInterstitialDelegate;
    }

    //VideoImaDelegate
    @Override
    public void adLoaded() {
        isReady = true;
        if (videoAdInterstitialDelegate != null) {
            videoAdInterstitialDelegate.videoAdInterstitialLoaded();
        }
    }

    @Override
    public void adStarted() {
    }

    @Override
    public void adSkipped() {
        if (videoAdInterstitialDelegate != null) {
            videoAdInterstitialDelegate.videoAdInterstitialCancelled();
        }
    }

    @Override
    public void adFinished() {
        if (videoAdInterstitialDelegate != null) {
            videoAdInterstitialDelegate.videoAdInterstitialCompleted();
        }
    }

    @Override
    public void adPlayingFailed() {
        if (videoAdInterstitialDelegate != null) {
            videoAdInterstitialDelegate.videoAdInterstitialFailed();
        }
    }

    @Override
    public void allAdsCompleted() {
    }

    //PbVideoAdDelegate
    @Override
    public void videoAdEvent(PbVideoAdEvent event) {
        if (videoAdInterstitialDelegate != null) {
            videoAdInterstitialDelegate.videoAdEvent(event);
        }
    }

}



