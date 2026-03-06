package org.prebid.mobile.prebidnextgendemo.activities.ads.original

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.google.android.libraries.ads.mobile.sdk.banner.AdSize
import com.google.android.libraries.ads.mobile.sdk.banner.AdView
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAd
import com.google.android.libraries.ads.mobile.sdk.banner.BannerAdRequest
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.AdViewUtils.PbFindSizeListener
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import java.util.EnumSet

class OriginalApiVideoBannerActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-original-api-video-banner"
        const val CONFIG_ID = "prebid-demo-video-outstream-original-api"
        const val WIDTH = 300
        const val HEIGHT = 250
    }

    private var adUnit: BannerAdUnit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // 1. Create BannerAdUnit
        adUnit = BannerAdUnit(CONFIG_ID, WIDTH, HEIGHT, EnumSet.of(AdUnitFormat.VIDEO))

        // 2. Configure video ad unit
        adUnit?.videoParameters = configureVideoParameters()

        // 3. Create AdView
        val adView = AdView(this)
        val adSize = AdSize(WIDTH, HEIGHT)


        adWrapperView.addView(adView)

        // 4. Make an ad request
        val requestBuilder = BannerAdRequest.Builder(AD_UNIT_ID, adSize)
        adUnit?.fetchDemand(requestBuilder) {

            // 5. Load an ad
            adView.loadAd(requestBuilder.build(), object : AdLoadCallback<BannerAd> {
                override fun onAdLoaded(ad: BannerAd) {
                    super.onAdLoaded(ad)
                    lifecycleScope.launch(Dispatchers.Main) {
                        // 6. Adjust ad view size
                        AdViewUtils.findPrebidCreativeSize(adView, object : PbFindSizeListener {
                            override fun success(width: Int, height: Int) {
                                adView.resize(AdSize(width, height))
                            }

                            override fun failure(error: PbFindSizeError) {}
                        })
                    }
                }
            })
        }
    }

    private fun configureVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/x-flv", "video/mp4")).apply {

            api = listOf(
                Signals.Api.VPAID_1,
                Signals.Api.VPAID_2
            )

            maxBitrate = 1500
            minBitrate = 300
            maxDuration = 30
            minDuration = 5
            playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOn)
            protocols = listOf(
                Signals.Protocols.VAST_2_0
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        adUnit?.stopAutoRefresh()
    }
}
