package org.prebid.mobile.prebidnextgendemo.activities.ads.original

import android.os.Bundle
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.interstitial.InterstitialAd
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity
import java.util.EnumSet

class OriginalApiVideoInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "prebid-demo-video-interstitial-320-480-original-api"
    }

    private var adUnit: InterstitialAdUnit? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // 1. Create InterstitialAdUnit
        adUnit = InterstitialAdUnit(CONFIG_ID, EnumSet.of(AdUnitFormat.VIDEO))

        // 2. Configure video ad unit
        adUnit?.videoParameters = configureVideoParameters()

        // 3. Make a bid request to Prebid Server
        val requestBuilder = AdRequest.Builder(AD_UNIT_ID)
        adUnit?.fetchDemand(requestBuilder) {

            // 4. Load an ad
            InterstitialAd.load(
                requestBuilder.build(),
                object : AdLoadCallback<InterstitialAd> {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.e(TAG, "Ad failed to load: $adError")
                    }

                    override fun onAdLoaded(ad: InterstitialAd) {
                        super.onAdLoaded(ad)
                        ad.show(this@OriginalApiVideoInterstitialActivity)
                    }
                }
            )
        }
    }

    private fun configureVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/x-flv", "video/mp4")).apply {
            placement = Signals.Placement.Interstitial
            plcmt = Signals.Plcmt.Interstitial

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

        adUnit?.destroy()
    }

}
