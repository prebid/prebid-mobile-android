package org.prebid.mobile.prebidnextgendemo.activities.ads.original

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
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
import com.google.android.libraries.ads.mobile.sdk.nativead.NativeAdView
import com.google.common.collect.Lists
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.NativeAdUnit
import org.prebid.mobile.NativeAsset
import org.prebid.mobile.NativeDataAsset
import org.prebid.mobile.NativeEventTracker
import org.prebid.mobile.NativeImageAsset
import org.prebid.mobile.NativeParameters
import org.prebid.mobile.NativeTitleAsset
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.PrebidNativeAdListener
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.original.PrebidAdUnit
import org.prebid.mobile.api.original.PrebidRequest
import org.prebid.mobile.prebidnextgendemo.R
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import org.prebid.mobile.prebidnextgendemo.utils.ImageUtils

class OriginalApiMultiformatBannerVideoNativeInAppActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-multiformat"
        const val CONFIG_ID_BANNER = "prebid-demo-banner-300-250"
        const val CONFIG_ID_NATIVE = "prebid-demo-banner-native-styles"
        const val CONFIG_ID_VIDEO = "prebid-demo-video-outstream-original-api"
        const val CUSTOM_FORMAT_ID = "12304464"
    }

    private var prebidAdUnit: PrebidAdUnit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // Random only for test cases. For production use one config id.
        val configId = listOf(CONFIG_ID_BANNER, CONFIG_ID_VIDEO, CONFIG_ID_NATIVE).random()

        // 1. Create PrebidAdUnit with configId
        prebidAdUnit = PrebidAdUnit(configId)

        // 2. Create PrebidRequest with needed multiformat parameters
        val prebidRequest = PrebidRequest()
        prebidRequest.setBannerParameters(createBannerParameters())
        prebidRequest.setVideoParameters(createVideoParameters())
        prebidRequest.setNativeParameters(createNativeParameters())

        // 3. Make a bid request to Prebid Server
        val requestBuilder = NativeAdRequest.Builder(
            AD_UNIT_ID,
            listOf(NativeAdType.NATIVE, NativeAdType.CUSTOM_NATIVE, NativeAdType.BANNER)
        ).setCustomFormatIds(listOf(CUSTOM_FORMAT_ID))
            .setAdSizes(listOf(AdSize.BANNER, AdSize.MEDIUM_RECTANGLE))
        prebidAdUnit?.fetchDemand(requestBuilder, prebidRequest) {
            load(requestBuilder.build())
        }
    }

    private fun load(request: NativeAdRequest) {
        // 4. Load an ad
        val adCallback = object : NativeAdLoaderCallback {
            override fun onNativeAdLoaded(nativeAd: NativeAd) {
                super.onNativeAdLoaded(nativeAd)
                Log.d(TAG, "Unified native loaded")
                lifecycleScope.launch(Dispatchers.Main) {
                    showNativeAd(nativeAd, adWrapperView)
                }
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
                        "Ad failed to load!",
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


    private fun createBannerParameters(): BannerParameters {
        val params = BannerParameters()
        params.adSizes = mutableSetOf(org.prebid.mobile.AdSize(300, 250))
        return params
    }

    private fun createVideoParameters(): VideoParameters {
        val params = VideoParameters(listOf("video/mp4"))
        params.adSize = org.prebid.mobile.AdSize(320, 480)
        return params
    }

    private fun createNativeParameters(): NativeParameters {
        val assets = mutableListOf<NativeAsset>()

        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        assets.add(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        assets.add(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        assets.add(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        assets.add(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        assets.add(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        assets.add(cta)

        val nativeParameters = NativeParameters(assets)
        nativeParameters.addEventTracker(
            NativeEventTracker(
                NativeEventTracker.EVENT_TYPE.IMPRESSION,
                arrayListOf(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
            )
        )
        nativeParameters.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        nativeParameters.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        nativeParameters.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        return nativeParameters
    }

    private fun showBannerAd(bannerAd: BannerAd) {
        // 5.1. Show banner
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

    private fun showNativeAd(ad: NativeAd, wrapper: ViewGroup) {
        // 5.2. Show Next native
        val nativeContainer: NativeAdView =
            View.inflate(wrapper.context, R.layout.next_layout_native, null) as NativeAdView

        val icon = nativeContainer.findViewById<ImageView>(R.id.imgIcon)
        val iconUrl = ad.icon?.uri?.toString()
        if (iconUrl != null) {
            ImageUtils.download(iconUrl, icon)
        }

        val title = nativeContainer.findViewById<TextView>(R.id.tvTitle)
        title.text = ad.headline

        val image = nativeContainer.findViewById<ImageView>(R.id.imgImage)
        val imageUrl = ad.image?.uri?.toString()
        if (imageUrl != null) {
            ImageUtils.download(imageUrl, image)
        }

        val description = nativeContainer.findViewById<TextView>(R.id.tvDesc)
        description.text = ad.body

        val cta = nativeContainer.findViewById<Button>(R.id.btnCta)
        cta.text = ad.callToAction

        nativeContainer.registerNativeAd(ad, null)
        wrapper.addView(nativeContainer)
    }

    private fun showPrebidNativeAd(customNativeAd: CustomNativeAd) {
        // 5.3. Show Prebid native
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
            null
        )

        adWrapperView.addView(nativeContainer)
    }


    override fun onDestroy() {
        super.onDestroy()
        prebidAdUnit?.destroy()
    }
}
