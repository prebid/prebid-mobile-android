package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.apollo.models.ntv.NativeAdConfiguration

class GamNativeStylesNoAssetsFragment : GamNativeStylesFragment() {
    override fun getNativeAdConfig(): NativeAdConfiguration? {
        return super.getNativeAdConfig()?.apply {
            assets.clear()
        }
    }
}