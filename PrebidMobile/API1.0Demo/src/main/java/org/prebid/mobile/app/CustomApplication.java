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

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;

import java.util.ArrayList;
import java.util.List;

import static android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON;
import static android.view.WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED;
import static android.view.WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //init MoPub SDK
        List<String> networksToInit = new ArrayList<String>();
        networksToInit.add("com.mopub.mobileads.VungleRewardedVideo");
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("a935eac11acd416f92640411234fbba6")
                .withNetworksToInit(networksToInit)
                .build();
        MoPub.initializeSdk(this, sdkConfiguration, null);
        //set Prebid Mobile global Settings
        //region PrebidMobile API
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID);
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(getApplicationContext());
        //endregion
        if (BuildConfig.DEBUG) {
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
            this.registerActivityLifecycleCallbacks(new ActivityLifecycleCallbacks() {
                @Override
                public void onActivityCreated(Activity activity, Bundle savedInstanceState) {
                    activity.getWindow().addFlags(FLAG_TURN_SCREEN_ON | FLAG_SHOW_WHEN_LOCKED | FLAG_KEEP_SCREEN_ON);
                }

                @Override
                public void onActivityStarted(Activity activity) {

                }

                @Override
                public void onActivityResumed(Activity activity) {

                }

                @Override
                public void onActivityPaused(Activity activity) {

                }

                @Override
                public void onActivityStopped(Activity activity) {

                }

                @Override
                public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

                }

                @Override
                public void onActivityDestroyed(Activity activity) {

                }
            });
        }
    }
}
