package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.enums.AdUnitFormat
import com.openx.apollo.bidding.parallel.InterstitialAdUnit
import com.openx.apollo.eventhandlers.GamInterstitialEventHandler
import com.openx.internal_test_app.plugplay.bidding.base.BaseBidInterstitialFragment

open class GamInterstitialFragment : BaseBidInterstitialFragment() {
    override fun initInterstitialAd(adUnitFormat: AdUnitFormat, adUnitId: String?, configId: String?, width: Int, height: Int) {
        val interstitialEventHandler = GamInterstitialEventHandler(requireContext(), adUnitId)
        interstitialAdUnit = if (adUnitFormat == AdUnitFormat.VIDEO){
            InterstitialAdUnit(requireContext(), configId, adUnitFormat, interstitialEventHandler)
        }
        else {
            InterstitialAdUnit(requireContext(), configId, AdSize(width, height), interstitialEventHandler)
        }

        interstitialAdUnit?.setInterstitialAdUnitListener(this)
    }
}