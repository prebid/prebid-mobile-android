package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration

class MopubNativeStylesNoAssetsFragment : MopubNativeStylesFragment() {
    override fun getNativeAdConfig(): NativeAdConfiguration? {
        return super.getNativeAdConfig()?.apply {
            assets.clear()
        }
    }
}