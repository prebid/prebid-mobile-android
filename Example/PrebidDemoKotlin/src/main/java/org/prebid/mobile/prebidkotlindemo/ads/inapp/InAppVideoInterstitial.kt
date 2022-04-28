package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.content.Context
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import java.util.*

object InAppVideoInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(context: Context, configId: String, storedAuctionResponse: String) {
        adUnit = InterstitialAdUnit(
            context,
            configId,
            EnumSet.of(AdUnitFormat.VIDEO)
        )
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit?.setInterstitialAdUnitListener(object :
            InterstitialAdUnitListener {
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