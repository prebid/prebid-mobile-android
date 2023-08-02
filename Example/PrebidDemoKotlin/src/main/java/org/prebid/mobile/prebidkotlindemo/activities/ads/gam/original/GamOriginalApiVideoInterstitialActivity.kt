package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import java.util.*

class GamOriginalApiVideoInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "prebid-ita-video-interstitial-320-480-original-api"
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
        val request = AdManagerAdRequest.Builder().build()
        adUnit?.fetchDemand(request) {

            // 4. Load a GAM ad
            AdManagerInterstitialAd.load(
                this@GamOriginalApiVideoInterstitialActivity,
                AD_UNIT_ID,
                request,
                createAdListener()
            )
        }
    }

    private fun configureVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/x-flv", "video/mp4")).apply {
            placement = Signals.Placement.Interstitial

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

    private fun createAdListener(): AdManagerInterstitialAdLoadCallback {
        return object : AdManagerInterstitialAdLoadCallback() {
            override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                super.onAdLoaded(interstitialAd)

                // 5. Display an interstitial ad
                interstitialAd.show(this@GamOriginalApiVideoInterstitialActivity)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                Log.e("GAM", "Ad failed to load: $loadAdError")
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        adUnit?.stopAutoRefresh()
    }

}
