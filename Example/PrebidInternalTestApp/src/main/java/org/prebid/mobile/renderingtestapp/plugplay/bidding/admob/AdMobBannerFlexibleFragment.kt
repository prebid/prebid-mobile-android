package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.widget.RelativeLayout
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds
import org.prebid.mobile.AdSize
import org.prebid.mobile.admob.AdMobMediationBannerUtils
import org.prebid.mobile.admob.PrebidBannerAdapter
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.renderingtestapp.R
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
        bannerView?.setAdSize(GamAdSize.getLandscapeInlineAdaptiveBannerAdSize(requireContext(), GamAdSize.FULL_WIDTH))
        bannerView?.adUnitId = adUnitId
        bannerView?.adListener = getListener()
        binding.viewContainer.addView(bannerView)

        adRequestExtras = Bundle()
        adRequest = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidBannerAdapter::class.java, adRequestExtras!!)
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