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

package org.prebid.mobile.prebidkotlindemo

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager.LayoutParams.*
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import java.util.*

class CustomApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        //init MoPub SDK
        val networksToInit = ArrayList<String>()
        networksToInit.add("com.mopub.mobileads.VungleRewardedVideo")
        val sdkConfiguration = SdkConfiguration.Builder("a935eac11acd416f92640411234fbba6")
            .withNetworksToInit(networksToInit)
            .build()
        MoPub.initializeSdk(this, sdkConfiguration, null)
        //set Prebid Mobile global Settings
        //region PrebidMobile API
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID)
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS)
        PrebidMobile.setShareGeoLocation(true)
        PrebidMobile.setApplicationContext(applicationContext)
        //endregion
        if (BuildConfig.DEBUG) {
            sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
            this.registerActivityLifecycleCallbacks(object : Application.ActivityLifecycleCallbacks {
                override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                    activity.window.addFlags(FLAG_KEEP_SCREEN_ON)
                }

                override fun onActivityStarted(activity: Activity) {

                }

                override fun onActivityResumed(activity: Activity) {

                }

                override fun onActivityPaused(activity: Activity) {

                }

                override fun onActivityStopped(activity: Activity) {

                }

                override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {

                }

                override fun onActivityDestroyed(activity: Activity) {

                }
            })
        }
    }
}
