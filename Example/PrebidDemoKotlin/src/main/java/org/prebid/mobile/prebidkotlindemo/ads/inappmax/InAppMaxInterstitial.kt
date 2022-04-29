package org.prebid.mobile.prebidkotlindemo.ads.inappmax

import android.app.Activity
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationInterstitialUtils
import com.applovin.mediation.ads.MaxInterstitialAd
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import java.util.*

object InAppMaxInterstitial {

    private var maxInterstitialAd: MaxInterstitialAd? = null
    private var adUnit: MediationInterstitialAdUnit? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String
    ) {
        maxInterstitialAd = MaxInterstitialAd(adUnitId, activity)
        maxInterstitialAd?.setListener(createListener())

        val mediationUtils =
            MaxMediationInterstitialUtils(
                maxInterstitialAd
            )
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            EnumSet.of(AdUnitFormat.DISPLAY),
            mediationUtils
        )
        adUnit?.fetchDemand {
            maxInterstitialAd?.loadAd()
        }
    }

    fun destroy() {
        maxInterstitialAd?.destroy()

        adUnit?.destroy()
    }

    private fun createListener(): MaxAdListener {
        return object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                maxInterstitialAd?.showAd()
            }

            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
        }
    }

}