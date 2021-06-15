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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.fragment_native.*
import kotlinx.android.synthetic.main.fragment_native.view.*
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_ad_events.*
import kotlinx.android.synthetic.main.lyt_native_in_app_events.*
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd
import org.prebid.mobile.rendering.bidding.display.NativeAdUnit
import org.prebid.mobile.rendering.bidding.listeners.NativeAdListener
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData
import org.prebid.mobile.rendering.utils.ntv.NativeUtils
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.SourcePicker
import org.prebid.mobile.renderingtestapp.utils.loadImage

open class PpmNativeFragment : AdFragment(), NativeAdListener {
    private val TAG = PpmNativeFragment::class.java.simpleName

    override val layoutRes: Int = R.layout.fragment_native
    protected var nativeAdUnit: NativeAdUnit? = null
    protected var nativeAd: NativeAd? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(true)
        }

        if (layoutRes == R.layout.fragment_native) {
            layoutInflater.inflate(getEventButtonViewId(), contentFragmentNative, true)
        }
    }

    override fun initAd(): Any? {
        nativeAdUnit = NativeAdUnit(context, configId, getNativeAdConfig()!!)
        return nativeAdUnit
    }

    override fun loadAd() {
        nativeAdUnit?.fetchDemand {

            if (it.fetchDemandResult != FetchDemandResult.SUCCESS) {
                btnFetchDemandResultFailure?.isEnabled = true
                return@fetchDemand
            }

            btnFetchDemandResultSuccess?.isEnabled = true

            NativeUtils.findNativeAd(it) { nativeAd ->
                if (nativeAd == null) {
                    btnGetNativeAdResultFailure?.isEnabled = true
                    return@findNativeAd
                }
                btnGetNativeAdResultSuccess?.isEnabled = true

                inflateViewContent(nativeAd)
            }
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        nativeAdUnit?.destroy()
        nativeAd?.destroy()
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(false)
        }
    }

    override fun onAdClicked(nativeAd: NativeAd?) {
        Log.d(TAG, "onAdClicked() called with: nativeAd = $nativeAd")
        btnAdClicked?.isEnabled = true
    }

    override fun onAdEvent(nativeAd: NativeAd?, eventType: NativeEventTracker.EventType?) {
        Log.d(TAG, "onAdEvent() called with: nativeAd = $nativeAd, eventType = $eventType")
        eventType ?: return

        when (eventType) {
            NativeEventTracker.EventType.IMPRESSION -> btnAdEventImpression?.isEnabled = true
            NativeEventTracker.EventType.VIEWABLE_MRC50 -> btnAdEventMrc50?.isEnabled = true
            NativeEventTracker.EventType.VIEWABLE_MRC100 -> btnAdEventMrc100?.isEnabled = true
            NativeEventTracker.EventType.VIEWABLE_VIDEO50 -> btnAdEventVideo50?.isEnabled = true
            else -> Log.d(TAG, "onAdEvent: event ignored $eventType")
        }
    }

    protected open fun getEventButtonViewId(): Int = R.layout.lyt_native_in_app_events

    protected open fun inflateViewContent(nativeAd: NativeAd?) {
        nativeAd ?: return

        this.nativeAd = nativeAd

        nativeAd.setNativeAdListener(this)

        nativeAd.registerView(adContainer, btnNativeAction)

        tvNativeTitle?.text = nativeAd.title
        tvNativeBody?.text = nativeAd.text
        tvNativeBrand?.text = nativeAd.getNativeAdDataList(NativeAssetData.DataType.SPONSORED).firstOrNull()?.value
        btnNativeAction?.text = nativeAd.callToAction

        loadImage(ivNativeMain, nativeAd.imageUrl)
        loadImage(ivNativeIcon, nativeAd.iconUrl)
    }
}