package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.util.Log
import android.view.ViewGroup
import org.prebid.mobile.AdSize
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.data.VideoPlacementType
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.listeners.BannerViewListener

object InAppVideoBanner {

    private val TAG = InAppVideoBanner::class.simpleName

    private var bannerView: BannerView? = null

    fun create(
        wrapper: ViewGroup,
        width: Int,
        height: Int,
        configId: String,
        storedAuctionResponse: String
    ) {
        bannerView = BannerView(
            wrapper.context,
            configId,
            AdSize(width, height)
        )
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)

        bannerView?.videoPlacementType = VideoPlacementType.IN_BANNER
        bannerView?.setBannerListener(object : BannerViewListener {
            override fun onAdLoaded(bannerView: BannerView?) {
                Log.d(TAG, "Ad loaded")
            }

            override fun onAdDisplayed(bannerView: BannerView?) {
                Log.d(TAG, "Displayed")
            }

            override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
                Log.e(TAG, "Ad failed: ${exception?.message}")
            }

            override fun onAdClicked(bannerView: BannerView?) {}

            override fun onAdClosed(bannerView: BannerView?) {}

        })
        bannerView?.loadAd()

        wrapper.addView(bannerView)
    }

    fun destroy() {
        bannerView?.destroy()
        bannerView = null
    }

}