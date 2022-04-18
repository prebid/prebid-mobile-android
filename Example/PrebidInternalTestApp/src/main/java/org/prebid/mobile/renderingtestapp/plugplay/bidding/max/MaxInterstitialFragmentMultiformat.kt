package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import com.applovin.mediation.adapters.prebid.utils.MaxInterstitialMediationUtils
import com.applovin.mediation.ads.MaxInterstitialAd
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import java.util.*

class MaxInterstitialFragmentMultiformat : MaxInterstitialFragment() {

    override fun initAd(): Any? {
        maxInterstitialAd = MaxInterstitialAd(adUnitId, activity)
        maxInterstitialAd?.setListener(createListener())

        val mediationUtils = MaxInterstitialMediationUtils(
            maxInterstitialAd
        )
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO),
            mediationUtils
        )
        return adUnit
    }

}