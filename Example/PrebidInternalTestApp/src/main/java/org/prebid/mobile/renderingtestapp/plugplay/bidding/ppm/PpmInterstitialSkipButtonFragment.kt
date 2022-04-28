package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment
import org.prebid.mobile.api.data.Position
import java.util.*

class PpmInterstitialSkipButtonFragment : BaseBidInterstitialFragment() {

    override fun initInterstitialAd(
        adUnitFormat: AdUnitFormat,
        adUnitId: String?,
        configId: String?,
        width: Int,
        height: Int
    ) {
        interstitialAdUnit = if (adUnitFormat == AdUnitFormat.VIDEO) {
            InterstitialAdUnit(
                requireContext(),
                configId,
                EnumSet.of(adUnitFormat)
            )
        } else {
            InterstitialAdUnit(requireContext(), configId)
        }
        interstitialAdUnit?.setInterstitialAdUnitListener(this)
        interstitialAdUnit?.setSkipDelay(5)
        interstitialAdUnit?.setSkipButtonArea(0.30)
        interstitialAdUnit?.setSkipButtonPosition(Position.TOP_LEFT)
    }

}