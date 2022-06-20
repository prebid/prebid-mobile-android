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
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
import android.webkit.WebView
import com.applovin.sdk.AppLovinSdk
import com.applovin.sdk.AppLovinSdkConfiguration
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration

import org.prebid.mobile.PrebidMobile

class CustomApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        initPrebidSDK()
        initAdMob()
        initApplovinMax()
        if (BuildConfig.DEBUG) {
            activateKeepScreenOnFlag()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WebView.setWebContentsDebuggingEnabled(true)
        }
    }



    private fun initPrebidSDK() {
        AdTypesRepository.usePrebidServer()
        PrebidMobile.initializeSdk(applicationContext, null)
        PrebidMobile.setShareGeoLocation(true)
    }

    private fun initAdMob() {
        MobileAds.initialize(this) {
            Log.d("MobileAds", "Initialization complete.")
        }
        val configuration = RequestConfiguration.Builder().setTestDeviceIds(
            listOf("38250D98D8E3A07A2C03CD3552013B29")
        ).build()
        MobileAds.setRequestConfiguration(configuration)
    }

    private fun initApplovinMax() {
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk { }
        AppLovinSdk.getInstance(this).settings.setVerboseLogging(false)
    }

    private fun activateKeepScreenOnFlag() {
        sendBroadcast(Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS))
        this.registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityCreated(activity: Activity, savedInstanceState: Bundle?) {
                activity.window.addFlags(FLAG_KEEP_SCREEN_ON)
            }

            override fun onActivityStarted(activity: Activity) {}
            override fun onActivityResumed(activity: Activity) {}
            override fun onActivityPaused(activity: Activity) {}
            override fun onActivityStopped(activity: Activity) {}
            override fun onActivitySaveInstanceState(activity: Activity, outState: Bundle) {}
            override fun onActivityDestroyed(activity: Activity) {}
        })
    }

}
