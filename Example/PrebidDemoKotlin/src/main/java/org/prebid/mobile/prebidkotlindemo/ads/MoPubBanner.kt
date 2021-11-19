package org.prebid.mobile.prebidkotlindemo.ads

import android.util.Log
import android.view.ViewGroup
import com.mopub.mobileads.MoPubView
import org.prebid.mobile.AdUnit
import org.prebid.mobile.BannerAdUnit

object MoPubBanner {

    private const val TAG = "MoPubBanner"

    private var adUnit: AdUnit? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        width: Int,
        height: Int,
        moPubViewAdSize: MoPubView.MoPubAdSize,
        adUnitId: String,
        configId: String
    ) {
        val adView = MoPubView(wrapper.context)

        adView.setAdUnitId(adUnitId)
        adView.minimumWidth = width
        adView.minimumHeight = height
        adView.adSize = moPubViewAdSize
        wrapper.addView(adView)

        adUnit = BannerAdUnit(configId, width, height)
        adUnit?.setAutoRefreshPeriodMillis(autoRefreshTime)
        adUnit?.fetchDemand(adView) { resultCode ->
            Log.d(TAG, "Result code: $resultCode")
            adView.loadAd()
        }
    }

    fun destroy() {
        adUnit?.stopAutoRefresh()
        adUnit = null
    }

}