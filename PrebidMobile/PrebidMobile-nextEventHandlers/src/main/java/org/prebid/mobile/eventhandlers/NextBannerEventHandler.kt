/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.prebid.mobile.eventhandlers

import android.annotation.SuppressLint
import android.content.Context
import android.os.Handler
import android.os.Looper
import org.prebid.mobile.AdSize
import org.prebid.mobile.LogUtil
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import org.prebid.mobile.rendering.bidding.interfaces.BannerEventHandler
import org.prebid.mobile.rendering.bidding.listeners.BannerEventListener

/**
 * Banner event handler for communication between Prebid rendering API and the Next-Gen SDK.
 * It implements the Prebid Rendering SDK EventHandler interface. Prebid Rendering SDK notifies (using EventHandler interface)
 * to make a request to Next-Gen SDK and pass the targeting parameters. This class also creates the Next-Gen's
 * AdViews, initializes them and listens for the callback methods. And pass the Next-Gen ad event to
 * Prebid Rendering SDK via BannerEventListener.
 *
 *  @param context     activity or application context.
 *  @param adUnitId    AdUnitId.
 *  @param adSizes     ad sizes for banner.
 */
class NextBannerEventHandler(
    context: Context,
    private val adUnitId: String,
    vararg adSizes: AdSize,
) : BannerEventHandler, NextAdEventListener {
    private val applicationContext: Context = context.applicationContext
    private val adSizes: List<AdSize> = listOf(*adSizes)

    private var requestBanner: AdViewWrapper? = null
    private var proxyBanner: AdViewWrapper? = null
    private var embeddedBanner: AdViewWrapper? = null
    private var recycledBanner: AdViewWrapper? = null

    private var bannerEventListener: BannerEventListener? = null
    private var appEventHandler: Handler? = null

    private var isExpectingAppEvent = false

    override fun onEvent(adEvent: AdEvent) {
        when (adEvent) {
            is AdEvent.AppEvent -> handleAppEvent()
            is AdEvent.Closed -> bannerEventListener?.onAdClosed()
            is AdEvent.Failed -> handleAdFailure(adEvent.errorCode)
            is AdEvent.Clicked -> bannerEventListener?.onAdClicked()
            is AdEvent.Loaded -> primaryAdReceived()
            else -> {}
        }
    }

    override fun getAdSizeArray(): Array<AdSize?> {
        if (adSizes.isEmpty()) {
            return arrayOfNulls(0)
        }

        return adSizes.toTypedArray()
    }

    override fun setBannerEventListener(
        bannerViewListener: BannerEventListener,
    ) {
        bannerEventListener = bannerViewListener
    }

    @SuppressLint("MissingPermission")
    override fun requestAdWithBid(bid: Bid?) {
        isExpectingAppEvent = false

        if (requestBanner != null) {
            LogUtil.error(
                TAG,
                "requestAdWithBid: Failed. Request to primaryAdServer is in progress."
            )
            return
        }

        if (recycledBanner != null) {
            requestBanner = recycledBanner
            recycledBanner = null
        } else {
            requestBanner = createPublisherAdView()
        }

        if (bid != null && bid.price > 0) {
            isExpectingAppEvent = true
        }

        if (requestBanner == null) {
            handleAdFailure(Constants.ERROR_CODE_INTERNAL_ERROR)
            return
        }

        requestBanner?.loadAd(bid)
    }

    override fun trackImpression() {
        proxyBanner?.recordManualImpression()
    }

    override fun destroy() {
        cancelTimer()
        destroyViews()
    }

    private fun createPublisherAdView(): AdViewWrapper {
        return AdViewWrapper.newInstance(applicationContext, adUnitId, this, adSizes)
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
        } else if (requestBanner != null) {
            val bannerView = requestBanner
            requestBanner = null
            recycleCurrentBanner()
            embeddedBanner = bannerView
            bannerEventListener?.onAdServerWin(bannerView?.view)
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
        val bannerView = requestBanner
        requestBanner = null
        isExpectingAppEvent = false
        recycleCurrentBanner()
        proxyBanner = bannerView
        bannerEventListener?.onPrebidSdkWin()
    }

    private fun scheduleTimer() {
        cancelTimer()

        appEventHandler = Handler(Looper.getMainLooper())
        appEventHandler?.postDelayed(
            { this.handleAppEventTimeout() },
            TIMEOUT_APP_EVENT_MS
        )
    }

    private fun cancelTimer() {
        appEventHandler?.removeCallbacksAndMessages(null)
        appEventHandler = null
    }

    private fun handleAppEventTimeout() {
        cancelTimer()
        val bannerView = requestBanner
        requestBanner = null
        recycleCurrentBanner()
        embeddedBanner = bannerView
        isExpectingAppEvent = false
        bannerEventListener?.onAdServerWin(bannerView?.view)
    }

    private fun handleAdFailure(errorCode: Int) {
        requestBanner = null
        recycleCurrentBanner()

        when (errorCode) {
            Constants.ERROR_CODE_INTERNAL_ERROR -> bannerEventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next Gen SDK encountered an internal error."
                )
            )

            Constants.ERROR_CODE_INVALID_REQUEST -> bannerEventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next Gen SDK - invalid request error."
                )
            )

            Constants.ERROR_CODE_NETWORK_ERROR -> bannerEventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next Gen SDK - network error."
                )
            )

            Constants.ERROR_CODE_NO_FILL -> bannerEventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next Gen SDK - no fill."
                )
            )

            else -> bannerEventListener?.onAdFailed(
                AdException(
                    AdException.THIRD_PARTY,
                    "Next Gen SDK - failed with errorCode: $errorCode"
                )
            )
        }
    }

    private fun recycleCurrentBanner() {
        if (embeddedBanner != null) {
            recycledBanner = embeddedBanner
            embeddedBanner = null
        } else if (proxyBanner != null) {
            recycledBanner = proxyBanner
            proxyBanner = null
        }
    }

    private fun destroyViews() {
        requestBanner?.destroy()
        proxyBanner?.destroy()
        embeddedBanner?.destroy()
        recycledBanner?.destroy()
    }

    companion object {
        private val TAG: String = NextBannerEventHandler::class.java.getSimpleName()

        private const val TIMEOUT_APP_EVENT_MS: Long = 600

    }
}
