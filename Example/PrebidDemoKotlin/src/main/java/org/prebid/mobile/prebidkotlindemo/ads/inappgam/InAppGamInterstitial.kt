package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.app.Activity
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler

object InAppGamInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(
        activity: Activity,
        minPercentageWidth: Int,
        minPercentageHeight: Int,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val eventHandler = GamInterstitialEventHandler(activity, adUnitId)
        adUnit =
            InterstitialAdUnit(activity, configId, eventHandler)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit?.setMinSizePercentage(AdSize(minPercentageWidth, minPercentageHeight))
        adUnit?.setInterstitialAdUnitListener(object :
            InterstitialAdUnitListener {
            override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit?) {
                adUnit?.show()
            }

            override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit?) {}
            override fun onAdFailed(interstitialAdUnit: InterstitialAdUnit?, exception: AdException?) {}
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