package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration

class GamNativeStylesNoAssetsFragment : GamNativeStylesFragment() {
    override fun getNativeAdConfig(): NativeAdConfiguration? {
        return super.getNativeAdConfig()?.apply {
            assets.clear()
        }
    }
}