package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.enums.AdUnitFormat
import com.openx.apollo.bidding.parallel.InterstitialAdUnit
import com.openx.internal_test_app.plugplay.bidding.base.BaseBidInterstitialFragment

open class PpmInterstitialFragment : BaseBidInterstitialFragment() {
    override fun initInterstitialAd(adUnitFormat: AdUnitFormat, adUnitId: String?,
                                    configId: String?, width: Int, height: Int) {
        interstitialAdUnit = if (adUnitFormat == AdUnitFormat.VIDEO){
            InterstitialAdUnit(requireContext(), configId, adUnitFormat)
        }
        else {
            InterstitialAdUnit(requireContext(), configId, AdSize(width, height))
        }
        interstitialAdUnit?.setInterstitialAdUnitListener(this)
    }
}