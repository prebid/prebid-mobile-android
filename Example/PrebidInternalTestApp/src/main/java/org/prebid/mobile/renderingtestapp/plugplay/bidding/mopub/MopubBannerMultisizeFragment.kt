package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import org.prebid.mobile.rendering.bidding.data.AdSize

class MopubBannerMultisizeFragment : MopubBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        bannerAdUnit?.addAdditionalSizes(AdSize(728, 90))
        return bannerAdUnit
    }
}