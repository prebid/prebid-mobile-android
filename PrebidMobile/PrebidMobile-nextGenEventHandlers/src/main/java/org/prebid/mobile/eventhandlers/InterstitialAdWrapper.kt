package org.prebid.mobile.eventhandlers

import android.app.Activity
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.FullScreenContentError
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAdEventCallback
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.LogUtil
import org.prebid.mobile.eventhandlers.global.Constants
import org.prebid.mobile.eventhandlers.utils.Utils
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import java.lang.ref.WeakReference

/**
 * Internal wrapper of PublisherInterstitialAd from Next-Gen SDK.
 */
internal class InterstitialAdWrapper(
    activity: Activity,
    private val adUnit: String,
    private val listener: NextAdEventListener,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : AdLoadCallback<InterstitialAd>, InterstitialAdEventCallback {

    companion object {
        private val TAG: String = InterstitialAdWrapper::class.java.getSimpleName()
    }

    private var interstitialAd: InterstitialAd? = null
    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)

    override fun onAdLoaded(ad: InterstitialAd) {
        super.onAdLoaded(ad)
        interstitialAd = ad
        interstitialAd?.adEventCallback = this
        CoroutineScope(mainDispatcher).launch {
            listener.onEvent(AdEvent.Loaded())
        }
    }

    override fun onAdFailedToLoad(adError: LoadAdError) {
        super.onAdFailedToLoad(adError)
        interstitialAd = null
        notifyErrorListener(adError.code.value)
    }

    private fun notifyErrorListener(code: Int) {
        val adEvent = AdEvent.Failed(code)

        CoroutineScope(mainDispatcher).launch {
            listener.onEvent(adEvent)
        }
    }

    override fun onAppEvent(name: String, data: String?) {
        super.onAppEvent(name, data)
        if (Constants.APP_EVENT == name) {
            CoroutineScope(mainDispatcher).launch {
                listener.onEvent(AdEvent.AppEvent())
            }
        }
    }

    override fun onAdClicked() {
        super.onAdClicked()
        CoroutineScope(mainDispatcher).launch {
            listener.onEvent(AdEvent.Clicked())
        }
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        CoroutineScope(mainDispatcher).launch {
            listener.onEvent(AdEvent.Closed())
        }
    }

    override fun onAdFailedToShowFullScreenContent(fullScreenContentError: FullScreenContentError) {
        super.onAdFailedToShowFullScreenContent(fullScreenContentError)
        interstitialAd = null
        notifyErrorListener(fullScreenContentError.code.value)
    }

    override fun onAdShowedFullScreenContent() {
        super.onAdShowedFullScreenContent()
        CoroutineScope(mainDispatcher).launch {
            listener.onEvent(AdEvent.Displayed())
        }
    }

    fun loadAd(bid: Bid?) {
        interstitialAd = null
        try {
            val adRequestBuilder = AdRequest.Builder(adUnit)
            bid?.let {
                val targetingMap = HashMap(it.prebid.targeting)
                Utils.handleCustomTargetingUpdate(adRequestBuilder, targetingMap)
            }

            InterstitialAd.load(adRequestBuilder.build(), this)
        } catch (e: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(e))
        }
    }

    fun show() {
        val activity = activityWeakReference.get()

        if (activity == null) {
            LogUtil.error(TAG, "show: Failed. Activity is null.")
            return
        }
        if (interstitialAd == null) {
            LogUtil.error(
                TAG, "show: Failure. Interstitial ad is null."
            )
            return
        }

        try {
            interstitialAd?.show(activity)
        } catch (e: Throwable) {
            LogUtil.error(TAG, Log.getStackTraceString(e))
        }
    }

    fun isLoaded() = interstitialAd != null
}