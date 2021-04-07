package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.internal_test_app.R
import kotlinx.android.synthetic.main.fragment_bidding_banner_in_layout.*

class PpmBannerInLayoutFragment : PpmBannerFragment() {

    override val layoutRes = R.layout.fragment_bidding_banner_in_layout

    override fun initAd(): Any? {
        bannerView = oxBannerView
        bannerView?.setBannerListener(this)
        return bannerView
    }
}