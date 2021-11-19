package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.app.Activity
import org.prebid.mobile.eventhandlers.GamRewardedEventHandler
import org.prebid.mobile.rendering.bidding.listeners.RewardedAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.RewardedAdUnit
import org.prebid.mobile.rendering.errors.AdException

object InAppGamRewardedInterstitial {

    private var eventHandler: GamRewardedEventHandler? = null
    private var adUnit: RewardedAdUnit? = null

    fun create(activity: Activity, adUnitId: String, configId: String) {
        eventHandler = GamRewardedEventHandler(activity, adUnitId)
        adUnit = RewardedAdUnit(activity, configId, eventHandler)
        adUnit?.setRewardedAdUnitListener(object : RewardedAdUnitListener {
            override fun onAdLoaded(rewardedAdUnit: RewardedAdUnit?) {
                adUnit?.show()
            }

            override fun onAdDisplayed(rewardedAdUnit: RewardedAdUnit?) {}
            override fun onAdFailed(rewardedAdUnit: RewardedAdUnit?, exception: AdException?) {}
            override fun onAdClicked(rewardedAdUnit: RewardedAdUnit?) {}
            override fun onAdClosed(rewardedAdUnit: RewardedAdUnit?) {}
            override fun onUserEarnedReward(rewardedAdUnit: RewardedAdUnit?) {}
        })
        adUnit?.loadAd()
    }

    fun destroy() {
        eventHandler?.destroy()
        eventHandler = null

        adUnit?.destroy()
        adUnit = null
    }

}