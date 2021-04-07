package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.bidding.data.AdSize

class PpmMultisizeBannerFragment : PpmBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        bannerView?.addAdditionalSizes(AdSize(728, 90))
        return bannerView
    }
}