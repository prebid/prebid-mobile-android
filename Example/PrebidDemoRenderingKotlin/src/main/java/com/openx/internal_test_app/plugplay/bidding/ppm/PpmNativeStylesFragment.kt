package com.openx.internal_test_app.plugplay.bidding.ppm

import com.openx.apollo.utils.helpers.Utils
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment
import com.openx.internal_test_app.utils.SourcePicker

open class PpmNativeStylesFragment : PpmBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        SourcePicker.enableQaEndpoint(true)

        val nativeAdConfig = getNativeAdConfig()
        val nativeStylesCreative = when {
            getTitle().contains("No Creative", ignoreCase = true) -> null
            getTitle().contains("KEYS") -> Utils.loadStringFromFile(resources, R.raw.native_styles_creative_keys)
            else -> Utils.loadStringFromFile(resources, R.raw.native_styles_creative)
        }
        nativeAdConfig?.nativeStylesCreative = nativeStylesCreative

        bannerView?.setNativeAdConfiguration(nativeAdConfig)
        return bannerView
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        SourcePicker.enableQaEndpoint(false)
    }
}