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
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.data.VideoPlacementType
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class GamRenderingApiVideoBannerActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_oxb_300x250_banner"
        const val CONFIG_ID = "imp-prebid-video-outstream"
        const val WIDTH = 300
        const val HEIGHT = 250
    }

    private var adView: BannerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val eventHandler = GamBannerEventHandler(this, AD_UNIT_ID, AdSize(WIDTH, HEIGHT))
        adView = BannerView(this, CONFIG_ID, eventHandler)
        adView?.setAutoRefreshDelay(refreshTimeSeconds)

        // For Video
        adView?.videoPlacementType = VideoPlacementType.IN_BANNER

        adWrapperView.addView(adView)
        adView?.loadAd()
    }


    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
    }

}
