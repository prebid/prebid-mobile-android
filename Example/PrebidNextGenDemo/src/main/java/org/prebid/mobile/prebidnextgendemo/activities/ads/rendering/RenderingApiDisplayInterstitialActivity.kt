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
package org.prebid.mobile.prebidnextgendemo.activities.ads.rendering

import android.os.Bundle
import android.util.Log
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import org.prebid.mobile.eventhandlers.NextInterstitialEventHandler
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity

class RenderingApiDisplayInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_oxb_html_interstitial"
        const val CONFIG_ID = "prebid-demo-display-interstitial-320-480"
    }

    private var adUnit: InterstitialAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val eventHandler = NextInterstitialEventHandler(this, AD_UNIT_ID)
        adUnit = InterstitialAdUnit(this, CONFIG_ID, eventHandler)
        adUnit?.setInterstitialAdUnitListener(createListener(adUnit))
        adUnit?.loadAd()
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }


    private fun createListener(adUnit: InterstitialAdUnit?): InterstitialAdUnitListener =
        object : InterstitialAdUnitListener {
            override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit?) {
                adUnit?.show()
            }

            override fun onAdFailed(
                interstitialAdUnit: InterstitialAdUnit?,
                exception: AdException?,
            ) {
                Log.e(TAG, "Ad failed: $exception")
            }

            override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit?) {}
            override fun onAdClicked(interstitialAdUnit: InterstitialAdUnit?) {}
            override fun onAdClosed(interstitialAdUnit: InterstitialAdUnit?) {}
        }

}
