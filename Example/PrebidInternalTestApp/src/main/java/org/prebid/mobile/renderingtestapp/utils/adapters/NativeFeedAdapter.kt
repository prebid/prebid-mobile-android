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

package org.prebid.mobile.renderingtestapp.utils.adapters

import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_ad.view.*
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd
import org.prebid.mobile.rendering.bidding.display.NativeAdUnit
import org.prebid.mobile.rendering.bidding.listeners.NativeAdListener
import org.prebid.mobile.rendering.bidding.listeners.OnNativeFetchCompleteListener
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData
import org.prebid.mobile.rendering.utils.ntv.NativeUtils
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.utils.loadImage

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