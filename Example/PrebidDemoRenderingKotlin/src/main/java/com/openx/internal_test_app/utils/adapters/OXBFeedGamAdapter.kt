package com.openx.internal_test_app.utils.adapters

import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.enums.VideoPlacementType
import com.openx.apollo.bidding.parallel.BannerView
import com.openx.apollo.eventhandlers.GamBannerEventHandler
import com.openx.internal_test_app.utils.OpenRtbConfigs

class OXBFeedGamAdapter(context: Context,
                        width: Int,
                        height: Int,
                        configId: String,
                        val adUnitId: String) : OXBFeedAdapter(context, width, height, configId) {

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        val eventHandler = GamBannerEventHandler(container.context, adUnitId,
                *getGamAdSizeArray(AdSize(width, height)))

        if (videoView == null) {
            videoView = BannerView(container.context, configId, eventHandler)
            videoView?.videoPlacementType = VideoPlacementType.IN_FEED
            val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
            layoutParams.gravity = Gravity.CENTER
            videoView?.layoutParams = layoutParams

            OpenRtbConfigs.setImpContextDataTo(videoView)
        }
        videoView?.loadAd()
        return videoView
    }

    private fun getGamAdSizeArray(initialSize: AdSize) = arrayOf(initialSize)

}