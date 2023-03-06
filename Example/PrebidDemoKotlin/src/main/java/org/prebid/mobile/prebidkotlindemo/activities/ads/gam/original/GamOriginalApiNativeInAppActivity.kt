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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd.OnCustomFormatAdLoadedListener
import org.prebid.mobile.*
import org.prebid.mobile.NativeEventTracker.EVENT_TRACKING_METHOD
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import org.prebid.mobile.prebidkotlindemo.utils.ImageUtils

class GamOriginalApiNativeInAppActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/apollo_custom_template_native_ad_unit"
        const val CONFIG_ID = "imp-prebid-banner-native-styles"
        const val CUSTOM_FORMAT_ID = "11934135"
        const val TAG = "GamOriginalNativeInApp"
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
        // 1. Create NativeAdUnit
        adUnit = NativeAdUnit(CONFIG_ID)
        adUnit?.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        adUnit?.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        adUnit?.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        // 2. Add native assets and trackers
        addNativeAssets(adUnit)

        // 3. Make a bid request to Prebid Server
        val adRequest = AdManagerAdRequest.Builder().build()
        adUnit?.fetchDemand(adRequest) {

            // 4. Load a GAM Native Ad
            adLoader = createAdLoader(adWrapperView)
            adLoader?.loadAd(adRequest)
        }
    }

    private fun inflatePrebidNativeAd(ad: PrebidNativeAd, wrapper: ViewGroup) {

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

    private fun createAdLoader(wrapper: ViewGroup): AdLoader? {
        val onGamAdLoaded = OnAdManagerAdViewLoadedListener { adManagerAdView: AdManagerAdView ->
            Log.d(TAG, "Gam loaded")
            adView = adManagerAdView
            wrapper.addView(adManagerAdView)
        }

        val onUnifiedAdLoaded = OnNativeAdLoadedListener { unifiedNativeAd: NativeAd? ->
            Log.d(TAG, "Unified native loaded")
            this.unifiedNativeAd = unifiedNativeAd
        }

        val onCustomAdLoaded = OnCustomFormatAdLoadedListener { nativeCustomTemplateAd: NativeCustomFormatAd? ->
            Log.d(TAG, "Custom ad loaded")

            // 5. Find Prebid Native Ad
            AdViewUtils.findNative(nativeCustomTemplateAd!!, object : PrebidNativeAdListener {
                override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {

                    // 6. Render native ad
                    inflatePrebidNativeAd(ad, wrapper)
                }

                override fun onPrebidNativeNotFound() {
                    Log.e(TAG, "onPrebidNativeNotFound")
                }

                override fun onPrebidNativeNotValid() {
                    Log.e(TAG, "onPrebidNativeNotValid")
                }
            })
        }

        return AdLoader.Builder(wrapper.context, AD_UNIT_ID)
            .forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(
                CUSTOM_FORMAT_ID, onCustomAdLoaded
            ) { _: NativeCustomFormatAd?, _: String? -> }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e(TAG, "DFP onAdFailedToLoad")
                }
            })
            .build()
    }

    private fun addNativeAssets(adUnit: NativeAdUnit?)  {
        // ADD NATIVE ASSETS

        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        adUnit?.addAsset(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        adUnit?.addAsset(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        adUnit?.addAsset(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        adUnit?.addAsset(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        adUnit?.addAsset(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        adUnit?.addAsset(cta)

        // ADD NATIVE EVENT TRACKERS
        val methods = ArrayList<EVENT_TRACKING_METHOD>()
        methods.add(EVENT_TRACKING_METHOD.IMAGE)
        methods.add(EVENT_TRACKING_METHOD.JS)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            adUnit?.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
        adUnit?.stopAutoRefresh()
        unifiedNativeAd?.destroy()
    }

}
