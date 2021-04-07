package com.openx.internal_test_app.plugplay.bidding.mopub

import com.openx.apollo.bidding.data.AdSize

class MopubBannerMultisizeFragment : MopubBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        bannerAdUnit?.addAdditionalSizes(AdSize(728, 90))
        return bannerAdUnit
    }
}