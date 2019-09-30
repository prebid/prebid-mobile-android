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

package org.prebid.mobile.app;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.VideoInterstitialAdUnit;
import org.prebid.mobile.video.ima.PbVideoAdDelegate;
import org.prebid.mobile.video.ima.PbVideoAdEvent;
import org.prebid.mobile.video.ima.PbVideoAdInterstitialDelegate;
import org.prebid.mobile.video.ima.VideoImaInterstitial;

public class VideoInterstitialActivity extends AppCompatActivity implements PbVideoAdDelegate, PbVideoAdInterstitialDelegate {

    private VideoImaInterstitial videoImaInterstitial;
    private Button playAdButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_interstitial);

        setupPrebid();

        playAdButton = findViewById(R.id.showInterstitialAd);
        playAdButton.setEnabled(false);

        videoImaInterstitial = new VideoImaInterstitial(this);
        videoImaInterstitial.setInterstitialDelegate(this);

        final VideoInterstitialAdUnit videoAdUnit = new VideoInterstitialAdUnit("1001_test_video", 640, 480);
        videoImaInterstitial.loadAd(videoAdUnit, "/5300653/test_adunit_vast_pavliuchyk");

        playAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                videoImaInterstitial.show();

                playAdButton.setEnabled(false);
            }
        });

    }

    private void setupPrebid() {
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        Host.CUSTOM.setHostUrl("https://prebid-server.qa.rubiconproject.com/openrtb2/auction");
        PrebidMobile.setPrebidServerAccountId("1001_top_level_video");
        PrebidMobile.setStoredAuctionResponse("1001_video_response");
    }

    @Override
    public void videoAdInterstitialLoaded() {
        playAdButton.setEnabled(true);
    }

    @Override
    public void videoAdInterstitialCancelled() {

    }

    @Override
    public void videoAdInterstitialCompleted() {

    }

    @Override
    public void videoAdInterstitialFailed() {

    }

    @Override
    public void videoAdEvent(PbVideoAdEvent event) {
        Log.d("VideoInterstitial", "videoAd event:" + event.getTypeString());
    }

}




