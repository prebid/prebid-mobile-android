package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.app.Activity
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.rendering.errors.AdException
import java.util.*

object InAppGamVideoInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(activity: Activity, adUnitId: String, configId: String,storedAuctionResponse: String) {
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        val eventHandler = GamInterstitialEventHandler(activity, adUnitId)
        adUnit = InterstitialAdUnit(activity, configId, EnumSet.of(AdUnitFormat.VIDEO), eventHandler)
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
    }

}