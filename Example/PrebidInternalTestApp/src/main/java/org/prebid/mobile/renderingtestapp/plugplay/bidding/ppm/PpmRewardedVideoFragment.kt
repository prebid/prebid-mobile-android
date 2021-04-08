package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.bidding.parallel.RewardedAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidRewardedFragment

class PpmRewardedVideoFragment : BaseBidRewardedFragment() {
    override fun initRewardedAd(adUnitId: String?, configId: String?) {
        rewardedAdUnit = RewardedAdUnit(context, configId)
        rewardedAdUnit?.setRewardedAdUnitListener(this)
    }
}