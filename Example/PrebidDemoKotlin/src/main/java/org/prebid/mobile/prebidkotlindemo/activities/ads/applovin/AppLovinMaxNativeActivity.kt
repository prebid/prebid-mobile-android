/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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
package org.prebid.mobile.prebidkotlindemo.activities.ads.applovin

import android.os.Bundle
import android.util.Log
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import org.prebid.mobile.*
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class AppLovinMaxNativeActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "f3bdfa9dd8da1c4d"
        const val CONFIG_ID = "imp-prebid-banner-native-styles"
    }

    private var nativeAdLoader: MaxNativeAdLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        nativeAdLoader = MaxNativeAdLoader(AD_UNIT_ID, this)
        nativeAdLoader?.setRevenueListener {}
        nativeAdLoader?.setNativeAdListener(object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAd: MaxAd?) {
                adWrapperView.removeAllViews()
                adWrapperView.addView(nativeAdView)
            }

            override fun onNativeAdLoadFailed(p0: String?, p1: MaxError?) {
                Log.e("AppLovinMaxNative", "Failed to load: $p0")
            }

            override fun onNativeAdClicked(p0: MaxAd?) {}
        })

        val nativeAdUnit = configureNativeAdUnit()
        nativeAdUnit.fetchDemand(nativeAdLoader ?: return) {
            nativeAdLoader?.loadAd(createNativeAdView())
        }
    }


    private fun configureNativeAdUnit(): NativeAdUnit {
        val nativeAdUnit = NativeAdUnit(CONFIG_ID)

        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        val methods: ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> = ArrayList()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            nativeAdUnit.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        nativeAdUnit.addAsset(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        nativeAdUnit.addAsset(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        nativeAdUnit.addAsset(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        nativeAdUnit.addAsset(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        nativeAdUnit.addAsset(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        nativeAdUnit.addAsset(cta)

        return nativeAdUnit
    }

    private fun createNativeAdView(): MaxNativeAdView {
        val binder = MaxNativeAdViewBinder.Builder(R.layout.view_native_ad_max)
            .setTitleTextViewId(R.id.tvHeadline)
            .setBodyTextViewId(R.id.tvBody)
            .setIconImageViewId(R.id.imgIco)
            .setMediaContentViewGroupId(R.id.frameMedia)
            .setCallToActionButtonId(R.id.btnCallToAction)
            .build()
        return MaxNativeAdView(binder, this)
    }


    override fun onDestroy() {
        super.onDestroy()
        nativeAdLoader?.destroy()
    }

}
