package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.apollo.bidding.parallel.RewardedAdUnit
import com.openx.apollo.eventhandlers.GamRewardedEventHandler
import com.openx.internal_test_app.plugplay.bidding.base.BaseBidRewardedFragment

class GamRewardedVideoFragment : BaseBidRewardedFragment() {

    override fun initRewardedAd(adUnitId: String?, configId: String?) {
        val eventHandler = GamRewardedEventHandler(activity, adUnitId)
        rewardedAdUnit = RewardedAdUnit(context, configId, eventHandler)
        rewardedAdUnit?.setRewardedAdUnitListener(this)
    }
}