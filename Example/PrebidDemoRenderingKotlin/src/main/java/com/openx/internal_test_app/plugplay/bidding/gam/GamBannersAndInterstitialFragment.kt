package com.openx.internal_test_app.plugplay.bidding.gam

import android.widget.Button
import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.parallel.BannerView
import com.openx.apollo.bidding.parallel.InterstitialAdUnit
import com.openx.apollo.eventhandlers.GamBannerEventHandler
import com.openx.apollo.eventhandlers.GamInterstitialEventHandler
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.bidding.ppm.PpmBannersWithInterstitialFragment

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