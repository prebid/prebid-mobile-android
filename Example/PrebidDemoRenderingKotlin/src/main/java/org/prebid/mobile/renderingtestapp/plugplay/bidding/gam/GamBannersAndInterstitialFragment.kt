package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import android.widget.Button
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm.PpmBannersWithInterstitialFragment

class GamBannersAndInterstitialFragment : PpmBannersWithInterstitialFragment() {
    override fun initBannerView(configId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): BannerView {
        val adSize = AdSize(BANNER_WIDTH, BANNER_HEIGHT)
        val eventHandler = GamBannerEventHandler(requireContext(), getString(R.string.adunit_gam_banner_320_50_app_event), adSize)
        val bannerView = BannerView(requireContext(), configId, eventHandler)
        bannerView.setAutoRefreshDelay(refreshIntervalSec)
        bannerView.setBannerListener(getBannerAdListener(configId, refreshIntervalSec, impressionCounterButton))
        return bannerView
    }

    override fun initInterstitialAdUnit(configId: String): InterstitialAdUnit {
        val eventHandler = GamInterstitialEventHandler(requireContext(), getString(R.string.adunit_gam_interstitial_320_480_app_event))
        val interstitialAdUnit = InterstitialAdUnit(requireContext(), configId, AdSize(30, 30), eventHandler)
        interstitialAdUnit.setInterstitialAdUnitListener(getInterstitialAdListener())
        interstitialAdUnit.loadAd()
        return interstitialAdUnit
    }
}