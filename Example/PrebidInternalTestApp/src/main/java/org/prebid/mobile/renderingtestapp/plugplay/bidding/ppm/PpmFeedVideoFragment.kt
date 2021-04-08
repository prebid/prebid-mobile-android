package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseFeedFragment
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter
import org.prebid.mobile.renderingtestapp.utils.adapters.OXBFeedAdapter

class PpmFeedVideoFragment : BaseFeedFragment() {

    override fun initFeedAdapter(): BaseFeedAdapter {
        return OXBFeedAdapter(requireContext(), width, height, configId)
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
}