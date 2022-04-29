package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import com.applovin.mediation.adapters.prebid.utils.MaxMediationInterstitialUtils
import com.applovin.mediation.ads.MaxInterstitialAd
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import java.util.*

class MaxInterstitialFragmentMultiformat : MaxInterstitialFragment() {

    override fun initAd(): Any? {
        maxInterstitialAd = MaxInterstitialAd(adUnitId, activity)
        maxInterstitialAd?.setListener(createListener())

        val mediationUtils =
            MaxMediationInterstitialUtils(
                maxInterstitialAd
            )
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO),
            mediationUtils
        )
        adUnit?.setMinSizePercentage(30, 30)
        return adUnit
    }

}