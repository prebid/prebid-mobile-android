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
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.data.VideoPlacementType
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.eventhandlers.nextgen.NextGenBannerEventHandler
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity


class RenderingApiVideoBannerActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_oxb_300x250_banner"
        const val CONFIG_ID = "prebid-demo-video-outstream"
        const val WIDTH = 300
        const val HEIGHT = 250
    }

    private var adView: BannerView? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val adUnitId = intent.getStringExtra(EXTRA_AD_UNIT_ID) ?: AD_UNIT_ID
        val configId = intent.getStringExtra(EXTRA_CONFIG_ID) ?: CONFIG_ID
        val width = intent.getIntExtra(EXTRA_WIDTH, WIDTH)
        val height = intent.getIntExtra(EXTRA_HEIGHT, HEIGHT)
        val eventHandler = NextGenBannerEventHandler(this, adUnitId, AdSize(width, height))
        adView = BannerView(this, configId, eventHandler)
        adView?.setAutoRefreshDelay(refreshTimeSeconds)
        adView?.setBannerListener(createListener())

        // For Video
        adView?.videoPlacementType = VideoPlacementType.IN_BANNER

        adWrapperView.addView(adView)
        adView?.loadAd()
    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
    }

    private fun createListener(): BannerViewListener = object : BannerViewListener {
        override fun onAdLoaded(bannerView: BannerView?) {
            Log.d(TAG, "Ad loaded")
            events.loaded(true)
        }

        override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
            Log.e(TAG, "Ad failed: $exception")
            events.failed(true)
        }

        override fun onAdDisplayed(bannerView: BannerView?) {
            events.displayed(true)
        }

        override fun onAdClicked(bannerView: BannerView?) {
            events.clicked(true)
        }

        override fun onAdClosed(bannerView: BannerView?) {
            events.closed(true)
        }
    }

}
