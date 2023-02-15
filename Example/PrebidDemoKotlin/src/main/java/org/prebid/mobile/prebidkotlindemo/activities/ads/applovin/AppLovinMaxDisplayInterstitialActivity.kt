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
package org.prebid.mobile.prebidkotlindemo.activities.ads.applovin

import android.os.Bundle
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationInterstitialUtils
import com.applovin.mediation.ads.MaxInterstitialAd
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import java.util.*

class AppLovinMaxDisplayInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "393697e649678807"
        const val CONFIG_ID = "imp-prebid-display-interstitial-320-480"
    }

    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var adUnit: MediationInterstitialAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        maxInterstitialAd = MaxInterstitialAd(AD_UNIT_ID, this)
        maxInterstitialAd?.setListener(object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                maxInterstitialAd?.showAd()
            }

            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
        })

        val mediationUtils = MaxMediationInterstitialUtils(maxInterstitialAd)
        adUnit = MediationInterstitialAdUnit(
            this,
            CONFIG_ID,
            EnumSet.of(AdUnitFormat.DISPLAY),
            mediationUtils
        )
        adUnit?.fetchDemand {
            maxInterstitialAd?.loadAd()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
        maxInterstitialAd?.destroy()
    }

}
