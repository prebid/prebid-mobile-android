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

import org.prebid.mobile.ExternalUserId;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.javademo.utils.Settings;

import java.util.ArrayList;
import java.util.HashMap;

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
        PrebidMobile.setPrebidServerHost(
            Host.createCustomHost(
                "https://prebid-server-test-j.prebid.org/openrtb2/auction"
            )
        );
        PrebidMobile.initializeSdk(getApplicationContext(), status -> {
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "SDK initialized successfully!");
            } else {
                Log.e(TAG, "SDK initialization error: " + status.getDescription());
            }
        });
    }

    private void initPrebidExternalUserIds() {
        ArrayList<ExternalUserId> externalUserIdArray = new ArrayList<>();
        externalUserIdArray.add(new ExternalUserId("adserver.org", "111111111111", null, new HashMap<String, Object>() {{
            put("rtiPartner", "TDID");
        }}));
        externalUserIdArray.add(new ExternalUserId("netid.de", "999888777", null, null));
        externalUserIdArray.add(new ExternalUserId("criteo.com", "_fl7bV96WjZsbiUyQnJlQ3g4ckh5a1N", null, null));
        externalUserIdArray.add(new ExternalUserId("liveramp.com", "AjfowMv4ZHZQJFM8TpiUnYEyA81Vdgg", null, null));
        externalUserIdArray.add(new ExternalUserId("sharedid.org", "111111111111", 1, new HashMap<String, Object>() {{
            put("third", "01ERJWE5FS4RAZKG6SKQ3ZYSKV");
        }}));
        PrebidMobile.setExternalUserIds(externalUserIdArray);
    }

}
