package org.prebid.mobile.eventhandlers.nextgen

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
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.prebid.mobile.LogUtil
import org.prebid.mobile.eventhandlers.nextgen.global.Constants
import org.prebid.mobile.eventhandlers.nextgen.utils.Utils
import org.prebid.mobile.rendering.bidding.data.bid.Bid
import java.lang.ref.WeakReference

/**
 * Internal wrapper of PublisherInterstitialAd from Next-Gen SDK.
 */
internal class InterstitialAdWrapper(
    activity: Activity,
    private val adUnit: String,
    private val listener: NextGenAdEventListener,
    private val mainDispatcher: CoroutineDispatcher = Dispatchers.Main,
) : AdLoadCallback<InterstitialAd>, InterstitialAdEventCallback {

    companion object {
        private val TAG: String = InterstitialAdWrapper::class.java.getSimpleName()
    }

    private val scope = CoroutineScope(mainDispatcher + SupervisorJob())
    private var interstitialAd: InterstitialAd? = null
    private val activityWeakReference: WeakReference<Activity> = WeakReference(activity)

    override fun onAdLoaded(ad: InterstitialAd) {
        super.onAdLoaded(ad)
        interstitialAd = ad
        interstitialAd?.adEventCallback = this
        scope.launch {
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

        scope.launch {
            listener.onEvent(adEvent)
        }
    }

    override fun onAppEvent(name: String, data: String?) {
        super.onAppEvent(name, data)
        if (Constants.APP_EVENT == name) {
            scope.launch {
                listener.onEvent(AdEvent.AppEvent())
            }
        }
    }

    override fun onAdClicked() {
        super.onAdClicked()
        scope.launch {
            listener.onEvent(AdEvent.Clicked())
        }
    }

    override fun onAdDismissedFullScreenContent() {
        super.onAdDismissedFullScreenContent()
        scope.launch {
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
        scope.launch {
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

    fun destroy() {
        scope.cancel()
    }

    fun isLoaded() = interstitialAd != null
}