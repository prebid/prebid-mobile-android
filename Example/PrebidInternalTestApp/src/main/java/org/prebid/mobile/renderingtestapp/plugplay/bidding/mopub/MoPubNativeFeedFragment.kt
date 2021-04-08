package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseFeedFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter
import org.prebid.mobile.renderingtestapp.utils.adapters.MoPubNativeFeedAdapter

class MoPubNativeFeedFragment : BaseFeedFragment() {
    override fun initFeedAdapter(): BaseFeedAdapter {
        return MoPubNativeFeedAdapter(requireContext(), configId, adUnitId, getNativeAdConfig())
    }
}