package org.prebid.mobile.prebidkotlindemo.ads

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.AdUnit
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.PrebidMobile

object GamInterstitial {

    private const val TAG = "GamInterstitial"

    private var adUnit: AdUnit? = null

    fun create(
        activity: Activity,
        autoRefreshTime: Int,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        val requestBuilder = AdManagerAdRequest.Builder()
        val request = requestBuilder.build()
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)

        adUnit = InterstitialAdUnit(configId)
        adUnit?.setAutoRefreshPeriodMillis(autoRefreshTime)
        adUnit?.fetchDemand(request) { resultCode ->
            Log.d(TAG, "Result code: $resultCode")

            val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(adManagerInterstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(adManagerInterstitialAd)
                    adManagerInterstitialAd.show(activity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)

                    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                    } else {
                        AlertDialog.Builder(activity)
                    }
                    builder.setTitle("Failed to load DFP interstitial ad")
                        .setMessage("Error: $loadAdError")
                        .setIcon(android.R.drawable.ic_dialog_alert)
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
    }

    fun destroy() {
        adUnit?.stopAutoRefresh()

        adUnit = null
    }

}