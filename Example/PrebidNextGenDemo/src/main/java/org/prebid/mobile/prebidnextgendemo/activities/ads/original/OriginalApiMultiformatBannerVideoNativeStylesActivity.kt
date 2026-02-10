package org.prebid.mobile.prebidnextgendemo.activities.ads.original

import android.os.Bundle
import android.util.Log
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
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
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.AdViewUtils.PbFindSizeListener
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.original.PrebidAdUnit
import org.prebid.mobile.api.original.PrebidRequest
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity

class OriginalApiMultiformatBannerVideoNativeStylesActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-multiformat-native-styles"
        const val CONFIG_ID_BANNER = "prebid-demo-banner-300-250"
        const val CONFIG_ID_NATIVE = "prebid-demo-banner-native-styles"
        const val CONFIG_ID_VIDEO = "prebid-demo-video-outstream-original-api"
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
        val requestBuilder = BannerAdRequest.Builder(
            AD_UNIT_ID,
            listOf(AdSize.FLUID, AdSize.BANNER, AdSize.MEDIUM_RECTANGLE)
        )
        prebidAdUnit?.fetchDemand(requestBuilder, prebidRequest) {
            loadAd(requestBuilder.build())
        }
    }

    private fun loadAd(request: BannerAdRequest) {
        // 4. Load an ad
        val adView = AdView(this)
        adView.loadAd(request, object : AdLoadCallback<BannerAd> {
            override fun onAdLoaded(ad: BannerAd) {
                super.onAdLoaded(ad)
                lifecycleScope.launch(Dispatchers.Main) {
                    AdViewUtils.findPrebidCreativeSize(adView, object : PbFindSizeListener {
                        override fun success(width: Int, height: Int) {
                            adView.resize(AdSize(width, height))
                        }

                        override fun failure(error: PbFindSizeError) {}
                    })
                }
            }

            override fun onAdFailedToLoad(adError: LoadAdError) {
                super.onAdFailedToLoad(adError)
                Log.e(TAG, "Ad failed to load: $adError")
            }
        })
        adWrapperView.addView(adView)
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

    override fun onDestroy() {
        super.onDestroy()
        prebidAdUnit?.destroy()
    }

}
