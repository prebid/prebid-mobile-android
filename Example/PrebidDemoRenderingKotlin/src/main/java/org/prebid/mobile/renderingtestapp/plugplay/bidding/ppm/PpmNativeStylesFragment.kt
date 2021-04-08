package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import org.prebid.mobile.rendering.utils.helpers.Utils
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.SourcePicker

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