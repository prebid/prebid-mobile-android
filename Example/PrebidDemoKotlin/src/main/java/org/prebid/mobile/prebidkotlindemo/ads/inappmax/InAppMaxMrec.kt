package org.prebid.mobile.prebidkotlindemo.ads.inappmax

import android.graphics.Color
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdFormat
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationBannerUtils
import com.applovin.mediation.ads.MaxAdView
import com.applovin.sdk.AppLovinSdkUtils
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.mediation.MediationBannerAdUnit


object InAppMaxMrec {

    private const val TAG = "InAppMaxMrec"

    private var adView: MaxAdView? = null
    private var adUnit: MediationBannerAdUnit? = null

    fun create(
        wrapper: ViewGroup,
        autoRefreshTime: Int,
        adUnitId: String,
        configId: String
    ) {
        adView = MaxAdView(adUnitId, MaxAdFormat.MREC, wrapper.context)
        adView?.setListener(createListener())
        adView?.setBackgroundColor(Color.TRANSPARENT)
        adView?.layoutParams = FrameLayout.LayoutParams(
            AppLovinSdkUtils.dpToPx(wrapper.context, 300),
            AppLovinSdkUtils.dpToPx(wrapper.context, 250)
        )
        wrapper.addView(adView)

        val mediationUtils =
            MaxMediationBannerUtils(adView)
        adUnit = MediationBannerAdUnit(
            wrapper.context,
            configId,
            AdSize(300, 250),
            mediationUtils
        )
        adUnit?.setRefreshInterval(autoRefreshTime)
        adUnit?.fetchDemand {
            adView?.loadAd()
        }
    }

    fun destroy() {
        adView?.destroy()

        adUnit?.destroy()
    }

    private fun createListener(): MaxAdViewAdListener {
        return object : MaxAdViewAdListener {
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                Log.e(TAG, "On ad load failed: " + error?.message)
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                Log.d(TAG, "On ad displayed")
            }

            override fun onAdLoaded(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
            override fun onAdExpanded(ad: MaxAd?) {}
            override fun onAdCollapsed(ad: MaxAd?) {}
        }
    }

}