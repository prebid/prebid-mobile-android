package org.prebid.mobile.prebidnextgendemo.activities.ads.original

import android.os.Bundle
import android.util.Log
import com.google.android.libraries.ads.mobile.sdk.common.AdLoadCallback
import com.google.android.libraries.ads.mobile.sdk.common.AdRequest
import com.google.android.libraries.ads.mobile.sdk.common.LoadAdError
import com.google.android.libraries.ads.mobile.sdk.rewarded.RewardedAd
import org.prebid.mobile.RewardedVideoAdUnit
import org.prebid.mobile.Signals
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.prebidnextgendemo.activities.BaseAdActivity

class OriginalApiVideoRewardedActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "prebid-demo-video-rewarded-320-480-original-api"
    }

    private var adUnit: RewardedVideoAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        // 1. Create RewardedVideoAdUnit
        adUnit = RewardedVideoAdUnit(CONFIG_ID)

        // 2. Configure Video parameters
        adUnit?.videoParameters = configureVideoParameters()

        // 3. Make a bid request to Prebid Server
        val request = AdRequest.Builder(AD_UNIT_ID)
        adUnit?.fetchDemand(request) {

            // 4. Load a Rewarded Ad
            RewardedAd.load(
                request.build(),
                object : AdLoadCallback<RewardedAd> {
                    override fun onAdLoaded(ad: RewardedAd) {
                        super.onAdLoaded(ad)
                        ad.show(this@OriginalApiVideoRewardedActivity) { _ -> }
                    }

                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        super.onAdFailedToLoad(adError)
                        Log.e("NEXT", "Ad failed to load: $adError")
                    }
                }
            )
        }
    }

    private fun configureVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/mp4")).apply {
            protocols = listOf(Signals.Protocols.VAST_2_0)
            playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOff)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        adUnit?.destroy()
    }

}
