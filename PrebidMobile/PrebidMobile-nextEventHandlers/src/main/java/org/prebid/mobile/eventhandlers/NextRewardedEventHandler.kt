package org.prebid.mobile.eventhandlers

import android.app.Activity
import android.os.Handler
import android.os.Looper
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardItem
import org.prebid.mobile.LogUtil
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import org.prebid.mobile.rendering.bidding.interfaces.RewardedEventHandler
import org.prebid.mobile.rendering.bidding.listeners.RewardedVideoEventListener
import org.prebid.mobile.rendering.interstitial.rewarded.Reward
import java.lang.ref.WeakReference

/**
 * Rewarded event handler for communication between Prebid rendering API and the Next-Gen SDK.
 * @param activity    Android activity
 * @param adUnitId    the GAM ad unit id for the rewarded ad unit
 */
class NextRewardedEventHandler(
    activity: Activity,
    private var adUnitId: String,
) : RewardedEventHandler, NextAdEventListener {

    companion object {
        private val TAG: String = NextRewardedEventHandler::class.java.getSimpleName()
        private const val TIMEOUT_APP_EVENT_MS: Long = 600
    }

    private var rewardedAd: RewardedAdWrapper? = null
    private val activityWeakReference = WeakReference(activity)

    private var listener: RewardedVideoEventListener? = null
    private var appEventHandler: Handler? = null

    private var isExpectingAppEvent = false
    private var didNotifiedBidWin = false

    override fun onEvent(adEvent: AdEvent) {
        when (adEvent) {
            AdEvent.APP_EVENT_RECEIVED -> handleAppEvent()
            AdEvent.LOADED -> primaryAdReceived()
            AdEvent.DISPLAYED -> listener?.onAdDisplayed()
            AdEvent.CLOSED -> listener?.onAdClosed()
            AdEvent.FAILED -> notifyErrorListener(adEvent.errorCode)
            AdEvent.REWARD_EARNED -> listener?.onUserEarnedReward()
            else -> {}
        }
    }

    private fun handleAppEvent() {
        if (!isExpectingAppEvent) {
            LogUtil.debug(
                TAG,
                "appEventDetected: Skipping event handling. App event is not expected"
            )
            return
        }

        cancelTimer()
        isExpectingAppEvent = false
        didNotifiedBidWin = true
        listener?.onPrebidSdkWin()
    }

    private fun cancelTimer() {
        appEventHandler?.removeCallbacksAndMessages(null)
        appEventHandler = null
    }

    private fun primaryAdReceived() {
        if (isExpectingAppEvent) {
            if (appEventHandler != null) {
                LogUtil.debug(
                    TAG,
                    "primaryAdReceived: AppEventTimer is not null. Skipping timer scheduling."
                )
                return
            }

            scheduleTimer()
        } else if (!didNotifiedBidWin) {
            listener?.onAdServerWin(getRewardItem())
        }
    }

    private fun scheduleTimer() {
        cancelTimer()

        appEventHandler = Handler(Looper.getMainLooper())
        appEventHandler?.postDelayed(
            { handleAppEventTimeout() },
            TIMEOUT_APP_EVENT_MS
        )
    }

    private fun getRewardItem(): RewardItem? {
        return rewardedAd?.getRewardItem()
    }

    private fun handleAppEventTimeout() {
        cancelTimer()
        isExpectingAppEvent = false
        listener?.onAdServerWin(getRewardItem())
    }

    private fun notifyErrorListener(errorCode: Int) {
        when (errorCode) {
            Constants.ERROR_CODE_INTERNAL_ERROR -> listener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK encountered an internal error."
                )
            )

            Constants.ERROR_CODE_INVALID_REQUEST -> listener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - invalid request error."
                )
            )

            Constants.ERROR_CODE_NETWORK_ERROR -> listener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - network error."
                )
            )

            Constants.ERROR_CODE_NO_FILL -> listener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - no fill."
                )
            )

            else -> listener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - failed with errorCode: $errorCode"
                )
            )
        }
    }

    override fun setRewardedEventListener(listener: RewardedVideoEventListener) {
        this.listener = listener
    }

    override fun requestAdWithBid(bid: Bid?) {
        isExpectingAppEvent = false
        didNotifiedBidWin = false

        initPublisherRewardedAd()

        if (bid != null && bid.getPrice() > 0) {
            isExpectingAppEvent = true
        }

        if (rewardedAd == null) {
            notifyErrorListener(Constants.ERROR_CODE_INTERNAL_ERROR)
            return
        }

        rewardedAd?.loadAd(bid)
    }

    private fun initPublisherRewardedAd() {
        rewardedAd = RewardedAdWrapper(adUnitId, this)
    }

    override fun show() {
        if (rewardedAd?.isLoaded() == true) {
            activityWeakReference.get()?.let {
                rewardedAd?.show(it)
            }
        } else {
            listener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "GAM SDK - failed to display ad."
                )
            )
        }
    }

    override fun trackImpression() {}

    override fun destroy() {
        cancelTimer()
    }

    override fun getReward(): Reward? {
        val rewardItem = getRewardItem() ?: return null

        return Reward(rewardItem.type, rewardItem.amount, null)
    }
}