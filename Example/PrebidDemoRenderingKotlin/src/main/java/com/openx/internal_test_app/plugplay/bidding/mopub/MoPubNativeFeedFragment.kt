package com.openx.internal_test_app.plugplay.bidding.mopub

import com.openx.internal_test_app.plugplay.bidding.base.BaseFeedFragment
import com.openx.internal_test_app.utils.adapters.BaseFeedAdapter
import com.openx.internal_test_app.utils.adapters.MoPubNativeFeedAdapter

class MoPubNativeFeedFragment : BaseFeedFragment() {
    override fun initFeedAdapter(): BaseFeedAdapter {
        return MoPubNativeFeedAdapter(requireContext(), configId, adUnitId, getNativeAdConfig())
    }
}