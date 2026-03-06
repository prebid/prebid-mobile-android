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
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import java.util.EnumSet
import java.util.Random


class OriginalApiMultiformatBannerActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-original-banner-multiformat"
        const val CONFIG_ID_BANNER = "prebid-demo-banner-300-250"
        const val CONFIG_ID_VIDEO = "prebid-demo-video-outstream-original-api"
        const val WIDTH = 300
        const val HEIGHT = 250
    }

    private var adUnit: BannerAdUnit? = null


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

        // 1. Create BannerAdUnit
        adUnit = BannerAdUnit(
            configId,
            WIDTH,
            HEIGHT,
            EnumSet.of(AdUnitFormat.BANNER, AdUnitFormat.VIDEO)
        )
        adUnit?.setAutoRefreshInterval(refreshTimeSeconds)

        // 2. Configure parameters
        val parameters = BannerParameters()
        parameters.api = listOf(Signals.Api.MRAID_3, Signals.Api.OMID_1)
        adUnit?.bannerParameters = parameters
        adUnit?.videoParameters = VideoParameters(listOf("video/mp4"))

        // 3. Create AdView
        val adView = AdView(this)

        // Add Next Gen SDK banner view to the app UI
        adWrapperView.addView(adView)

        // 4. Make a bid request to Prebid Server
        val requestBuilder = BannerAdRequest.Builder(AD_UNIT_ID, AdSize(WIDTH, HEIGHT))
        adUnit?.fetchDemand(requestBuilder) {

            // 5. Load the Ad
            adView.loadAd(requestBuilder.build(), object : AdLoadCallback<BannerAd> {

                override fun onAdLoaded(ad: BannerAd) {
                    super.onAdLoaded(ad)
                    lifecycleScope.launch(Dispatchers.Main) {
                        AdViewUtils.findPrebidCreativeSize(
                            adView,
                            object : AdViewUtils.PbFindSizeListener {
                                override fun success(width: Int, height: Int) {
                                    adView.resize(AdSize(width, height))
                                }

                                override fun failure(error: PbFindSizeError) {}
                            })
                    }
                }
            })
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }
}
