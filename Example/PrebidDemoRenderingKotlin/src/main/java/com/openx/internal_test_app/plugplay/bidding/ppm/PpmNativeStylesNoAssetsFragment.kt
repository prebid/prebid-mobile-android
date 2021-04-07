package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.models.ntv.NativeAdConfiguration

class PpmNativeStylesNoAssetsFragment : PpmNativeStylesFragment() {
    override fun getNativeAdConfig(): NativeAdConfiguration? {
        return super.getNativeAdConfig()?.apply {
            assets.clear()
        }
    }
}