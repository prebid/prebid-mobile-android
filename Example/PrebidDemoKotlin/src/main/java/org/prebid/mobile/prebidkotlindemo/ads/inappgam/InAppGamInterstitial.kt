package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.app.Activity
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.rendering.errors.AdException

object InAppGamInterstitial {

    private var adUnit: InterstitialAdUnit? = null
    private var eventHandler: GamInterstitialEventHandler? = null

    fun create(
        activity: Activity,
        minPercentageWidth: Int,
        minPercentageHeight: Int,
        adUnitId: String,
        configId: String
    ) {
        eventHandler = GamInterstitialEventHandler(activity, adUnitId)
        adUnit = InterstitialAdUnit(activity, configId, AdSize(minPercentageWidth, minPercentageHeight), eventHandler)
        adUnit?.setInterstitialAdUnitListener(object : InterstitialAdUnitListener {
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

        eventHandler?.destroy()
        eventHandler = null
    }

}