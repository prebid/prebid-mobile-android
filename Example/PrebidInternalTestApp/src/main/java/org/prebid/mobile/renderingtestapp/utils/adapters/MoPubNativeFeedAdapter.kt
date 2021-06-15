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
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.nativeads.*
import org.prebid.mobile.rendering.bidding.display.MoPubNativeAdUnit
import org.prebid.mobile.rendering.models.ntv.NativeAdConfiguration
import org.prebid.mobile.renderingtestapp.R

class MoPubNativeFeedAdapter(context: Context,
                             private val configId: String,
                             private val adUnitId: String,
                             private val nativeAdConfiguration: NativeAdConfiguration?) : BaseFeedAdapter(context) {
    private var mopubNative: MoPubNative? = null
    private var mopubNativeAdUnit: MoPubNativeAdUnit? = null

    override fun destroy() {
        mopubNative?.destroy()
        mopubNativeAdUnit?.destroy()
    }

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        val context = container.context
        mopubNative = MoPubNative(context, adUnitId, object : MoPubNative.MoPubNativeNetworkListener {
            override fun onNativeLoad(nativeAd: NativeAd?) {
                val adapterHelper = AdapterHelper(context, 0, 3)
                val view = adapterHelper.getAdView(null, parent, nativeAd)
                container.addView(view)
            }

            override fun onNativeFail(errorCode: NativeErrorCode?) {}
        })
        val viewBinder = ViewBinder.Builder(R.layout.lyt_native_ad)
                .titleId(R.id.tvNativeTitle)
                .textId(R.id.tvNativeBody)
                .sponsoredTextId(R.id.tvNativeBrand)
                .mainImageId(R.id.ivNativeMain)
                .iconImageId(R.id.ivNativeIcon)
                .callToActionId(R.id.btnNativeAction)
                .build()
        mopubNative?.registerAdRenderer(PrebidNativeAdRenderer(viewBinder))
        mopubNative?.registerAdRenderer(MoPubStaticNativeAdRenderer(viewBinder))
        mopubNativeAdUnit = MoPubNativeAdUnit(context, configId, nativeAdConfiguration)
        MoPub.initializeSdk(context, SdkConfiguration.Builder(adUnitId).build()) {
            val keywordsContainer = HashMap<String, String>()
            mopubNativeAdUnit?.fetchDemand(keywordsContainer, mopubNative!!) {
                val requestParameters = RequestParameters.Builder()
                        .keywords(convertMapToMoPubKeywords(keywordsContainer))
                        .build()
                mopubNative?.makeRequest(requestParameters)
            }
        }

        return null
    }

    private fun convertMapToMoPubKeywords(keywordMap: Map<String, String>): String? {
        val result = StringBuilder()
        for (key in keywordMap.keys) {
            result.append(key).append(":").append(keywordMap[key]).append(",")
        }
        if (result.isNotEmpty()) {
            result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }
}