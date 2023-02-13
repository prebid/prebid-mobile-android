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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.rendering

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import org.prebid.mobile.*
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import org.prebid.mobile.prebidkotlindemo.utils.ImageUtils

class GamRenderingApiNativeActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/apollo_custom_template_native_ad_unit"
        const val CONFIG_ID = "imp-prebid-banner-native-styles"
        const val CUSTOM_FORMAT_ID = "11934135"
    }

    private var adView: AdManagerAdView? = null
    private var unifiedNativeAd: NativeAd? = null
    private var adUnit: NativeAdUnit? = null
    private var adLoader: AdLoader? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        adUnit = configureNativeAdUnit()
        val adRequest = AdManagerAdRequest.Builder().build()
        adLoader = createAdLoader(adWrapperView)
        adUnit?.fetchDemand(adRequest) {
            adLoader!!.loadAd(adRequest)
        }
    }

    private fun configureNativeAdUnit(): NativeAdUnit {
        val adUnit = NativeAdUnit(CONFIG_ID)
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
        val methods = ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD>()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            adUnit.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        adUnit.addAsset(title)
        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        adUnit.addAsset(icon)
        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        adUnit.addAsset(image)
        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        adUnit.addAsset(data)
        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        adUnit.addAsset(body)
        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        adUnit.addAsset(cta)
        return adUnit
    }

    private fun createAdLoader(
        wrapper: ViewGroup,
    ): AdLoader {
        val onGamAdLoaded = OnAdManagerAdViewLoadedListener { adManagerAdView: AdManagerAdView? ->
            Log.d("GamNative", "Gam loaded")
            adView = adManagerAdView
            wrapper.addView(adManagerAdView)
        }
        val onUnifiedAdLoaded = NativeAd.OnNativeAdLoadedListener { unifiedNativeAd: NativeAd ->
            Log.d("GamNative", "Unified native loaded")
            this.unifiedNativeAd = unifiedNativeAd
        }
        val onCustomAdLoaded =
            NativeCustomFormatAd.OnCustomFormatAdLoadedListener { nativeCustomTemplateAd: NativeCustomFormatAd? ->
                Log.d("GamNative", "Custom ad loaded")
                AdViewUtils.findNative(nativeCustomTemplateAd!!, object : PrebidNativeAdListener {
                    override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {
                        inflatePrebidNativeAd(ad, wrapper)
                    }

                    override fun onPrebidNativeNotFound() {
                        Log.e("GamNative", "onPrebidNativeNotFound")
                    }

                    override fun onPrebidNativeNotValid() {
                        Log.e("GamNative", "onPrebidNativeNotFound")
                    }
                })
            }
        return AdLoader.Builder(wrapper.context, AD_UNIT_ID).forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(CUSTOM_FORMAT_ID, onCustomAdLoaded) { _: NativeCustomFormatAd?, _: String? -> }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Toast.makeText(wrapper.context, "DFP onAdFailedToLoad", Toast.LENGTH_SHORT).show()
                }
            }).build()
    }

    private fun inflatePrebidNativeAd(
        ad: PrebidNativeAd, wrapper: ViewGroup
    ) {
        val nativeContainer = View.inflate(wrapper.context, R.layout.layout_native, null)
        ad.registerView(nativeContainer, object : PrebidNativeAdEventListener {
            override fun onAdClicked() {}

            override fun onAdImpression() {}

            override fun onAdExpired() {}
        })
        val icon = nativeContainer.findViewById<ImageView>(R.id.imgIcon)
        ImageUtils.download(ad.iconUrl, icon)
        val title = nativeContainer.findViewById<TextView>(R.id.tvTitle)
        title.text = ad.title
        val image = nativeContainer.findViewById<ImageView>(R.id.imgImage)
        ImageUtils.download(ad.imageUrl, image)
        val description = nativeContainer.findViewById<TextView>(R.id.tvDesc)
        description.text = ad.description
        val cta = nativeContainer.findViewById<Button>(R.id.btnCta)
        cta.text = ad.callToAction
        wrapper.addView(nativeContainer)
    }


    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
        unifiedNativeAd?.destroy()
        adUnit?.stopAutoRefresh()
    }

}
