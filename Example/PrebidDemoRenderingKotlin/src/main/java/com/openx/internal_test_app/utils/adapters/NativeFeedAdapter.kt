package com.openx.internal_test_app.utils.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.openx.apollo.bidding.data.FetchDemandResult
import com.openx.apollo.bidding.data.ntv.NativeAd
import com.openx.apollo.bidding.display.NativeAdUnit
import com.openx.apollo.bidding.listeners.NativeAdListener
import com.openx.apollo.bidding.listeners.OnNativeFetchCompleteListener
import com.openx.apollo.models.ntv.NativeEventTracker
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetData
import com.openx.apollo.utils.ntv.NativeUtils
import com.openx.internal_test_app.R
import com.openx.internal_test_app.utils.loadImage
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_ad.view.*

class NativeFeedAdapter(context: Context, private val nativeAdUnit: NativeAdUnit) : BaseFeedAdapter(context) {
    private val TAG = NativeFeedAdapter::class.java.simpleName

    var nativeAdLayout: ConstraintLayout? = null
    var nativeAd: NativeAd? = null

    private val fetchCompleteListener = OnNativeFetchCompleteListener {
        if (it.fetchDemandResult != FetchDemandResult.SUCCESS) {
            return@OnNativeFetchCompleteListener
        }

        NativeUtils.findNativeAd(it) { nativeAd ->
            inflateViewContent(nativeAd)
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
        Log.d(TAG, "Destroying adapter")
        nativeAd?.destroy()
        nativeAdUnit.destroy()
    }

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        if (nativeAdLayout == null) {
            nativeAdLayout = layoutInflater.inflate(R.layout.lyt_native_ad, parent, false) as ConstraintLayout
        }
        nativeAdUnit.fetchDemand(fetchCompleteListener)
        return nativeAdLayout
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