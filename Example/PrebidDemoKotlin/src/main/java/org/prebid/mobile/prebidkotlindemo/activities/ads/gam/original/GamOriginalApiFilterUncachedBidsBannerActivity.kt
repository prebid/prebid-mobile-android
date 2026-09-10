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
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.ResultCode
import org.prebid.mobile.Signals
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.data.BidInfo
import org.prebid.mobile.api.original.PrebidAdUnit
import org.prebid.mobile.api.original.PrebidRequest
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

/**
 * Demonstrates [PrebidMobile.setFilterOutUncachedBids] with the Original API.
 *
 * When enabled, the SDK drops bids that Prebid Server failed to cache, so the ad server never
 * receives targeting for a bid whose creative could not be fetched from Prebid Cache. There are two
 * outcomes worth handling:
 *
 * 1. Some cached demand survived. The result code is [ResultCode.SUCCESS] and the ad request proceeds
 *    as usual. If the bid Prebid Server designated as the winner was the one dropped, a lower-priced
 *    cached bid is promoted in its place and [BidInfo.isTopBidFiltered] is `true`. That is still a
 *    success: the ad serves, only the yield is lower. Keep loading the ad and track the flag if you
 *    want to measure the impact.
 * 2. Every returned bid failed to cache. The result code is [ResultCode.NO_CACHED_BIDS] and no Prebid
 *    targeting is attached, so the request falls through to the ad server's own demand.
 *
 * This uses [PrebidAdUnit], which reports a full [BidInfo] while still applying targeting to the ad
 * request. `BannerAdUnit.fetchDemand(adObject, listener)` reports only a [ResultCode], so it cannot
 * surface [BidInfo.isTopBidFiltered].
 */
class GamOriginalApiFilterUncachedBidsBannerActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid_demo_app_original_api_banner"
        const val CONFIG_ID = "prebid-ita-banner-320-50"
        const val WIDTH = 320
        const val HEIGHT = 50
        private const val TAG = "FilterUncachedBids"
    }

    private var prebidAdUnit: PrebidAdUnit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // 1. Ignore bids Prebid Server could not cache. Original API only: the Rendering API renders
        // creatives from the bid markup and never fetches them from Prebid Cache.
        PrebidMobile.setFilterOutUncachedBids(true)

        // 2. Create a GAM banner view
        val adView = AdManagerAdView(this)
        adView.adUnitId = AD_UNIT_ID
        adView.setAdSizes(AdSize(WIDTH, HEIGHT))
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        adView.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {}
                })
            }
        }
        adWrapperView.addView(adView)

        // 3. Create a PrebidAdUnit and a banner PrebidRequest
        prebidAdUnit = PrebidAdUnit(CONFIG_ID)

        val bannerParameters = BannerParameters()
        bannerParameters.adSizes = mutableSetOf(org.prebid.mobile.AdSize(WIDTH, HEIGHT))
        bannerParameters.api = listOf(Signals.Api.MRAID_3, Signals.Api.OMID_1)

        val prebidRequest = PrebidRequest()
        prebidRequest.setBannerParameters(bannerParameters)

        // 4. Make a bid request to Prebid Server
        val gamRequest = AdManagerAdRequest.Builder().build()
        prebidAdUnit?.fetchDemand(gamRequest, prebidRequest) { bidInfo ->
            handleFetchDemandResult(bidInfo)

            // 5. Load the GAM ad. The request is made in every case: without Prebid targeting it
            // simply falls through to the ad server's own demand.
            adView.loadAd(gamRequest)
        }
    }

    private fun handleFetchDemandResult(bidInfo: BidInfo) {
        when (bidInfo.resultCode) {
            ResultCode.SUCCESS -> {
                // Cached demand is attached to the request. If the top bid was the one dropped,
                // a cheaper cached bid took its place, so the ad still serves.
                if (bidInfo.isTopBidFiltered) {
                    Log.i(TAG, "Top bid was dropped for a failed cache entry; a lower-priced cached bid was promoted.")
                }
            }

            ResultCode.NO_CACHED_BIDS -> {
                // Prebid Server returned bids but none of them were cached, so none of them could
                // have rendered. No Prebid targeting is attached to the request.
                Log.w(TAG, "No bid returned by Prebid Server was cached.")
            }

            else -> Log.e(TAG, "Prebid demand fetch failed: ${bidInfo.resultCode}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        prebidAdUnit?.destroy()
        // The setting is global, so restore it when leaving the example.
        PrebidMobile.setFilterOutUncachedBids(false)
    }

}
