package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.view.ViewGroup
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.rendering.BannerView

object InAppBanner {

    private var adView: BannerView? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        configId: String,
        storedAuctionResponse:String
    ) {
        adView = BannerView(
            wrapper.context,
            configId,
            AdSize(width, height)
        )
        wrapper.addView(adView)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adView?.setAutoRefreshDelay(autoRefreshTime)
        adView?.loadAd()
    }

    fun destroy() {
        adView?.destroy()
        adView = null
    }

}