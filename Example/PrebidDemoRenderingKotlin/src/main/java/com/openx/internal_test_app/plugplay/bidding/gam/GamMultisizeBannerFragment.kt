package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.apollo.bidding.data.AdSize

class GamMultisizeBannerFragment : GamBannerFragment() {

    override fun getAdditionalOxbBannerSizeArray() = arrayOf(AdSize(728, 90))

    override fun getGamAdSizeArray(initialSize: AdSize) =
            arrayOf(initialSize, AdSize(728, 90))
}