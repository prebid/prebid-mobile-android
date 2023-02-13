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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.rendering

import android.os.Bundle
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import java.util.*

class GamRenderingApiVideoInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "imp-prebid-video-interstitial-320-480"
    }

    private var adUnit: InterstitialAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val eventHandler = GamInterstitialEventHandler(this, AD_UNIT_ID)
        adUnit = InterstitialAdUnit(this, CONFIG_ID, EnumSet.of(AdUnitFormat.VIDEO), eventHandler)
        adUnit?.setInterstitialAdUnitListener(object :
            InterstitialAdUnitListener {
            override fun onAdLoaded(adUnit: InterstitialAdUnit?) {
                adUnit?.show()
            }

            override fun onAdDisplayed(adUnit: InterstitialAdUnit?) {}
            override fun onAdFailed(adUnit: InterstitialAdUnit?, exception: AdException?) {}
            override fun onAdClicked(adUnit: InterstitialAdUnit?) {}
            override fun onAdClosed(adUnit: InterstitialAdUnit?) {}
        })
        adUnit?.loadAd()
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }

}
