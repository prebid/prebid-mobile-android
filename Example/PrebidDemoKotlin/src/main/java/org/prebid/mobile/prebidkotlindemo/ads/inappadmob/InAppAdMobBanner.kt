package org.prebid.mobile.prebidkotlindemo.ads.inappadmob

import android.annotation.SuppressLint
import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.*
//import org.prebid.mobile.admob.PrebidAdMobMediationDelegate
import org.prebid.mobile.admob.PrebidAdMobRequest
import org.prebid.mobile.rendering.bidding.display.MediationBannerAdUnit

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
        configId: String
    ) {
        MobileAds.initialize(wrapper.context) { status ->
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

        /** Google recommends put activity for mediation ad networks */
        bannerView = AdView(activity)
        bannerView?.adSize = AdSize.BANNER
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d("AdListener", "Won ad network: ${bannerView?.responseInfo?.mediationAdapterClassName}")
            }

            override fun onAdFailedToLoad(p0: LoadAdError) {
                Log.d("AdListener", "Failed to load ad $p0")
            }
        }
        wrapper.addView(bannerView)

        val request = PrebidAdMobRequest.create()
////        val mediationUtils = PrebidAdMobMediationDelegate()
//        adUnit = MediationBannerAdUnit(
//            wrapper.context,
//            configId,
//            org.prebid.mobile.rendering.bidding.data.AdSize(width, height),
////            mediationUtils
//        )
//        adUnit?.setRefreshInterval(autoRefreshTime / 1000)
//        adUnit?.fetchDemand(request) { result ->
//            Log.d("Prebid", "Fetch demand result: $result")
//            bannerView?.loadAd(request)
//        }
    }

    fun destroy() {
        bannerView?.destroy()
        bannerView = null

        adUnit?.destroy()
        adUnit = null
    }

}