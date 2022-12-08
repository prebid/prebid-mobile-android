/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.rendering

import android.widget.Button
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm.PpmBannersWithInterstitialFragment

class GamBannersAndInterstitialFragment : PpmBannersWithInterstitialFragment() {
    override fun initBannerView(configId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): BannerView {
        val adSize = AdSize(BANNER_WIDTH, BANNER_HEIGHT)
        val eventHandler =
            GamBannerEventHandler(requireContext(), getString(R.string.adunit_gam_banner_320_50_app_event), adSize)
        val bannerView =
            BannerView(requireContext(), configId, eventHandler)
        bannerView.setAutoRefreshDelay(refreshIntervalSec)
        bannerView.setBannerListener(getBannerAdListener(configId, refreshIntervalSec, impressionCounterButton))
        return bannerView
    }

    override fun initInterstitialAdUnit(configId: String): InterstitialAdUnit {
        val eventHandler = GamInterstitialEventHandler(
            requireActivity(),
            getString(R.string.adunit_gam_interstitial_320_480_app_event)
        )
        val interstitialAdUnit = InterstitialAdUnit(
            requireContext(),
            configId,
            eventHandler
        )
        interstitialAdUnit.setMinSizePercentage(AdSize(30, 30))
        interstitialAdUnit.setInterstitialAdUnitListener(getInterstitialAdListener())
        interstitialAdUnit.loadAd()
        return interstitialAdUnit
    }
}