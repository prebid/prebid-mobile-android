package org.prebid.mobile.eventhandlers

import android.app.Activity
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.OnUserEarnedRewardListener
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAdEventCallback
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.LogUtil
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.eventhandlers.utils.Utils
import org.prebid.mobile.rendering.bidding.data.bid.Bid

/**
 * Internal wrapper of rewarded ad from GAM SDK.
 */
internal class RewardedAdWrapper(
    private val adUnitId: String,
    private val listener: NextAdEventListener,
) : AdLoadCallback<RewardedAd>, RewardedAdEventCallback, OnUserEarnedRewardListener {
    companion object {
        private val TAG: String = RewardedAdWrapper::class.java.getSimpleName()
        const val KEY_METADATA: String = "AdTitle"
    }

    private var rewardedAd: RewardedAd? = null

    override fun onAdLoaded(ad: RewardedAd) {
        super.onAdLoaded(ad)
        rewardedAd = ad
        rewardedAd?.adEventCallback = this
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Loaded())
            if (metadataContainsAdEvent()) {
                listener.onEvent(AdEvent.AppEvent())
            }
        }
    }

    private fun metadataContainsAdEvent(): Boolean {
        try {
            if (rewardedAd == null) {
                LogUtil.debug(
                    TAG,
                    "metadataContainsAdEvent: Failed to process. RewardedAd is null."
                )
                return false
            }

            val adMetadata = rewardedAd?.getAdMetadata()
            return Constants.APP_EVENT == adMetadata?.getString(KEY_METADATA)
        } catch (throwable: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable))
        }
        return false
    }

    override fun onAdFailedToLoad(adError: LoadAdError) {
        super.onAdFailedToLoad(adError)
        notifyErrorListener(adError.code.value)
    }

    private fun notifyErrorListener(errorCode: Int) {
        val adEvent = AdEvent.Failed(errorCode)

        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(adEvent)
        }
    }

    override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
        super.onAdFailedToShowFullScreenContent(fullScreenContentError)
        notifyErrorListener(fullScreenContentError.code.value)
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Displayed())
        }
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Closed())
        }
    }

    override fun onUserEarnedReward(reward: RewardItem) {
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Reward())
            listener.onEvent(AdEvent.Closed())
        }
    }

    fun loadAd(bid: Bid?) {
        try {
            val requestBuilder = AdRequest.Builder(adUnitId)

            bid?.let {
                val targetingMap = HashMap(bid.getPrebid().targeting)
                Utils.handleCustomTargetingUpdate(requestBuilder, targetingMap)
            }

            RewardedAd.load(
                requestBuilder.build(),
                this
            )
        } catch (throwable: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable))
        }
    }

    fun isLoaded() = rewardedAd != null

    fun show(activity: Activity) {
        if (rewardedAd == null) {
            LogUtil.error(TAG, "show: Failed! Rewarded ad is null.")
            return
        }
        try {
            rewardedAd?.show(activity, this)
        } catch (throwable: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable))
        }
    }

    fun getRewardItem(): RewardItem? {
        return rewardedAd?.getRewardItem()
    }
}