package org.prebid.mobile.prebidkotlindemo.ads

import android.R
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.google.android.gms.ads.rewarded.RewardItem
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.prebid.mobile.*


object GamVideoRewarded {
    private const val TAG: String = "GamVideoRewarded"

    private var adUnit: RewardedVideoAdUnit? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val parameters = VideoBaseAdUnit.Parameters()
        parameters.mimes = listOf("video/mp4")
        parameters.protocols = listOf(Signals.Protocols.VAST_2_0)
        parameters.playbackMethod = listOf(Signals.PlaybackMethod.AutoPlaySoundOff)
        val builder = AdManagerAdRequest.Builder()
        adUnit = RewardedVideoAdUnit(configId)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit!!.parameters = parameters
        adUnit!!.fetchDemand(builder) {
            val request = builder.build()
            RewardedAd.load(
                activity,
                adUnitId,
                request,
                createListener(activity)
            )
        }
    }

    fun destroy() {
        if (adUnit != null) {
            adUnit!!.stopAutoRefresh()
            adUnit = null
        }
    }

    private fun createListener(activity: Activity): RewardedAdLoadCallback {
        return object : RewardedAdLoadCallback() {
            override fun onAdLoaded(rewardedAd: RewardedAd) {
                Log.e(TAG, "Ad Loaded")
                rewardedAd.show(
                    activity
                ) { }
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                Log.e(TAG, loadAdError.message)
            }
        }
    }
}