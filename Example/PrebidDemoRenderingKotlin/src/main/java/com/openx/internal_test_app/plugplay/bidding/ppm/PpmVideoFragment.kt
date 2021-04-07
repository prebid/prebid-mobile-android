package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.enums.VideoPlacementType
import com.openx.apollo.bidding.listeners.BannerViewListener
import com.openx.apollo.bidding.parallel.BannerView
import com.openx.internal_test_app.R
import kotlinx.android.synthetic.main.fragment_bidding_banner.*

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