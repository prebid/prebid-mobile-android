package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.view.ViewGroup
import org.prebid.mobile.AdSize
import org.prebid.mobile.rendering.bidding.parallel.BannerView

object InAppBanner {

    private var adView: BannerView? = null

    fun create(wrapper: ViewGroup, autoRefreshTime: Int, width: Int, height: Int, configId: String) {
        adView = BannerView(
            wrapper.context,
            configId,
            AdSize(width, height)
        )
        wrapper.addView(adView)

        adView?.setAutoRefreshDelay(autoRefreshTime)
        adView?.loadAd()
    }

    fun destroy() {
        adView?.destroy()
        adView = null
    }

}