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
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity

class DisplayInterstitialOriginalApiActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-display-interstitial"
        const val CONFIG_ID = "prebid-demo-display-interstitial-320-480"
    }

    private var adUnit: InterstitialAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // 1. Create InterstitialAdUnit
        adUnit = InterstitialAdUnit(CONFIG_ID, 80, 60)

        // Activate additional impression tracker (for burl)
        adUnit?.activateInterstitialPrebidImpressionTracker()

        // 2. Make a bid request to Prebid Server
        val request = AdRequest.Builder(AD_UNIT_ID)
        adUnit?.fetchDemand(request) {

            // 3. Load an interstitial ad
            InterstitialAd.load(
                request.build(),
                object : AdLoadCallback<InterstitialAd> {
                    override fun onAdLoaded(ad: InterstitialAd) {
                        super.onAdLoaded(ad)
                        ad.show(this@DisplayInterstitialOriginalApiActivity)
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.e("NEXT", "Ad failed to load: $adError")
                    }
                })
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        adUnit?.destroy()
    }
}
