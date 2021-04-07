package com.openx.internal_test_app.plugplay.bidding.mopub

import com.openx.apollo.models.ntv.NativeAdConfiguration

class MopubNativeStylesNoAssetsFragment : MopubNativeStylesFragment() {
    override fun getNativeAdConfig(): NativeAdConfiguration? {
        return super.getNativeAdConfig()?.apply {
            assets.clear()
        }
    }
}