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

package org.prebid.mobile.prebidnextgendemo

import android.app.Application
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.MobileAds
import com.google.android.libraries.ads.mobile.sdk.initialization.InitializationConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.TargetingParams
import org.prebid.mobile.api.data.InitializationStatus
import org.prebid.mobile.prebidnextgendemo.utils.Settings

class CustomApplication : Application() {

    companion object {
        private const val TAG = "PrebidCustomApplication"
    }

    override fun onCreate() {
        super.onCreate()
        initPrebidSDK()
        TargetingParams.setSubjectToGDPR(true)
        Settings.init(this)
        initNextGenSdk()
    }

    private fun initPrebidSDK() {
        PrebidMobile.setPrebidServerAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")
        PrebidMobile.setCustomStatusEndpoint("https://prebid-server-test-j.prebid.org/status")
        PrebidMobile.initializeSdk(applicationContext, "https://prebid-server-test-j.prebid.org/openrtb2/auction") { status ->
            if (status == InitializationStatus.SUCCEEDED) {
                Log.d(TAG, "SDK initialized successfully!")
            } else {
                Log.e(TAG, "SDK initialization error: $status\n${status.description}")
            }
        }
        PrebidMobile.setShareGeoLocation(true)

        TargetingParams.setGlobalOrtbConfig(
            """
                {
                  "displaymanager": "Google",
                  "displaymanagerver": "${MobileAds.getVersion()}",
                  "ext": {
                    "myext": {
                      "test": 1
                    }
                  }
                }
            """.trimIndent()
        )
    }

    private fun initNextGenSdk() {
        val backgroundScope = CoroutineScope(Dispatchers.IO)
        backgroundScope.launch {
            // Initialize GMA Next-Gen SDK on a background thread.
            MobileAds.initialize(
                this@CustomApplication,
                InitializationConfig.Builder("ca-app-pub-1875909575462531~6255590079").build()
            ) {
                // Adapter initialization is complete.
            }
            // SDK initialization is complete. If you don't want to wait for bidding adapters to finish
            // initializing, start loading ads now.
        }
    }
}
