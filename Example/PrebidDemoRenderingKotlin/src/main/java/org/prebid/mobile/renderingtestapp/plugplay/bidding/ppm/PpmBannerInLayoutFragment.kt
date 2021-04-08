package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import kotlinx.android.synthetic.main.fragment_bidding_banner_in_layout.*
import org.prebid.mobile.renderingtestapp.R

class PpmBannerInLayoutFragment : PpmBannerFragment() {

    override val layoutRes = R.layout.fragment_bidding_banner_in_layout

    override fun initAd(): Any? {
        bannerView = oxBannerView
        bannerView?.setBannerListener(this)
        return bannerView
    }
}