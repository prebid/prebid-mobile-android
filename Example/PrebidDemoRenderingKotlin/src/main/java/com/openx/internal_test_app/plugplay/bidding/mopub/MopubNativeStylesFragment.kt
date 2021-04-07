package com.openx.internal_test_app.plugplay.bidding.mopub

import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment

open class MopubNativeStylesFragment : MopubBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        bannerAdUnit?.setNativeAdConfiguration(getNativeAdConfig())
        return bannerAdUnit
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null
}