package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.view.ViewGroup
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.parallel.BannerView

object InAppGamBanner {

    private var adView: BannerView? = null

    fun create(wrapper: ViewGroup, autoRefreshTime: Int, width: Int, height: Int, adUnitId: String, configId: String) {
        val eventHandler = GamBannerEventHandler(wrapper.context, adUnitId, AdSize(width, height))
        adView = BannerView(wrapper.context, configId, eventHandler)
        wrapper.addView(adView)

        adView?.setAutoRefreshDelay(autoRefreshTime)
        adView?.loadAd()
    }

    fun destroy() {
        adView?.destroy()
        adView = null
    }

}