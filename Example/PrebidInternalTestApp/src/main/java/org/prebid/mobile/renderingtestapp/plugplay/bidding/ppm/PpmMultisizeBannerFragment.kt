package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.bidding.data.AdSize

class PpmMultisizeBannerFragment : PpmBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        bannerView?.addAdditionalSizes(AdSize(728, 90))
        return bannerView
    }
}