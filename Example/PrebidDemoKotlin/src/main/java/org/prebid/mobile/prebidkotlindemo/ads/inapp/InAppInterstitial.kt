package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.content.Context
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import java.util.*

object InAppInterstitial {

    private var adUnit: InterstitialAdUnit? = null

    fun create(
        context: Context,
        minPercentageWidth: Int,
        minPercentageHeight: Int,
        configId: String,
        storedAuctionResponse: String,
        adFormats: EnumSet<AdUnitFormat>
    ) {
        adUnit = InterstitialAdUnit(context, configId,adFormats)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit?.setMinSizePercentage(AdSize(minPercentageWidth, minPercentageHeight))
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