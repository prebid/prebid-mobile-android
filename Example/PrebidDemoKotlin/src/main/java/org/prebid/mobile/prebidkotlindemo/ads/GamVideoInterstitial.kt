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
import org.prebid.mobile.*


object GamVideoInterstitial {
    private val TAG = GamVideoInterstitial::class.java.simpleName

    private var adUnit: AdUnit? = null
//    private var amInterstitial: PublisherInterstitialAd? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val request = AdManagerAdRequest.Builder().build()
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)

        val videoInterstitialAdUnit = VideoInterstitialAdUnit(configId)
        val parameters = VideoBaseAdUnit.Parameters().apply {
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
        videoInterstitialAdUnit.parameters = parameters
        adUnit = videoInterstitialAdUnit
        adUnit?.fetchDemand(request) { resultCode ->
            Log.d(TAG, "Result code: $resultCode")
            loadAd(activity, adUnitId, request)
        }

    }

    private fun loadAd(
        activity: Activity,
        adUnitId: String,
        request: AdManagerAdRequest
    ) {
        val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
            override fun onAdLoaded(rewardedInterstitialAd: AdManagerInterstitialAd) {
                super.onAdLoaded(rewardedInterstitialAd)
                rewardedInterstitialAd.show(activity)
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)

                val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlertDialog.Builder(activity, R.style.Theme_Material_Dialog_Alert)
                } else {
                    AlertDialog.Builder(activity)
                }
                Log.d(TAG, "onAdFailedToLoad: $loadAdError")
                builder.setTitle("Failed to load DFP interstitial ad")
                    .setMessage("Error: $loadAdError")
                    .setIcon(R.drawable.ic_dialog_alert)
                    .show()
            }
        }


        AdManagerInterstitialAd.load(
            activity,
            adUnitId,
            request,
            adLoadCallback
        )
    }

    fun destroy() {
        adUnit = null
    }
}