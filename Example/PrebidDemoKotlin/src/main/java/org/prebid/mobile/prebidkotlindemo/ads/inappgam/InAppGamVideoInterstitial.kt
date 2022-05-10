package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.app.Activity
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import java.util.*

object InAppGamVideoInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(activity: Activity, adUnitId: String, configId: String,storedAuctionResponse: String) {
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        val eventHandler = GamInterstitialEventHandler(activity, adUnitId)
        adUnit = InterstitialAdUnit(
            activity,
            configId,
            EnumSet.of(AdUnitFormat.VIDEO),
            eventHandler
        )
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