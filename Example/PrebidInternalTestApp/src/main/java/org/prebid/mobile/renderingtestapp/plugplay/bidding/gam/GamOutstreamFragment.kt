package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.renderingtestapp.R

class GamOutstreamFragment : GamBannerFragment() {
    override val layoutRes: Int = R.layout.fragment_bidding_banner_video

    override fun initBanner(configId: String?, eventHandler: GamBannerEventHandler): BannerView {
        val bannerView = BannerView(requireContext(), configId, eventHandler)
        bannerView.videoPlacementType = VideoPlacementType.IN_BANNER
        return bannerView
    }
}