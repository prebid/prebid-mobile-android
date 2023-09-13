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
package org.prebid.mobile.renderingtestapp.utils

import android.R
import android.app.AlertDialog
import android.content.Context
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.view.ViewTreeObserver
import android.webkit.WebView
import android.widget.FrameLayout
import android.widget.ImageButton
import org.json.JSONObject
import org.prebid.mobile.LogUtil
import org.prebid.mobile.api.data.AdFormat
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface
import org.prebid.mobile.rendering.bidding.listeners.DisplayVideoListener
import org.prebid.mobile.api.rendering.pluginrenderer.PluginEventListener
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRenderer
import org.prebid.mobile.configuration.AdUnitConfiguration
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener

class SampleCustomRenderer : PrebidMobilePluginRenderer {

    override fun getName(): String = SAMPLE_PLUGIN_RENDERER_NAME

    private val pluginEventListenerMap = mutableMapOf<String, SampleCustomRendererEventListener>()

    override fun getVersion(): String = "1.0.0"

    override fun getData(): JSONObject? = null

    override fun registerEventListener(
        pluginEventListener: PluginEventListener,
        listenerKey: String
    ) {
        (pluginEventListener as? SampleCustomRendererEventListener)?.let {
            pluginEventListenerMap[listenerKey] = it
        }
    }

    override fun unregisterEventListener(listenerKey: String) {
        pluginEventListenerMap.remove(listenerKey)
    }

    override fun createBannerAdView(
        context: Context,
        displayViewListener: DisplayViewListener,
        displayVideoListener: DisplayVideoListener?,
        adUnitConfiguration: AdUnitConfiguration,
        bidResponse: BidResponse
    ): View {
        if (bidResponse.winningBid?.adm.isNullOrBlank()) {
            displayViewListener.onAdFailed(AdException(AdException.SERVER_ERROR, "adm is null"))
            // return empty view
            return View(context)
        }

        val bannerView = getBannerUi(
            adContent = bidResponse.winningBid!!.adm,
            context = context,
            onClick = { displayViewListener.onAdClicked() },
            onClosed = { displayViewListener.onAdClosed() }
        )
        val visibilityChecker = ViewVisibilityObserver(bannerView) {
            // Dispatch additional event listener based on criteria from ViewVisibilityObserver
            pluginEventListenerMap[adUnitConfiguration.fingerprint]?.onImpression()
        }

        bannerView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // View has been inflated
                displayViewListener.onAdDisplayed()
                visibilityChecker.startObserving()
                bannerView.viewTreeObserver.removeOnGlobalLayoutListener(this)
            }
        })

        return bannerView
    }

    override fun createInterstitialController(
        context: Context,
        interstitialControllerListener: InterstitialControllerListener,
        adUnitConfiguration: AdUnitConfiguration,
        bidResponse: BidResponse
    ): PrebidMobileInterstitialControllerInterface {
        val alertDialog = AlertDialog.Builder(context)
            .setPositiveButton("Click") { dialog: DialogInterface, _: Int -> interstitialControllerListener.onInterstitialClicked() }
            .setNegativeButton("Close") { dialog: DialogInterface, _: Int -> dialog.dismiss() }
            .setOnDismissListener { interstitialControllerListener.onInterstitialClosed() }
            .setTitle("Interstitial")
            .setCancelable(false)
            .create()

        return object : PrebidMobileInterstitialControllerInterface {
            override fun loadAd(adUnitConfiguration: AdUnitConfiguration, bidResponse: BidResponse) {
                if (bidResponse.winningBid?.adm.isNullOrBlank()) {
                    interstitialControllerListener.onInterstitialFailedToLoad(AdException(AdException.SERVER_ERROR, "adm is null"))
                } else {
                    val webView = WebView(context).apply { loadData(bidResponse.winningBid?.adm!!, "text/html", "UTF-8") }
                    alertDialog.setView(webView)
                    interstitialControllerListener.onInterstitialReadyForDisplay()
                }
            }

            override fun show() {
                alertDialog.show()
                interstitialControllerListener.onInterstitialDisplayed()
                // Dispatch additional event listener
                pluginEventListenerMap[adUnitConfiguration.fingerprint]?.onImpression()
            }

            override fun destroy() {}
        }
    }

    override fun isSupportRenderingFor(adUnitConfiguration: AdUnitConfiguration): Boolean {
        return when {
            adUnitConfiguration.isAdType(AdFormat.BANNER) -> true
            adUnitConfiguration.isAdType(AdFormat.INTERSTITIAL) -> true
            else -> false
        }
    }

    private fun getBannerUi(
        adContent: String,
        context: Context,
        onClick: () -> Unit,
        onClosed: () -> Unit
    ): View {
        val webView = WebView(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            loadData(adContent, "text/html", "UTF-8")
        }

        val frameLayout = FrameLayout(context).apply {
            layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
            )
            setBackgroundColor(Color.parseColor("#D3D3D3"))
            setOnClickListener { onClick() }
        }

        val closeButton = ImageButton(context).apply {
            setImageResource(R.drawable.ic_menu_close_clear_cancel)
            setBackgroundColor(Color.parseColor("#00FFFFFF"))
            this.layoutParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT,
                Gravity.TOP or Gravity.END
            ).also { it.setMargins(0, 20, 10, 0) }
            setOnClickListener {
                onClosed()
                frameLayout.visibility = View.GONE
            }
        }

        frameLayout.addView(webView)
        frameLayout.addView(closeButton)

        return frameLayout
    }

    private class ViewVisibilityObserver(val view: View, block: () -> Unit) {
        private val handler = Handler(Looper.getMainLooper())
        private var isViewVisible = false
        private val CHECK_INTERVAL = 1000 // 1 second
        private val MIN_VISIBILITY_STATE = 0.5f
        private var isObserving = false;

        private val runnable = object : Runnable {
            override fun run() {
                if (isObserving.not()) return
                val isVisibleNow = view.run {
                    val rect = Rect()
                    view.getLocalVisibleRect(rect)

                    val viewHeight = view.height
                    val visibleHeight = rect.bottom - rect.top
                    LogUtil.debug(SAMPLE_PLUGIN_RENDERER_NAME, "Visibility percentage: ${(visibleHeight.toFloat() / viewHeight) * 100}%")

                    visibleHeight >= viewHeight * MIN_VISIBILITY_STATE
                }
                if (isVisibleNow != isViewVisible) {
                    isViewVisible = isVisibleNow
                    if (isViewVisible) {
                        block()
                        stopObserving()
                    }
                }
                handler.postDelayed(this, CHECK_INTERVAL.toLong())
            }
        }

        fun startObserving() {
            isObserving = true
            handler.postDelayed(runnable, CHECK_INTERVAL.toLong())
        }

        fun stopObserving() {
            isObserving = false
            handler.removeCallbacks(runnable)
        }
    }

    companion object {
        const val SAMPLE_PLUGIN_RENDERER_NAME = "SampleCustomRenderer"
    }
}

