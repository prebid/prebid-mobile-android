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
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.video.ima.PbVideoAdDelegate;
import org.prebid.mobile.video.ima.PbVideoAdEvent;
import org.prebid.mobile.video.ima.VideoImaView;

public class VideoOutStreamActivity extends AppCompatActivity implements PbVideoAdDelegate {

    private VideoImaView videoImaView;
    private TextView playerConsole;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_outstream);

        setupPrebid();

        videoImaView = findViewById(R.id.videoImaView);
        videoImaView.setVideoAdDelegate(this);
        playerConsole = findViewById(R.id.logConsole);
        playerConsole.setMovementMethod(new ScrollingMovementMethod());

        final VideoAdUnit videoAdUnit = new VideoAdUnit("1001_test_video", 640, 480);

        Button playAdButton = findViewById(R.id.playAdButton);
        playAdButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playerConsole.setText("");
                logMessage("request ad");

                videoImaView.loadAd(videoAdUnit, "/5300653/test_adunit_vast_pavliuchyk");
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
    protected void onDestroy() {

        videoImaView.reset();

        super.onDestroy();
    }

    //PbVideoAdDelegate
    @Override
    public void videoAdEvent(PbVideoAdEvent event) {
        logMessage("videoAd event:" + event.getTypeString());
    }

    //private zone
    private void logMessage(String log) {
        Log.d("VideoOutStreamActivity", log);

        playerConsole.append("\n" + log + "\n");

        final int scrollAmount = playerConsole.getLayout().getLineTop(playerConsole.getLineCount()) - playerConsole.getHeight();
        // if there is no need to scroll, scrollAmount will be <=0
        if (scrollAmount > 0)
            playerConsole.scrollTo(0, scrollAmount);
        else
            playerConsole.scrollTo(0, 0);
    }
}




