package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment

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