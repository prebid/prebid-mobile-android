package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.view.ViewGroup
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType
import org.prebid.mobile.rendering.bidding.parallel.BannerView

object InAppGamVideoBanner {

    private var bannerView: BannerView? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        adUnitId: String,
        configId: String
    ) {
        val eventHandler = GamBannerEventHandler(wrapper.context, adUnitId, AdSize(width, height))

        bannerView = BannerView(wrapper.context, configId, eventHandler)
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