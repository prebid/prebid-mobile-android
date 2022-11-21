package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.*
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class GamOriginalApiVideoInterstitialActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "imp-prebid-video-interstitial-320-480"
        const val STORED_RESPONSE = "response-prebid-video-interstitial-320-480-original-api"
    }

    private var adUnit: AdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // The ID of Mocked Bid Response on PBS. Only for test cases.
        PrebidMobile.setStoredAuctionResponse(STORED_RESPONSE)

        createAd()
    }

    private fun createAd() {
        val request = AdManagerAdRequest.Builder().build()

        val videoInterstitialAdUnit = VideoInterstitialAdUnit(CONFIG_ID)
        videoInterstitialAdUnit.parameters = configureVideoParameters()
        adUnit = videoInterstitialAdUnit
        adUnit?.fetchDemand(request) {
            val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(interstitialAd)
                    interstitialAd.show(this@GamOriginalApiVideoInterstitialActivity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.e("GAM", "Ad failed to load: $loadAdError")
                }
            }

            AdManagerInterstitialAd.load(
                this@GamOriginalApiVideoInterstitialActivity,
                AD_UNIT_ID,
                request,
                adLoadCallback
            )
        }
    }

    private fun configureVideoParameters(): VideoBaseAdUnit.Parameters {
        return VideoBaseAdUnit.Parameters().apply {
            placement = Signals.Placement.Interstitial
            api = listOf(
                Signals.Api.VPAID_1,
                Signals.Api.VPAID_2
            )
            maxBitrate = 1500
            minBitrate = 300
            maxDuration = 30
            minDuration = 5
            mimes = listOf("video/x-flv", "video/mp4")
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
