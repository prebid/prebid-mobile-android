package org.prebid.mobile.prebidkotlindemo.ads.inappmax

import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationBannerUtils
import com.applovin.mediation.ads.MaxAdView
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.prebidkotlindemo.R

object InAppMaxBanner {

    private const val TAG = "InAppMaxBanner"

    private var adView: MaxAdView? = null
    private var adUnit: MediationBannerAdUnit? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        adUnitId: String,
        configId: String
    ) {
        adView = MaxAdView(adUnitId, wrapper.context)
        adView?.setListener(createListener())
        adView?.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            wrapper.context.resources.getDimensionPixelSize(R.dimen.banner_height)
        )
        wrapper.addView(adView)

        val mediationUtils =
            MaxMediationBannerUtils(adView)
        adUnit = MediationBannerAdUnit(
            wrapper.context,
            configId,
            AdSize(320, 50),
            mediationUtils
        )
        adUnit?.setRefreshInterval(autoRefreshTime)
        adUnit?.fetchDemand {
            adView?.loadAd()
        }
    }

    fun destroy() {
        adView?.stopAutoRefresh()
        adView?.destroy()

        adUnit?.stopRefresh()
        adUnit?.destroy()
    }

    private fun createListener(): MaxAdViewAdListener {
        return object : MaxAdViewAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                Log.d(TAG, "onAdLoaded()")
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdExpanded(ad: MaxAd?) {}
            override fun onAdCollapsed(ad: MaxAd?) {}
            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
        }
    }

}