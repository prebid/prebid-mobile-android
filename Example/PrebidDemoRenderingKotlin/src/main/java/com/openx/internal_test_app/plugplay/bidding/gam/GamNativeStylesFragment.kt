package com.openx.internal_test_app.plugplay.bidding.gam

import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment

open class GamNativeStylesFragment : GamBannerFragment() {
    override fun initAd() {
        super.initAd()
        bannerView?.setNativeAdConfiguration(getNativeAdConfig())
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

}