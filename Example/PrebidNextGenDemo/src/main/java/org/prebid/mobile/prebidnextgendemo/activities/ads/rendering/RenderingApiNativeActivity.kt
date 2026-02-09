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
package org.prebid.mobile.prebidnextgendemo.activities.ads.rendering

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.nativead.CustomNativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAd.NativeAdType
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoader
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdLoaderCallback
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdRequest
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.NativeAdUnit
import org.prebid.mobile.NativeDataAsset
import org.prebid.mobile.NativeEventTracker
import org.prebid.mobile.NativeImageAsset
import org.prebid.mobile.NativeTitleAsset
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.PrebidNativeAdEventListener
import org.prebid.mobile.PrebidNativeAdListener
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import org.prebid.mobile.prebidnextgendemo.utils.ImageUtils

class RenderingApiNativeActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/apollo_custom_template_native_ad_unit"
        const val CONFIG_ID = "prebid-demo-banner-native-styles"
        const val CUSTOM_FORMAT_ID = "11934135"
    }

    private var adView: AdView? = null
    private var unifiedNativeAd: NativeAd? = null
    private var adUnit: NativeAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        adUnit = configureNativeAdUnit()
        val builder = NativeAdRequest.Builder(
            AD_UNIT_ID,
            listOf(NativeAdType.NATIVE, NativeAdType.CUSTOM_NATIVE, NativeAdType.BANNER)
        )
            .setCustomFormatIds(listOf(CUSTOM_FORMAT_ID))
            .setAdSize(AdSize.BANNER)
        adUnit?.fetchDemand(builder) {
            loadAd(builder.build())
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

    private fun loadAd(
        request: NativeAdRequest,
    ) {
        val adCallback = object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                super.onNativeAdLoaded(nativeAd)
                Log.d(TAG, "Unified native loaded")
                Log.d("GamNative", "Unified native loaded")
                unifiedNativeAd = nativeAd
            }

            override fun onCustomNativeAdLoaded(customNativeAd: CustomNativeAd) {
                super.onCustomNativeAdLoaded(customNativeAd)
                Log.d(TAG, "Custom ad loaded")
                lifecycleScope.launch(Dispatchers.Main) {
                    showPrebidNativeAd(customNativeAd)
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                Log.d(TAG, "Ad failed $adError")
                lifecycleScope.launch(Dispatchers.Main) {
                    Toast.makeText(
                        applicationContext,
                        "DFP onAdFailedToLoad",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onAdLoadingCompleted() {
                super.onAdLoadingCompleted()
                Log.d(TAG, "Ad loaded")
            }

            override fun onBannerAdLoaded(bannerAd: BannerAd) {
                super.onBannerAdLoaded(bannerAd)
                Log.d(TAG, "Banner ad loaded")
                lifecycleScope.launch(Dispatchers.Main) {
                    showBannerAd(bannerAd)
                }
            }
        }
        NativeAdLoader.load(request, adCallback)
    }

    private fun showPrebidNativeAd(customNativeAd: CustomNativeAd) {

        AdViewUtils.findNative(customNativeAd, object : PrebidNativeAdListener {
            override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {
                inflatePrebidNativeAd(ad)
            }

            override fun onPrebidNativeNotFound() {
                Log.e("PrebidAdViewUtils", "Find native failed: native not found")
            }

            override fun onPrebidNativeNotValid() {
                Log.e("PrebidAdViewUtils", "Find native failed: native not valid")
            }
        })
    }

    private fun inflatePrebidNativeAd(ad: PrebidNativeAd) {
        val nativeContainer = View.inflate(this, R.layout.prebid_layout_native, null)

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

        ad.registerView(
            nativeContainer,
            Lists.newArrayList(icon, title, image, description, cta),
            SafeNativeListener()
        )

        adWrapperView.addView(nativeContainer)
    }

    /**
     * It's important to use class implementation instead of anonymous object.
     */
    private class SafeNativeListener : PrebidNativeAdEventListener {
        override fun onAdClicked() {}
        override fun onAdImpression() {}
        override fun onAdExpired() {}
    }

    private fun showBannerAd(bannerAd: BannerAd) {
        val adView = AdView(this)
        adView.registerBannerAd(bannerAd, this)
        adWrapperView.addView(adView)
        AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
            override fun success(width: Int, height: Int) {
                adView.resize(AdSize(width, height))
            }

            override fun failure(error: PbFindSizeError) {}
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        adView?.destroy()
        unifiedNativeAd?.destroy()
        adUnit?.stopAutoRefresh()
    }

}
