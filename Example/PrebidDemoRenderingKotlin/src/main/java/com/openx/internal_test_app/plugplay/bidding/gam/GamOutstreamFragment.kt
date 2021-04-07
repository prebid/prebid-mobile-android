package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.apollo.bidding.enums.VideoPlacementType
import com.openx.apollo.bidding.parallel.BannerView
import com.openx.apollo.eventhandlers.GamBannerEventHandler
import com.openx.internal_test_app.R

class GamOutstreamFragment : GamBannerFragment() {
    override val layoutRes: Int = R.layout.fragment_bidding_banner_video

    override fun initBanner(configId: String?, eventHandler: GamBannerEventHandler): BannerView {
        val bannerView = BannerView(requireContext(), configId, eventHandler)
        bannerView.videoPlacementType = VideoPlacementType.IN_BANNER
        return bannerView
    }
}