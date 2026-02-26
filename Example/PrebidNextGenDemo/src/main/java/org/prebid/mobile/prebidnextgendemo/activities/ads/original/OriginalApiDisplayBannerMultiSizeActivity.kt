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
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize

import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.Signals
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity

class OriginalApiDisplayBannerMultiSizeActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_demo_app_original_api_banner_multisize"
        const val CONFIG_ID = "prebid-demo-banner-multisize"
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
        val adSize = AdSize(WIDTH, HEIGHT)
        val adRequestBuilder = BannerAdRequest.Builder(AD_UNIT_ID, adSize)

        adWrapperView.addView(adView)

        adUnit = BannerAdUnit(CONFIG_ID, WIDTH, HEIGHT)
        adUnit?.setAutoRefreshInterval(refreshTimeSeconds)

        val parameters = BannerParameters()
        parameters.api = listOf(Signals.Api.MRAID_3, Signals.Api.OMID_1)
        adUnit?.bannerParameters = parameters

        // For multi-size request
        adUnit?.addAdditionalSize(728, 90)

        adUnit?.fetchDemand(adRequestBuilder) {
            adView.loadAd(adRequestBuilder.build(), object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    super.onAdLoaded(ad)
                    Log.d(TAG, "Ad loaded.")
                    // Resize the banner. Start on main thread
                    lifecycleScope.launch(Dispatchers.Main) {
                        AdViewUtils.findPrebidCreativeSize(
                            adView,
                            object : AdViewUtils.PbFindSizeListener {
                                override fun success(width: Int, height: Int) {
                                    Log.d(TAG, "Resize: $width x $height")
                                    adView.resize(AdSize(width, height))
                                }

                                override fun failure(error: PbFindSizeError) {
                                    Log.e(TAG, "Resize failed")
                                }
                            })
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    super.onAdFailedToLoad(adError)
                    Log.e(TAG, "Ad failed to load: $adError")
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }

}
