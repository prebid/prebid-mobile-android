package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.content.Context
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.rendering.errors.AdException

object InAppInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(
        context: Context,
        minPercentageWidth: Int,
        minPercentageHeight: Int,
        configId: String,
        storedAuctionResponse: String
    ) {
        adUnit = InterstitialAdUnit(context, configId)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit?.setMinSizePercentage(AdSize(minPercentageWidth, minPercentageHeight))
        adUnit?.setInterstitialAdUnitListener(object : InterstitialAdUnitListener {
            override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit?) {
                adUnit?.show()
            }

            override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit?) {}
            override fun onAdFailed(
                interstitialAdUnit: InterstitialAdUnit?,
                exception: AdException?
            ) {
            }

            override fun onAdClicked(interstitialAdUnit: InterstitialAdUnit?) {}
            override fun onAdClosed(interstitialAdUnit: InterstitialAdUnit?) {}
        })
        adUnit?.loadAd()
    }

    fun destroy() {
        adUnit?.destroy()
        PrebidMobile.setStoredAuctionResponse(null)
        adUnit = null
    }

}