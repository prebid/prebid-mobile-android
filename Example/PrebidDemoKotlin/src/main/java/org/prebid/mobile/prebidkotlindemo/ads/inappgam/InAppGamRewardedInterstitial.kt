package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.app.Activity
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.RewardedAdUnit
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener
import org.prebid.mobile.eventhandlers.GamRewardedEventHandler

object InAppGamRewardedInterstitial {

    private var adUnit: RewardedAdUnit? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        val eventHandler = GamRewardedEventHandler(activity, adUnitId)
        adUnit = RewardedAdUnit(activity, configId, eventHandler)
        adUnit?.setRewardedAdUnitListener(object :
            RewardedAdUnitListener {
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
        adUnit?.destroy()
        adUnit = null
    }

}