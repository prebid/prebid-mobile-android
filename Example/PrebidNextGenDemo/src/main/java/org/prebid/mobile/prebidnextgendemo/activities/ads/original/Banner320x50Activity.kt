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
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.Signals
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity

class Banner320x50Activity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_demo_app_original_api_banner"
        const val CONFIG_ID = "prebid-ita-banner-320-50"
        const val WIDTH = 320
        const val HEIGHT = 50
    }

    private var adUnit: BannerAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val adView = AdView(this)
        val adSize = AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(this, 320)
        val adRequestBuilder = BannerAdRequest.Builder(AD_UNIT_ID, adSize)

        adWrapperView.addView(adView)

        adUnit = BannerAdUnit(CONFIG_ID, WIDTH, HEIGHT)
        setOpenRtbConfig()

        val parameters = BannerParameters()
        parameters.api = listOf(Signals.Api.MRAID_3, Signals.Api.OMID_1)
        adUnit?.bannerParameters = parameters

        // Optional: the activation of the native impression tracker
        adUnit?.activatePrebidImpressionTracker(adView)

        adUnit?.setAutoRefreshInterval(refreshTimeSeconds)
        adUnit?.fetchDemand(adRequestBuilder) {
            adView.loadAd(adRequestBuilder.build(), object : AdLoadCallback<BannerAd> {})
        }
    }

    /**
     * Optional. Sets additional parameters.
     */
    private fun setOpenRtbConfig() {
        adUnit?.impOrtbConfig = """
            {
              "bidfloor": 0.01,
              "banner": {
                "battr": [1,2,3,4]
              }
            }
        """
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }

}
