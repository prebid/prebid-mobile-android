package org.prebid.mobile.prebidkotlindemo.ads.inappadmob

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.initialization.InitializationStatus
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.admob.AdMobMediationBannerUtils
import org.prebid.mobile.admob.PrebidBannerAdapter
import org.prebid.mobile.api.mediation.MediationBannerAdUnit

object InAppAdMobBanner {

    private const val TAG = "InAppAdMobBanner"

    private var bannerView: AdView? = null
    private var adUnit: MediationBannerAdUnit? = null

    fun create(
        activity: Activity,
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        MobileAds.initialize(wrapper.context) { status ->
            Log.d("MobileAds", "Initialization complete.")
            logAdaptersInitializationStatus(status)
        }
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        /** Google recommends put activity for mediation ad networks */
        bannerView = AdView(activity)
        bannerView?.adSize = AdSize.BANNER
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(
                    "AdListener",
                    "Won ad network: ${bannerView?.responseInfo?.mediationAdapterClassName}"
                )
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                Log.d("AdListener", "Failed to load ad $p0")
            }
        }
        wrapper.addView(bannerView)

        val extras = Bundle()
        val request = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidBannerAdapter::class.java, extras)
            .build()
        val mediationUtils = AdMobMediationBannerUtils(extras, bannerView)

        adUnit = MediationBannerAdUnit(
            wrapper.context,
            configId,
            org.prebid.mobile.AdSize(width, height),
            mediationUtils
        )
        adUnit?.setRefreshInterval(autoRefreshTime / 1000)
        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")
            bannerView?.loadAd(request)
        }
    }

    fun destroy() {
        bannerView?.destroy()
        bannerView = null
        adUnit?.destroy()
        adUnit = null
    }

    private fun logAdaptersInitializationStatus(status: InitializationStatus) {
        val statusMap = status.adapterStatusMap
        for (adapterClass in statusMap.keys) {
            val adapterStatus = statusMap[adapterClass]
            val adapterDescription = adapterStatus?.description ?: ""
            if (adapterDescription.isNotBlank()) {
                Log.e("MobileAds", "Mediation adapter: $adapterClass $adapterDescription")
            } else {
                Log.d("MobileAds", "Mediation adapter: $adapterClass")
            }
        }
    }

}