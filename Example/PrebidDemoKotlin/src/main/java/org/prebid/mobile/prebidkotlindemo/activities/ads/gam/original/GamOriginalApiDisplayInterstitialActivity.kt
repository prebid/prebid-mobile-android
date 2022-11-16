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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.AdUnit
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class GamOriginalApiDisplayInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-display-interstitial"
        const val CONFIG_ID = "imp-prebid-display-interstitial-320-480"
        const val STORED_RESPONSE = "response-prebid-display-interstitial-320-480"
    }

    private var adUnit: AdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The ID of Mocked Bid Response on PBS. Only for test cases.
        PrebidMobile.setStoredAuctionResponse(STORED_RESPONSE)

        createAd()
    }

    private fun createAd() {
        val requestBuilder = AdManagerAdRequest.Builder()
        val request = requestBuilder.build()

        adUnit = InterstitialAdUnit(CONFIG_ID)
        adUnit?.setAutoRefreshInterval(refreshTimeSeconds)
        adUnit?.fetchDemand(request) {
            val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(adManagerInterstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(adManagerInterstitialAd)
                    adManagerInterstitialAd.show(this@GamOriginalApiDisplayInterstitialActivity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e("GAM", "Ad failed to load: $loadAdError")
                }
            }
            AdManagerInterstitialAd.load(this, AD_UNIT_ID, request, adLoadCallback)
        }
    }


    override fun onDestroy() {
        super.onDestroy()

        adUnit?.stopAutoRefresh()
    }

}
