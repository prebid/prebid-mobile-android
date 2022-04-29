package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.view.ViewGroup
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.VideoPlacementType
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.eventhandlers.GamBannerEventHandler

object InAppGamVideoBanner {

    private var bannerView: BannerView? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val eventHandler = GamBannerEventHandler(wrapper.context, adUnitId, AdSize(width, height))
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)

        bannerView =
            BannerView(wrapper.context, configId, eventHandler)
        bannerView?.videoPlacementType = VideoPlacementType.IN_BANNER
        bannerView?.setAutoRefreshDelay(autoRefreshTime)
        wrapper.addView(bannerView)

        bannerView?.loadAd()
    }

    fun destroy() {
        bannerView?.destroy()
        bannerView = null
    }

}