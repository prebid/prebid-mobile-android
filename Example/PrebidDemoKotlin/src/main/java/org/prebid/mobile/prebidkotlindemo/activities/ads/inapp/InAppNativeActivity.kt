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
package org.prebid.mobile.prebidkotlindemo.activities.ads.inapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import org.prebid.mobile.*
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import org.prebid.mobile.prebidkotlindemo.utils.ImageUtils
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider

class InAppNativeActivity : BaseAdActivity() {

    companion object {
        const val CONFIG_ID = "imp-prebid-banner-native-styles"
    }

    private var nativeAdUnit: MediationNativeAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val extras = Bundle()
        nativeAdUnit = configureNativeAdUnit(extras)
        nativeAdUnit?.fetchDemand {
            inflatePrebidNativeAd(NativeAdProvider.getNativeAd(extras)!!)
        }
    }

    private fun configureNativeAdUnit(extras: Bundle): MediationNativeAdUnit {
        val nativeAdUnit = MediationNativeAdUnit(CONFIG_ID, extras)

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

    private fun inflatePrebidNativeAd(ad: PrebidNativeAd) {
        val nativeContainer = LinearLayout(this)
        nativeContainer.orientation = LinearLayout.VERTICAL
        val iconAndTitle = LinearLayout(this)
        iconAndTitle.orientation = LinearLayout.HORIZONTAL
        val icon = ImageView(this)
        icon.layoutParams = LinearLayout.LayoutParams(160, 160)
        ImageUtils.download(ad.iconUrl, icon)
        iconAndTitle.addView(icon)
        val title = TextView(this)
        title.textSize = 20f
        title.text = ad.title
        iconAndTitle.addView(title)
        nativeContainer.addView(iconAndTitle)
        val image = ImageView(this)
        image.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        ImageUtils.download(ad.imageUrl, image)
        nativeContainer.addView(image)
        val description = TextView(this)
        description.textSize = 18f
        description.text = ad.description
        nativeContainer.addView(description)
        val cta = Button(this)
        cta.text = ad.callToAction
        cta.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://openx.com"))
            startActivity(browserIntent)
        }
        nativeContainer.addView(cta)
        adWrapperView.addView(nativeContainer)
    }

    override fun onDestroy() {
        super.onDestroy()
        nativeAdUnit?.destroy()
    }

}
