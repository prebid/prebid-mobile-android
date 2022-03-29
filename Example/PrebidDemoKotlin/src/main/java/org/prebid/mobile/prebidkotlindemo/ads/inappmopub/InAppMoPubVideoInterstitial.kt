package org.prebid.mobile.prebidkotlindemo.ads.inappmopub

import android.annotation.SuppressLint
import android.app.Activity
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mediation.MoPubInterstitialMediationUtils
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import java.util.*

object InAppMoPubVideoInterstitial {

    @SuppressLint("StaticFieldLeak")
    private var moPubInterstitial: MoPubInterstitial? = null
    private var adUnit: MediationInterstitialAdUnit? = null

    fun create(
        activity: Activity,
        adUnitId: String,
        configId: String,
        minPercentageWidth: Int,
        minPercentageHeight: Int
    ) {
        moPubInterstitial = MoPubInterstitial(activity, adUnitId)
        moPubInterstitial?.interstitialAdListener = object : MoPubInterstitial.InterstitialAdListener {
            override fun onInterstitialLoaded(p0: MoPubInterstitial?) {
                moPubInterstitial?.show()
            }

            override fun onInterstitialFailed(p0: MoPubInterstitial?, p1: MoPubErrorCode?) {}
            override fun onInterstitialShown(p0: MoPubInterstitial?) {}
            override fun onInterstitialClicked(p0: MoPubInterstitial?) {}
            override fun onInterstitialDismissed(p0: MoPubInterstitial?) {}
        }
        val mediationUtils = MoPubInterstitialMediationUtils(moPubInterstitial)
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            EnumSet.of(AdUnitFormat.VIDEO),
            mediationUtils
        )
        adUnit?.setMinSizePercentage(minPercentageWidth, minPercentageHeight)

        MoPub.initializeSdk(activity, SdkConfiguration.Builder(adUnitId).build()) {
            adUnit?.fetchDemand {
                moPubInterstitial?.load()
            }
        }
    }

    fun destroy() {
        moPubInterstitial?.destroy()
        moPubInterstitial = null

        adUnit?.destroy()
        adUnit = null
    }

}