package org.prebid.mobile.prebidkotlindemo.ads.inappmopub

import android.app.Activity
import com.mopub.common.MoPub
import com.mopub.common.MoPubReward
import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubRewardedAdListener
import com.mopub.mobileads.MoPubRewardedAdManager
import com.mopub.mobileads.MoPubRewardedAds
import org.prebid.mobile.rendering.bidding.display.MoPubRewardedVideoAdUnit

object InAppMoPubRewardedInterstitial {

    private var adUnit: MoPubRewardedVideoAdUnit? = null

    fun create(activity: Activity, adUnitId: String, configId: String, keywordsMap: HashMap<String, String>) {
        val builder = SdkConfiguration.Builder(adUnitId)
        MoPubRewardedAdManager.init(activity)
        MoPubRewardedAdManager.updateActivity(activity)
        MoPubRewardedAds.setRewardedAdListener(object : MoPubRewardedAdListener {
            override fun onRewardedAdLoadSuccess(adUnitId: String) {
                MoPubRewardedAds.showRewardedAd(adUnitId)
            }

            override fun onRewardedAdClicked(adUnitId: String) {}
            override fun onRewardedAdClosed(adUnitId: String) {}
            override fun onRewardedAdCompleted(adUnitIds: Set<String?>, reward: MoPubReward) {}
            override fun onRewardedAdLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {}
            override fun onRewardedAdShowError(adUnitId: String, errorCode: MoPubErrorCode) {}
            override fun onRewardedAdStarted(adUnitId: String) {}
        })

        adUnit = MoPubRewardedVideoAdUnit(activity, adUnitId, configId)
        MoPub.initializeSdk(activity, builder.build()) {
            adUnit?.fetchDemand(keywordsMap) {
                val keywordsString = convertMapToMoPubKeywords(keywordsMap)
                val params = MoPubRewardedAdManager.RequestParameters(keywordsString)

                MoPubRewardedAds.loadRewardedAd(adUnitId, params, null)
            }
        }
    }

    fun destroy() {
        adUnit?.destroy()
        adUnit = null
    }

    private fun convertMapToMoPubKeywords(keywordMap: Map<String, String>): String? {
        val result = StringBuilder()
        for (key in keywordMap.keys) {
            result.append(key).append(":").append(keywordMap[key]).append(",")
        }
        if (result.isNotEmpty()) {
            result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }

}