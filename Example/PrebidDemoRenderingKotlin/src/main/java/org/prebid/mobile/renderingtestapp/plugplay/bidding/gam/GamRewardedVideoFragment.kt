package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.eventhandlers.GamRewardedEventHandler
import org.prebid.mobile.rendering.bidding.parallel.RewardedAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidRewardedFragment

class GamRewardedVideoFragment : BaseBidRewardedFragment() {

    override fun initRewardedAd(adUnitId: String?, configId: String?) {
        val eventHandler = GamRewardedEventHandler(activity, adUnitId)
        rewardedAdUnit = RewardedAdUnit(context, configId, eventHandler)
        rewardedAdUnit?.setRewardedAdUnitListener(this)
    }
}