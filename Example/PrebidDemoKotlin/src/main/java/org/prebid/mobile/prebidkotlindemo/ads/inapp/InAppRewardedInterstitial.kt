package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.content.Context
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.RewardedAdUnit
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener

object InAppRewardedInterstitial {

    private var adUnit: RewardedAdUnit? = null

    fun create(context: Context, configId: String,storedAuctionResponse:String) {
        adUnit = RewardedAdUnit(context, configId)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
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