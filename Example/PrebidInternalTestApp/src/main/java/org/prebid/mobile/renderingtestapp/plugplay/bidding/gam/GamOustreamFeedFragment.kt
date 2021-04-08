package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseFeedFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter
import org.prebid.mobile.renderingtestapp.utils.adapters.OXBFeedGamAdapter

class GamOustreamFeedFragment : BaseFeedFragment() {
    override fun initFeedAdapter(): BaseFeedAdapter {
        return OXBFeedGamAdapter(requireContext(), width, height, configId, adUnitId)
    }
}