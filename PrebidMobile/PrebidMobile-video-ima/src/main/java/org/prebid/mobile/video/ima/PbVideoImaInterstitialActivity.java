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

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.ViewGroup;

import com.google.ads.interactivemedia.v3.api.AdsManager;

import static org.prebid.mobile.video.ima.VideoImaInterstitial.VIDEO_IMA_VIEW_ID_KEY;

public class PbVideoImaInterstitialActivity extends Activity {

    private VideoImaView videoImaView;
    private String videoImaViewId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        videoImaViewId = getIntent().getStringExtra(VIDEO_IMA_VIEW_ID_KEY);
        videoImaView = VideoImaInterstitial.getVideoImaView(videoImaViewId);
        videoImaView.addVideoImaDelegate(new ImaAdapter.VideoImaDelegate() {
            @Override
            public void adLoaded() {

            }

            @Override
            public void adStarted() {

            }

            @Override
            public void adSkipped() {

            }

            @Override
            public void adFinished() {

            }

            @Override
            public void adPlayingFailed() {

            }

            @Override
            public void allAdsCompleted() {
                finish();
            }
        });

        setContentView(videoImaView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        videoImaView.setAutoPlayAndShowAd();
    }

    @Override
    public void onResume() {
        AdsManager adsManager = videoImaView.getAdsManager();
        if (adsManager != null) {
            adsManager.resume();
        }
        super.onResume();
    }
    @Override
    public void onPause() {
        AdsManager adsManager = videoImaView.getAdsManager();
        if (adsManager != null) {
            adsManager.pause();
        }
        super.onPause();
    }

    @Override
    protected void onDestroy() {

        videoImaView.reset();
        VideoImaInterstitial.removeVideoImaView(videoImaViewId);
        videoImaView = null;

        super.onDestroy();
    }

}
