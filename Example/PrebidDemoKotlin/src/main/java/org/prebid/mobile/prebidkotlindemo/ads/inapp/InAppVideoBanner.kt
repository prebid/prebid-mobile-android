package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.view.ViewGroup
import org.prebid.mobile.AdSize
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType
import org.prebid.mobile.rendering.bidding.parallel.BannerView

object InAppVideoBanner {

    private var bannerView: BannerView? = null

    fun create(wrapper: ViewGroup, autoRefreshTime: Int, width: Int, height: Int, configId: String) {
        bannerView = BannerView(wrapper.context, configId, AdSize(width, height))
        wrapper.addView(bannerView)

        bannerView?.videoPlacementType = VideoPlacementType.IN_BANNER
        bannerView?.setAutoRefreshDelay(autoRefreshTime)
        bannerView?.loadAd()
    }

    fun destroy() {
        bannerView?.destroy()
        bannerView = null
    }

}