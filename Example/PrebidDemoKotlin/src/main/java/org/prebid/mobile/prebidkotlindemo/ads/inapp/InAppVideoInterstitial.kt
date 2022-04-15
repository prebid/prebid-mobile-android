package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.content.Context
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.rendering.errors.AdException
import java.util.*

object InAppVideoInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(context: Context, configId: String, storedAuctionResponse: String) {
        adUnit = InterstitialAdUnit(context, configId, EnumSet.of(AdUnitFormat.VIDEO))
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
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
        adUnit = null
    }

}