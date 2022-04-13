package org.prebid.mobile.prebidkotlindemo.ads

import android.R
import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd
import com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAdLoadCallback
import org.prebid.mobile.AdUnit
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.VideoAdUnit
import org.prebid.mobile.VideoInterstitialAdUnit

object GamVideoInterstitial {
    private val TAG = GamVideoInterstitial::class.java.simpleName

    private var adUnit: AdUnit? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val request = AdManagerAdRequest.Builder().build()
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)

        adUnit = VideoInterstitialAdUnit(configId)
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
        val adLoadCallback = object : RewardedInterstitialAdLoadCallback() {
            override fun onAdLoaded(rewardedInterstitialAd: RewardedInterstitialAd) {
                super.onAdLoaded(rewardedInterstitialAd)
                rewardedInterstitialAd.show(activity) {}
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


        RewardedInterstitialAd.load(
            activity,
            adUnitId,
            request,
            adLoadCallback
        )
    }

    fun destroy() {
        adUnit?.stopAutoRefresh()
        PrebidMobile.setStoredAuctionResponse(null)
        adUnit = null
    }
}