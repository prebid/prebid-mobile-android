package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration

class PpmNativeStylesNoAssetsFragment : PpmNativeStylesFragment() {
    override fun getNativeAdConfig(): NativeAdConfiguration? {
        return super.getNativeAdConfig()?.apply {
            assets.clear()
        }
    }
}