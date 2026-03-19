package org.prebid.mobile.eventhandlers

import android.app.Activity
import android.os.Handler
import android.os.Looper
import org.prebid.mobile.LogUtil
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialEventHandler
import org.prebid.mobile.rendering.bidding.listeners.InterstitialEventListener
import java.lang.ref.WeakReference

/**
 * Interstitial event handler for communication between Prebid rendering API and the Next-Gen SDK.
 */
class NextInterstitialEventHandler(activity: Activity, private val adUnitId: String) :
    NextAdEventListener, InterstitialEventHandler {

    companion object {
        private val TAG: String = NextInterstitialEventHandler::class.java.getSimpleName()
        private const val TIMEOUT_APP_EVENT_MS: Long = 600
    }

    private var requestInterstitial: InterstitialAdWrapper? = null
    private val activityWeakReference = WeakReference(activity)

    private var eventListener: InterstitialEventListener? = null
    private var appEventHandler: Handler? = null

    private var isExpectingAppEvent = false
    private var didNotifiedBidWin = false

    override fun onEvent(adEvent: AdEvent) {
        when (adEvent) {
            is AdEvent.AppEvent -> handleAppEvent()
            is AdEvent.Loaded -> primaryAdReceived()
            is AdEvent.Closed -> eventListener?.onAdClosed()
            is AdEvent.Displayed -> eventListener?.onAdDisplayed()
            is AdEvent.Failed -> handleAdFailure(adEvent.errorCode)
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
        eventListener?.onPrebidSdkWin()
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
            eventListener?.onAdServerWin()
        }
    }

    private fun scheduleTimer() {
        cancelTimer()

        appEventHandler = Handler(Looper.getMainLooper())
        appEventHandler?.postDelayed(
            { this.handleAppEventTimeout() },
            TIMEOUT_APP_EVENT_MS
        )
    }

    private fun handleAppEventTimeout() {
        cancelTimer()
        isExpectingAppEvent = false
        eventListener?.onAdServerWin()
    }

    private fun handleAdFailure(errorCode: Int) {
        when (errorCode) {
            Constants.ERROR_CODE_INTERNAL_ERROR -> eventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK encountered an internal error."
                )
            )

            Constants.ERROR_CODE_INVALID_REQUEST -> eventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - invalid request error."
                )
            )

            Constants.ERROR_CODE_NETWORK_ERROR -> eventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - network error."
                )
            )

            Constants.ERROR_CODE_NO_FILL -> eventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - no fill."
                )
            )

            else -> eventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - failed with errorCode: $errorCode"
                )
            )
        }
    }

    override fun setInterstitialEventListener(interstitialEventListener: InterstitialEventListener?) {
        eventListener = interstitialEventListener
    }

    override fun requestAdWithBid(bid: Bid?) {
        isExpectingAppEvent = false
        didNotifiedBidWin = false

        initPublisherInterstitialAd()

        if (bid != null && bid.price > 0) {
            isExpectingAppEvent = true
        }

        if (requestInterstitial == null) {
            handleAdFailure(Constants.ERROR_CODE_INTERNAL_ERROR)
            return
        }

        requestInterstitial?.loadAd(bid)
    }

    private fun initPublisherInterstitialAd() {
        if (requestInterstitial != null) {
            requestInterstitial = null
        }

        val activity = activityWeakReference.get()
        if (activity == null) {
            LogUtil.error(TAG, "Activity is null")
            return
        }

        requestInterstitial = InterstitialAdWrapper(
            activity,
            adUnitId,
            this
        )
    }

    override fun show() {
        if (requestInterstitial?.isLoaded() == true) {
            requestInterstitial?.show()
        } else {
            eventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next-Gen SDK - failed to display ad."
                )
            )
        }
    }

    override fun trackImpression() {}

    override fun destroy() {
        cancelTimer()
    }
}