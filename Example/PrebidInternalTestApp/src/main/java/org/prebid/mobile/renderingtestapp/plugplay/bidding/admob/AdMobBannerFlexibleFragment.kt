package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.AdSize
import org.prebid.mobile.admob.AdMobMediationBannerUtils
import org.prebid.mobile.admob.PrebidBannerAdapter
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import com.google.android.gms.ads.AdSize as GamAdSize


class AdMobBannerFlexibleFragment : AdMobBannerFragment() {

    companion object {
        private const val TAG = "FlexibleAdMobBanner"
    }

    override fun initAd(): Any? {
        MobileAds.initialize(requireContext()) {
            Log.d("MobileAds", "Initialization complete.")
        }

        bannerView = AdView(requireActivity())
        bannerView?.adSize = GamAdSize.getLandscapeInlineAdaptiveBannerAdSize(requireContext(), GamAdSize.FULL_WIDTH)
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = getListener()
        viewContainer.addView(bannerView)

        adRequestExtras = Bundle()
        adRequest = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidBannerAdapter::class.java, adRequestExtras!!)
            .build()
        val mediationUtils =
            AdMobMediationBannerUtils(adRequestExtras, bannerView)


        adUnit = MediationBannerAdUnit(
            requireContext(),
            configId,
            AdSize(width, height),
            mediationUtils
        )
        adUnit?.addAdditionalSizes(AdSize(728, 90))
        adUnit?.setRefreshInterval(refreshDelay)
        return adUnit
    }

}