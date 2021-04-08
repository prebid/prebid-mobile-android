package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.enums.VideoPlacementType
import org.prebid.mobile.rendering.bidding.listeners.BannerViewListener
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.renderingtestapp.R

open class PpmVideoFragment : PpmBannerFragment(), BannerViewListener {
    override val layoutRes = R.layout.fragment_bidding_banner_video

    override fun initAd(): Any? {
        bannerView = BannerView(requireContext(), configId, AdSize(width, height))
        bannerView?.videoPlacementType = VideoPlacementType.IN_BANNER
        bannerView?.setBannerListener(this)
        viewContainer.addView(bannerView)
        return bannerView
    }
}