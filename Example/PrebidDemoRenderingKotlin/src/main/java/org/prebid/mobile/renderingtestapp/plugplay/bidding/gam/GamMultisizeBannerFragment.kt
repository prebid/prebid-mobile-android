package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.rendering.bidding.data.AdSize

class GamMultisizeBannerFragment : GamBannerFragment() {

    override fun getAdditionalOxbBannerSizeArray() = arrayOf(AdSize(728, 90))

    override fun getGamAdSizeArray(initialSize: AdSize) =
            arrayOf(initialSize, AdSize(728, 90))
}