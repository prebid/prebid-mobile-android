package org.prebid.mobile.prebidkotlindemo.ads.inappadmob

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.admob.AdMobMediationInterstitialUtils
import org.prebid.mobile.admob.PrebidInterstitialAdapter
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import java.util.*

object InAppAdMobInterstitial {

    private var adUnit: MediationInterstitialAdUnit? = null
    private var interstitialAd: InterstitialAd? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        adUnitFormats: EnumSet<AdUnitFormat>,
        storedAuctionResponse: String
    ) {
        val extras = Bundle()
        val request = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidInterstitialAdapter::class.java, extras)
            .build()
        val mediationUtils = AdMobMediationInterstitialUtils(extras)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            adUnitFormats,
            mediationUtils
        )
        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            InterstitialAd.load(activity, adUnitId, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(interstitial: InterstitialAd) {
                    interstitialAd = interstitial
                    val mediationAdapter = interstitial.responseInfo.mediationAdapterClassName
                    if (mediationAdapter!!.contains("prebid")) {
                        interstitialAd?.show(activity)
                    }

                }

                override fun onAdFailedToLoad(error: LoadAdError) {
                    interstitialAd = null
                }
            })
        }
    }

    fun destroy() {
        adUnit?.destroy()
        adUnit = null
        interstitialAd = null
    }

}