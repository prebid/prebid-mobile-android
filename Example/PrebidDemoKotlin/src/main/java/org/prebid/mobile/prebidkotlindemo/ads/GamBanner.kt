package org.prebid.mobile.prebidkotlindemo.ads

import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.MobileAds
import com.google.android.gms.ads.RequestConfiguration
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.AdUnit
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError

object GamBanner {

    private const val TAG = "GamBanner"

    private var adUnit: AdUnit? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        adUnitId: String,
        configId: String
    ) {
        val adView = AdManagerAdView(wrapper.context)
        adView.adUnitId = adUnitId

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "Banner loaded!")

                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        adView.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {}
                })

            }
        }
        adView.setAdSizes(AdSize(width, height))

        wrapper.addView(adView)

        val request = AdManagerAdRequest.Builder().build()
        adUnit = BannerAdUnit(configId, width, height)
        adUnit?.setAutoRefreshPeriodMillis(autoRefreshTime)
        adUnit?.fetchDemand(request) { resultCode ->
            Log.d(TAG, "Result code: $resultCode")
            adView.loadAd(request)
        }
    }

    fun destroy() {
        adUnit?.stopAutoRefresh()
        adUnit = null
    }

}