package org.prebid.mobile.prebidkotlindemo.ads.inappadmob

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.admob.AdMobMediationRewardedUtils
import org.prebid.mobile.admob.PrebidRewardedAdapter
import org.prebid.mobile.api.mediation.MediationRewardedVideoAdUnit

object InAppAdMobRewarded {

    private const val TAG = "InAppAdMobRewarded"
    private var rewardedAd: RewardedAd? = null
    private var adUnit: MediationRewardedVideoAdUnit? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val extras = Bundle()
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidRewardedAdapter::class.java, extras)
            .build()
        val mediationUtils = AdMobMediationRewardedUtils(extras)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit = MediationRewardedVideoAdUnit(
            activity,
            configId,
            mediationUtils
        )
        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            RewardedAd.load(activity, adUnitId, request, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d(TAG, "Ad was loaded.")
                    rewardedAd = ad

                    rewardedAd?.show(activity) { rewardItem ->
                        val rewardAmount = rewardItem.amount
                        val rewardType = rewardItem.type
                        Log.d(TAG, "User earned the reward ($rewardAmount, $rewardType)")
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, adError.message)
                    rewardedAd = null
                }
            })
        }

    }

    fun destroy() {
        rewardedAd = null
        adUnit?.destroy()
        adUnit = null
    }

}