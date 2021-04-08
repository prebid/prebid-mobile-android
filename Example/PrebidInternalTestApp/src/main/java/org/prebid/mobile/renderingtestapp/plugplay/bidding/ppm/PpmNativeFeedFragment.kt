package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.bidding.display.NativeAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseFeedFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter
import org.prebid.mobile.renderingtestapp.utils.adapters.NativeFeedAdapter

class PpmNativeFeedFragment : BaseFeedFragment() {

    override fun initFeedAdapter(): BaseFeedAdapter {
        val nativeAdUnit = NativeAdUnit(context, configId, getNativeAdConfig()!!)
        return NativeFeedAdapter(requireContext(), nativeAdUnit)
    }

}