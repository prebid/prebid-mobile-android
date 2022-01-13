package org.prebid.mobile.prebidkotlindemo.ads

import android.app.Activity
import android.os.Build
import android.util.Log
import androidx.appcompat.app.AlertDialog
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import org.prebid.mobile.AdUnit
import org.prebid.mobile.InterstitialAdUnit

object MoPubInterstitial {

    private const val TAG = "MoPubInterstitial"

    private var adUnit: AdUnit? = null

    fun create(activity: Activity, autoRefreshTime: Int, adUnitId: String, configId: String) {
        val interstitial = MoPubInterstitial(activity, adUnitId)
        interstitial.interstitialAdListener = object : MoPubInterstitial.InterstitialAdListener {
            override fun onInterstitialLoaded(interstitial: MoPubInterstitial) {
                interstitial.show()
            }

            override fun onInterstitialFailed(interstitial: MoPubInterstitial, errorCode: MoPubErrorCode) {
                val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlertDialog.Builder(activity, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    AlertDialog.Builder(activity)
                }
                builder.setTitle("Failed to load MoPub interstitial ad")
                    .setMessage("Error code: $errorCode")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }

            override fun onInterstitialShown(interstitial: MoPubInterstitial) {}
            override fun onInterstitialClicked(interstitial: MoPubInterstitial) {}
            override fun onInterstitialDismissed(interstitial: MoPubInterstitial) {}
        }
        adUnit = InterstitialAdUnit(configId)
        adUnit?.setAutoRefreshPeriodMillis(autoRefreshTime)
        adUnit?.fetchDemand(interstitial) { resultCode ->
            Log.d(TAG, "Result code: $resultCode")
            interstitial.load()
        }
    }

    fun destroy() {
        adUnit?.stopAutoRefresh()
        adUnit = null
    }

}