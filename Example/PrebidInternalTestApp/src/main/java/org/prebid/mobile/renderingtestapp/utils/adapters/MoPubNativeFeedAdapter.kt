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
import com.mopub.mediation.MoPubNativeMediationUtils
import com.mopub.nativeads.*
import org.prebid.mobile.*
import org.prebid.mobile.rendering.bidding.display.MediationNativeAdUnit
import org.prebid.mobile.renderingtestapp.R

class MoPubNativeFeedAdapter(
    context: Context,
    private val configId: String,
    private val adUnitId: String
) : BaseFeedAdapter(context) {

    private var mopubNative: MoPubNative? = null
    private var mopubNativeAdUnit: MediationNativeAdUnit? = null

    override fun destroy() {
        mopubNative?.destroy()
        mopubNativeAdUnit?.destroy()
    }

    override fun initAndLoadAdView(parent: ViewGroup?, container: FrameLayout): View? {
        val context = container.context
        val adapterHelper = AdapterHelper(context, 0, 3)
        mopubNative = MoPubNative(context, adUnitId, object : MoPubNative.MoPubNativeNetworkListener {
            override fun onNativeLoad(nativeAd: NativeAd?) {
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
        mopubNative?.registerAdRenderer(PrebidNativeAdRenderer(viewBinder, null))
        mopubNative?.registerAdRenderer(MoPubStaticNativeAdRenderer(viewBinder))

        val keywords = HashMap<String, String>()
        val mediationUtils = MoPubNativeMediationUtils(keywords, mopubNative)

        mopubNativeAdUnit = MediationNativeAdUnit(
            configId,
            mediationUtils
        )
        configureNativeAdUnit()
        MoPub.initializeSdk(context, SdkConfiguration.Builder(adUnitId).build()) {
            mopubNativeAdUnit?.fetchDemand {
                val requestParameters = RequestParameters.Builder()
                    .keywords(convertMapToMoPubKeywords(keywords))
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


    private fun configureNativeAdUnit() {
        mopubNativeAdUnit?.apply {
            setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
            setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
            setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)

            val methods = arrayListOf(
                NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE,
                NativeEventTracker.EVENT_TRACKING_METHOD.JS
            )
            val eventTracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            addEventTracker(eventTracker)

            val assetTitle = NativeTitleAsset()
            assetTitle.setLength(90)
            assetTitle.isRequired = true
            addAsset(assetTitle)

            val assetIcon = NativeImageAsset()
            assetIcon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
            assetIcon.wMin = 20
            assetIcon.hMin = 20
            assetIcon.isRequired = true
            addAsset(assetIcon)

            val image = NativeImageAsset()
            image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
            image.hMin = 200
            image.wMin = 200
            image.isRequired = true
            addAsset(image)

            val data = NativeDataAsset()
            data.len = 90
            data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
            data.isRequired = true
            addAsset(data)

            val body = NativeDataAsset()
            body.isRequired = true
            body.dataType = NativeDataAsset.DATA_TYPE.DESC
            addAsset(body)

            val cta = NativeDataAsset()
            cta.isRequired = true
            cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
            addAsset(cta)
        }
    }

}