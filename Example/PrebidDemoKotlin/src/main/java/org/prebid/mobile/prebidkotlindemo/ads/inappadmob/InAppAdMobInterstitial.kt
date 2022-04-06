package org.prebid.mobile.prebidkotlindemo.ads.inappadmob

import android.app.Activity
import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.admob.AdMobInterstitialMediationUtils
import org.prebid.mobile.admob.PrebidInterstitialAdapter
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
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
        val mediationUtils = AdMobInterstitialMediationUtils(extras)
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
                    interstitialAd?.show(activity)
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
        PrebidMobile.setStoredAuctionResponse(null)
        interstitialAd = null
    }

}