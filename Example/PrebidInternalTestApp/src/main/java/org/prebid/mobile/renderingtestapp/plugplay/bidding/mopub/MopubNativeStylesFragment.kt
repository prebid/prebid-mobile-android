package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class MopubNativeStylesFragment : MopubBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        bannerAdUnit?.setNativeAdConfiguration(getNativeAdConfig())
        return bannerAdUnit
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null
}