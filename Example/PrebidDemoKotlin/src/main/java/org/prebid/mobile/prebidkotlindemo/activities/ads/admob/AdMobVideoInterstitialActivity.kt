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
package org.prebid.mobile.prebidkotlindemo.activities.ads.admob

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.prebid.mobile.admob.AdMobMediationInterstitialUtils
import org.prebid.mobile.admob.PrebidInterstitialAdapter
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import java.util.*

class AdMobVideoInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "ca-app-pub-1875909575462531/6393291067"
        const val CONFIG_ID = "imp-prebid-video-interstitial-320-480"
    }

    private var adUnit: MediationInterstitialAdUnit? = null
    private var interstitialAd: InterstitialAd? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val extras = Bundle()
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidInterstitialAdapter::class.java, extras)
            .build()
        val mediationUtils = AdMobMediationInterstitialUtils(extras)
        adUnit = MediationInterstitialAdUnit(
            this,
            CONFIG_ID,
            EnumSet.of(AdUnitFormat.VIDEO),
            mediationUtils
        )
        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            InterstitialAd.load(this, AD_UNIT_ID, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitial: InterstitialAd) {
                    interstitialAd = interstitial
                    val mediationAdapter = interstitial.responseInfo.mediationAdapterClassName
                    if (mediationAdapter!!.contains("prebid")) {
                        interstitialAd?.show(this@AdMobVideoInterstitialActivity)
                    }
                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }

}
