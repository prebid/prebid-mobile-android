package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.internal_test_app.plugplay.bidding.base.BaseFeedFragment
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment
import com.openx.internal_test_app.utils.adapters.BaseFeedAdapter
import com.openx.internal_test_app.utils.adapters.OXBFeedAdapter

class PpmFeedVideoFragment : BaseFeedFragment() {

    override fun initFeedAdapter(): BaseFeedAdapter {
        return OXBFeedAdapter(requireContext(), width, height, configId)
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
}