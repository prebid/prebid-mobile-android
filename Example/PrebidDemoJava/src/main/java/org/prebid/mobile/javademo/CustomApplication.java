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

package org.prebid.mobile.javademo;

import android.app.Application;
import android.util.Log;

import com.google.android.gms.ads.MobileAds;

import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.javademo.utils.Settings;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class CustomApplication extends Application {

    private static final String TAG = "PrebidCustomApplication";

    @Override
    public void onCreate() {
        super.onCreate();

        Settings.init(this);
        initPrebid();
        initPrebidExternalUserIds();
    }

    private void initPrebid() {
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d");
        PrebidMobile.setCustomStatusEndpoint("https://prebid-server-test-j.prebid.org/status");
        PrebidMobile.initializeSdk(getApplicationContext(), "https://prebid-server-test-j.prebid.org/openrtb2/auction", status -> {
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "SDK initialized successfully!");
            } else {
                Log.e(TAG, "SDK initialization error: " + status.getDescription());
            }
        });

        TargetingParams.setGlobalOrtbConfig(
                "{" +
                        " \"displaymanager\": \"Google\"," +
                        " \"displaymanagerver\": \"" + MobileAds.getVersion() + "\"," +
                        " \"ext\": {" +
                        "   \"myext\": {" +
                        "    \"test\": 1" +
                        "   }" +
                        " }" +
                        "}"
        );
    }

    private void initPrebidExternalUserIds() {
        ExternalUserId id1 = new ExternalUserId("adserver.org", List.of(new ExternalUserId.UniqueId("111111111111", 1)));
        id1.setExt(new HashMap() {{
            put("rtiPartner", "TDID");
        }});

        ExternalUserId id2 = new ExternalUserId("netid.de", List.of(new ExternalUserId.UniqueId("999888777", 2)));
        ExternalUserId id3 = new ExternalUserId("criteo.com", List.of(new ExternalUserId.UniqueId("_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", 3)));
        ExternalUserId id4 = new ExternalUserId("liveramp.com", List.of(new ExternalUserId.UniqueId("AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", 3)));
        ExternalUserId id5 = new ExternalUserId("sharedid.org", List.of(new ExternalUserId.UniqueId("111111111111", 1)));
        id5.setExt(new HashMap() {{
            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
        }});

        TargetingParams.setExternalUserIds(new ArrayList<>(Arrays.asList(id1, id2, id3, id4, id5)));
    }

}
