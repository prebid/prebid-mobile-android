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
import android.os.Build;
import android.webkit.WebView;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.javademo.utils.ScreenUtils;

public class CustomApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PrebidMobile.setShareGeoLocation(true);

        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d");
        PrebidMobile.setPrebidServerHost(
            Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction")
        );
        PrebidMobile.initializeSdk(getApplicationContext(), null);

        ScreenUtils.closeSystemWindowsAndKeepScreenOn(this);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true);
        }
    }

}
