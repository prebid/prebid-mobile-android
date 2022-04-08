package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import java.util.*

class PpmInterstitialSoundButtonFragment : PpmInterstitialFragment() {

    override fun initInterstitialAd(
        adUnitFormat: AdUnitFormat,
        adUnitId: String?,
        configId: String?,
        width: Int,
        height: Int
    ) {
        interstitialAdUnit = if (adUnitFormat == AdUnitFormat.VIDEO) {
            InterstitialAdUnit(requireContext(), configId, EnumSet.of(adUnitFormat))
        } else {
            InterstitialAdUnit(requireContext(), configId)
        }
        interstitialAdUnit?.apply {
            setInterstitialAdUnitListener(this@PpmInterstitialSoundButtonFragment)
            setIsMuted(false)
            setIsSoundButtonVisible(true)
        }

    }

}