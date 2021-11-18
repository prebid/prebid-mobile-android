package org.prebid.mobile.prebidkotlindemo.ads.inappmopub

import android.annotation.SuppressLint
import android.app.Activity
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import org.prebid.mobile.rendering.bidding.display.MoPubInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat

object InAppMoPubVideoInterstitial {

    @SuppressLint("StaticFieldLeak")
    private var moPubInterstitial: MoPubInterstitial? = null
    private var adUnit: MoPubInterstitialAdUnit? = null

    fun create(activity: Activity, adUnitId: String, configId: String) {
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
        adUnit = MoPubInterstitialAdUnit(activity, configId, AdUnitFormat.VIDEO)

        MoPub.initializeSdk(activity, SdkConfiguration.Builder(adUnitId).build()) {
            adUnit?.fetchDemand(moPubInterstitial) {
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