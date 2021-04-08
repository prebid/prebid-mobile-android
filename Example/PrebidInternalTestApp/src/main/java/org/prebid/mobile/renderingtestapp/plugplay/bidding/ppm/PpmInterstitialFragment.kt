package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment

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