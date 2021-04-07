package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.bidding.parallel.RewardedAdUnit
import com.openx.internal_test_app.plugplay.bidding.base.BaseBidRewardedFragment

class PpmRewardedVideoFragment : BaseBidRewardedFragment() {
    override fun initRewardedAd(adUnitId: String?, configId: String?) {
        rewardedAdUnit = RewardedAdUnit(context, configId)
        rewardedAdUnit?.setRewardedAdUnitListener(this)
    }
}