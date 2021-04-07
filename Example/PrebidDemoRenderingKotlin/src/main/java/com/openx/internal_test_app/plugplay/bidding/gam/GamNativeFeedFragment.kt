package com.openx.internal_test_app.plugplay.bidding.gam

import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.openx.apollo.bidding.display.NativeAdUnit
import com.openx.internal_test_app.plugplay.bidding.base.BaseFeedFragment
import com.openx.internal_test_app.utils.adapters.BaseFeedAdapter
import com.openx.internal_test_app.utils.adapters.NativeGamFeedAdapter


class GamNativeFeedFragment : BaseFeedFragment() {
    override fun initFeedAdapter(): BaseFeedAdapter {
        val nativeAdUnit = NativeAdUnit(context, configId, getNativeAdConfig()!!)
        val customTemplateId = arguments?.getString(GamNativeFragment.ARG_CUSTOM_TEMPLATE_ID, "")
        val adLoader = createCustomTemplateAdLoader(customTemplateId)

        return NativeGamFeedAdapter(requireContext(), nativeAdUnit, adLoader)
    }

    private fun createCustomTemplateAdLoader(customTemplateId: String?) = AdLoader.Builder(requireContext(), adUnitId)
            .forCustomTemplateAd(customTemplateId, { customTemplate ->
                (getAdapter() as? NativeGamFeedAdapter)?.handleCustomTemplateAd(customTemplate)
            }, null)
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(p0: Int) {
                }
            })
            .build()
}