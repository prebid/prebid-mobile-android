package org.prebid.mobile.prebidkotlindemo.ads.inappmax

import android.app.Activity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.adapters.prebid.utils.MaxRewardedMediationUtils
import com.applovin.mediation.ads.MaxRewardedAd
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit

object InAppMaxRewarded {

    private var maxRewardedAd: MaxRewardedAd? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String
    ) {
        maxRewardedAd = MaxRewardedAd.getInstance(adUnitId, activity)
        maxRewardedAd?.setListener(createListener())

        val mediationUtils =
            MaxRewardedMediationUtils(maxRewardedAd)
        val adUnit = MediationRewardedVideoAdUnit(
            activity,
            configId,
            mediationUtils
        )

        adUnit.fetchDemand {
            maxRewardedAd?.loadAd()
        }
    }

    fun destroy() {
        maxRewardedAd?.destroy()
    }

    private fun createListener(): MaxRewardedAdListener {
        return object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                maxRewardedAd?.showAd()
            }

            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
            override fun onRewardedVideoStarted(ad: MaxAd?) {}
            override fun onRewardedVideoCompleted(ad: MaxAd?) {}
            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {}
        }
    }

}