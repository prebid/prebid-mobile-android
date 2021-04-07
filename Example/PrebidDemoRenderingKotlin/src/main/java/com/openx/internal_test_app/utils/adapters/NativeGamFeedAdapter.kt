package com.openx.internal_test_app.utils.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.formats.NativeCustomTemplateAd
import com.openx.apollo.bidding.data.FetchDemandResult
import com.openx.apollo.bidding.data.ntv.NativeAd
import com.openx.apollo.bidding.display.NativeAdUnit
import com.openx.apollo.bidding.listeners.NativeAdListener
import com.openx.apollo.bidding.listeners.OnNativeFetchCompleteListener
import com.openx.apollo.eventhandlers.utils.GamUtils
import com.openx.apollo.models.ntv.NativeEventTracker
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetData
import com.openx.internal_test_app.R
import com.openx.internal_test_app.utils.loadImage
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_ad.view.*
import kotlinx.android.synthetic.main.lyt_native_gam_events.*

class NativeGamFeedAdapter(context: Context,
                           private val nativeAdUnit: NativeAdUnit,
                           private val gamAdLoader: AdLoader) : BaseFeedAdapter(context) {
    private val TAG = NativeFeedAdapter::class.java.simpleName

    private var nativeAdLayout: ConstraintLayout? = null
    private var nativeAd: NativeAd? = null

    private val fetchCompleteListener = OnNativeFetchCompleteListener {
        val builder = PublisherAdRequest.Builder()
        val publisherAdRequest = builder.build()

        nativeAdUnit.fetchDemand { result ->
            val fetchDemandResult = result.fetchDemandResult

            if (fetchDemandResult != FetchDemandResult.SUCCESS) {
                gamAdLoader.loadAd(publisherAdRequest)
                return@fetchDemand
            }

            GamUtils.prepare(builder, result)
            gamAdLoader.loadAd(publisherAdRequest)
        }
    }

    private val nativeAdListener = object : NativeAdListener {
        override fun onAdClicked(nativeAd: NativeAd?) {
            Log.d(TAG, "onAdClicked() called with: nativeAd = $nativeAd")
        }

        override fun onAdEvent(nativeAd: NativeAd?, eventType: NativeEventTracker.EventType?) {
            Log.d(TAG, "onAdEvent() called with: nativeAd = $nativeAd, eventType = $eventType")
        }
    }

    override fun destroy() {
        nativeAdUnit.destroy()
    }

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        if (nativeAdLayout == null) {
            nativeAdLayout = layoutInflater.inflate(R.layout.lyt_native_ad, parent, false) as ConstraintLayout
        }
        nativeAdUnit.fetchDemand(fetchCompleteListener)
        return nativeAdLayout
    }

    fun handleCustomTemplateAd(customTemplate: NativeCustomTemplateAd?) {
        customTemplate ?: return

        if (GamUtils.didApolloWin(customTemplate)) {
            GamUtils.findNativeAd(customTemplate) {
                inflateViewContent(it)
            }
        }
        else {
            Log.d(TAG, "handleCustomTemplateAd: apollo lost")
        }
    }

    private fun inflateViewContent(nativeAd: NativeAd?) {
        if (nativeAd == null) {
            return
        }

        this.nativeAd?.destroy()

        this.nativeAd = nativeAd
        nativeAd.setNativeAdListener(nativeAdListener)

        nativeAd.registerView(nativeAdLayout as View, nativeAdLayout?.btnNativeAction)

        nativeAdLayout?.tvNativeTitle?.text = nativeAd.title
        nativeAdLayout?.tvNativeBody?.text = nativeAd.text
        nativeAdLayout?.tvNativeBrand?.text = nativeAd.getNativeAdDataList(NativeAssetData.DataType.SPONSORED).firstOrNull()?.value
        nativeAdLayout?.btnNativeAction?.text = nativeAd.callToAction

        loadImage(nativeAdLayout!!.ivNativeMain, nativeAd.imageUrl)
        loadImage(nativeAdLayout!!.ivNativeIcon, nativeAd.iconUrl)
    }
}