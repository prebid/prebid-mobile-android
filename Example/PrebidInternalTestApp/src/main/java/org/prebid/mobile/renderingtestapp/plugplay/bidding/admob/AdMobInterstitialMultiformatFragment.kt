package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import org.prebid.mobile.admob.AdMobInterstitialMediationUtils
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import java.util.*

open class AdMobInterstitialMultiformatFragment : AdMobInterstitialFragment() {

    override fun initAd(): Any? {
        extras = Bundle()
        val mediationUtils = AdMobInterstitialMediationUtils(extras)
        val adUnitFormats = EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO)
        adUnit = MediationInterstitialAdUnit(activity, configId, adUnitFormats, mediationUtils)
        adUnit?.setMinSizePercentage(30, 30)
        return adUnit
    }

}