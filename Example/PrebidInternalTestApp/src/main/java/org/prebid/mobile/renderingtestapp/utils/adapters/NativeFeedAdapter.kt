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
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import kotlinx.android.synthetic.main.lyt_native_ad.view.*
import org.prebid.mobile.LogUtil
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.PrebidNativeAdEventListener
import org.prebid.mobile.api.data.FetchDemandResult
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.utils.loadImage

class NativeFeedAdapter(
    context: Context,
    private val nativeAdUnit: MediationNativeAdUnit,
    private val extras: Bundle
) : BaseFeedAdapter(context) {

    private val TAG = NativeFeedAdapter::class.java.simpleName

    var nativeAdLayout: ConstraintLayout? = null
    var nativeAd: PrebidNativeAd? = null

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        if (nativeAdLayout == null) {
            nativeAdLayout = layoutInflater.inflate(R.layout.lyt_native_ad, parent, false) as ConstraintLayout
        }
        nativeAdUnit.fetchDemand {
            if (it != FetchDemandResult.SUCCESS) {
                return@fetchDemand
            }

            val nativeAd = NativeAdProvider.getNativeAd(extras)
            if (nativeAd == null) {
                LogUtil.error(TAG, "Native ad is null")
                return@fetchDemand
            }

            inflateViewContent(nativeAd)
        }
        return nativeAdLayout
    }

    override fun destroy() {}

    private fun inflateViewContent(nativeAd: PrebidNativeAd) {
        this.nativeAd = nativeAd

        nativeAdLayout?.let {
            nativeAd.registerViewList(
                it,
                listOf(
                    it.tvNativeTitle,
                    it.tvNativeBody,
                    it.tvNativeBrand,
                    it.btnNativeAction,
                    it.ivNativeIcon,
                    it.ivNativeMain
                ),
                createListener()
            )

            it.tvNativeTitle?.text = nativeAd.title
            it.tvNativeBody?.text = nativeAd.description
            it.tvNativeBrand?.text = nativeAd.sponsoredBy
            it.btnNativeAction?.text = nativeAd.callToAction

            loadImage(it.ivNativeMain, nativeAd.imageUrl)
            loadImage(it.ivNativeIcon, nativeAd.iconUrl)
        }
    }

    private fun createListener(): PrebidNativeAdEventListener {
        return object : PrebidNativeAdEventListener {
            override fun onAdClicked() {}
            override fun onAdImpression() {}
            override fun onAdExpired() {}
        }
    }

}