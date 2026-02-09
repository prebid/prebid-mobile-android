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

import android.content.Context
import android.util.Log
import android.view.View
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdEventCallback
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.AdSize
import org.prebid.mobile.LogUtil
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.eventhandlers.utils.Utils
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize as NextSize

/**
 * Internal wrapper of PublisherAdView from Next-Gen SDK.
 * To achieve safe integration between various Next-Gen SDK versions we have to wrap all PublisherAdView method execution in try / catch.
 * This class instance should be created via newInstance method, which will catch any potential exception on PublisherAdView / PublisherAdViewWrapper instance creation
 */
internal class AdViewWrapper private constructor(
    context: Context,
    private val nextAdUnit: String,
    private val listener: NextAdEventListener,
    adSizes: List<AdSize>,
) : AdLoadCallback<BannerAd>, BannerAdEventCallback {

    private val adView: AdView by lazy {
        AdView(context)
    }
    private var ad: BannerAd? = null
    private val nextSizes = adSizes.toNextAdSizes()

    companion object {
        private val TAG: String = AdViewWrapper::class.java.getSimpleName()

        fun newInstance(
            context: Context,
            adUnitId: String,
            eventListener: NextAdEventListener,
            adSizes: List<AdSize>,
        ): AdViewWrapper {
            return AdViewWrapper(
                context,
                adUnitId,
                eventListener,
                adSizes
            )
        }
    }

    override fun onAppEvent(name: String, data: String?) {
        if (Constants.APP_EVENT == name) {
            CoroutineScope(Dispatchers.Main).launch {
                listener.onEvent(AdEvent.AppEvent())
            }
        }
    }

    override fun onAdClicked() {
        super.onAdClicked()
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Clicked())
        }
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Closed())
        }
    }

    override fun onAdFailedToLoad(adError: LoadAdError) {
        super.onAdFailedToLoad(adError)
        CoroutineScope(Dispatchers.Main).launch {
            val adEvent = AdEvent.Failed(adError.code.value)

            listener.onEvent(adEvent)
        }
    }

    override fun onAdLoaded(ad: BannerAd) {
        super.onAdLoaded(ad)
        this.ad = ad
        this.ad?.adEventCallback = this
        CoroutineScope(Dispatchers.Main).launch {
            listener.onEvent(AdEvent.Loaded())
        }
    }

    fun loadAd(bid: Bid?) {
        try {
            val adRequestBuilder = BannerAdRequest.Builder(nextAdUnit, nextSizes)

            bid?.let {
                val targetingMap = HashMap(it.prebid.targeting)
                Utils.handleCustomTargetingUpdate(adRequestBuilder, targetingMap)
            }

            adView.loadAd(adRequestBuilder.build(), this)
        } catch (throwable: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable))
        }
    }

    fun recordManualImpression() {
        try {
            ad?.recordManualImpression()
        } catch (throwable: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable))
        }
    }

    fun destroy() {
        try {
            adView.destroy()
        } catch (throwable: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(throwable))
        }
    }

    val view: View
        get() = adView

    private fun List<AdSize>.toNextAdSizes(): List<NextSize> {
        val list = mutableListOf<NextSize>()
        for (adSize in this) {
            list.add(NextSize(adSize.width, adSize.height))
        }

        return list
    }

}
