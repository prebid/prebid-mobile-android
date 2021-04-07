package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.bidding.display.NativeAdUnit
import com.openx.internal_test_app.plugplay.bidding.base.BaseFeedFragment
import com.openx.internal_test_app.utils.adapters.BaseFeedAdapter
import com.openx.internal_test_app.utils.adapters.NativeFeedAdapter

class PpmNativeFeedFragment : BaseFeedFragment() {

    override fun initFeedAdapter(): BaseFeedAdapter {
        val nativeAdUnit = NativeAdUnit(context, configId, getNativeAdConfig()!!)
        return NativeFeedAdapter(requireContext(), nativeAdUnit)
    }

}