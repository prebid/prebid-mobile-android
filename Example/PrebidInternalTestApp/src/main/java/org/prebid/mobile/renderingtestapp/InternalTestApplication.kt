/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.renderingtestapp

import android.content.Context
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.preference.PreferenceManager
import android.webkit.WebView
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication
import com.applovin.sdk.AppLovinSdk
import org.prebid.mobile.Host
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.renderingtestapp.utils.DemoItemProvider
import org.prebid.mobile.renderingtestapp.utils.SourcePicker


class InternalTestApplication : MultiDexApplication() {

    companion object {
        @JvmStatic
        lateinit var instance: InternalTestApplication
            private set
    }

    override fun onCreate() {
        super.onCreate()
        instance = this

        PrebidMobile.setPrebidServerHost(Host.createCustomHost("https://prebid-server-test-j.prebid.org/openrtb2/auction"))
        PrebidMobile.initializeSdk(this, null)
        PrebidMobile.setPrebidServerAccountId(getString(R.string.prebid_account_id_prod))
        PrebidMobile.logLevel = PrebidMobile.LogLevel.DEBUG
        SourcePicker.setBidServerHost(SourcePicker.PBS_SERVER_DOMAIN)

        // Setup mock responses only in mock build

        DemoItemProvider.init(this)
        // Only uncomment while testing memory leaks
        checkKeepConsentSettingsFlag()
        WebView.setWebContentsDebuggingEnabled(true)

        initApplovinMax()
    }

    private fun initApplovinMax() {
        AppLovinSdk.getInstance(this).mediationProvider = "max"
        AppLovinSdk.getInstance(this).initializeSdk { }
        AppLovinSdk.getInstance(this).settings.setVerboseLogging(false);
    }


    private fun enableStrictMode() {
        StrictMode.setThreadPolicy(
            StrictMode.ThreadPolicy.Builder()
                .detectAll()
                .penaltyLog()
                .build()
        )
        StrictMode.setVmPolicy(VmPolicy.Builder()
                .detectLeakedSqlLiteObjects()
                .detectLeakedClosableObjects()
                .penaltyLog()
                .penaltyDeath()
                .build())
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }

    fun clearAdConfigSettings() {
        PreferenceManager.getDefaultSharedPreferences(this).edit().clear().apply()
    }

    private fun checkKeepConsentSettingsFlag() {
        val keepSettingsKey = getString(R.string.key_keep_settings)
        val keepSettings = PreferenceManager.getDefaultSharedPreferences(this)
                .getBoolean(keepSettingsKey, false)

        if (!keepSettings) {
            clearAdConfigSettings()
        }
    }
}