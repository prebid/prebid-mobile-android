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
package org.prebid.mobile.prebidnextgendemo.activities.ads.original

import android.os.Bundle
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import java.util.EnumSet
import java.util.Random

class OriginalApiMultiformatInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-intestitial-multiformat"
        const val CONFIG_ID_BANNER = "prebid-demo-display-interstitial-320-480"
        const val CONFIG_ID_VIDEO = "prebid-demo-video-interstitial-320-480-original-api";
    }

    private var adUnit: InterstitialAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val configId = if (Random().nextBoolean()) {
            CONFIG_ID_BANNER
        } else {
            CONFIG_ID_VIDEO
        }

        // 1. Create InterstitialAdUnit
        adUnit = InterstitialAdUnit(configId, EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO))
        adUnit?.setMinSizePercentage(80, 60)
        adUnit?.videoParameters = VideoParameters(listOf("video/mp4"))


        // 2. Make a bid request to Prebid Server
        val requestBuilder = AdRequest.Builder(AD_UNIT_ID)
        adUnit?.fetchDemand(requestBuilder) {

            // 3. Load an interstitial ad
            InterstitialAd.load(
                requestBuilder.build(),
                object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        super.onAdLoaded(ad)
                        ad.show(this@OriginalApiMultiformatInterstitialActivity)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.e(TAG, "Ad failed to load: $adError")
                    }
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }
}
