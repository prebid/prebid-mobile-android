package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.view.ViewGroup
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.eventhandlers.GamBannerEventHandler

object InAppGamBanner {

    private var adView: BannerView? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        val eventHandler = GamBannerEventHandler(wrapper.context, adUnitId, AdSize(width, height))
        adView =
            BannerView(wrapper.context, configId, eventHandler)
        wrapper.addView(adView)
        adView?.setAutoRefreshDelay(autoRefreshTime)
        adView?.loadAd()
    }

    fun destroy() {
        adView?.destroy()
        adView = null
    }

}