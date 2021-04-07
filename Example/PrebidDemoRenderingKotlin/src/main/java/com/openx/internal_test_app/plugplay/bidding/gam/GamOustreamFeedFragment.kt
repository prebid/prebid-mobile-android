package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.internal_test_app.plugplay.bidding.base.BaseFeedFragment
import com.openx.internal_test_app.utils.adapters.BaseFeedAdapter
import com.openx.internal_test_app.utils.adapters.OXBFeedGamAdapter

class GamOustreamFeedFragment : BaseFeedFragment() {
    override fun initFeedAdapter(): BaseFeedAdapter {
        return OXBFeedGamAdapter(requireContext(), width, height, configId, adUnitId)
    }
}