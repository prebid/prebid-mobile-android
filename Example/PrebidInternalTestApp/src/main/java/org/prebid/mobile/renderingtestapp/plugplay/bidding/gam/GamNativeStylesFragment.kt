package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class GamNativeStylesFragment : GamBannerFragment() {
    override fun initAd() {
        super.initAd()
        bannerView?.setNativeAdConfiguration(getNativeAdConfig())
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

}