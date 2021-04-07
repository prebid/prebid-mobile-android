package com.openx.internal_test_app.plugplay.bidding.base

import com.openx.internal_test_app.AdFragment
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment

abstract class BaseBannersWithInterstitialFragment : AdFragment() {
    protected val TAG = this::class.java.simpleName
    protected val REFRESH_BANNER_TOP_SEC = 15
    protected val REFRESH_BANNER_BOTTOM_SEC = 60
    protected val BANNER_WIDTH = 320
    protected val BANNER_HEIGHT = 50

    override val layoutRes = R.layout.fragment_interstitial_html_with_banners
    protected lateinit var bannerConfigId: String
    protected lateinit var interstitialConfigId: String

    abstract fun loadInterstitial()

    abstract fun loadBanners()

    override fun initAd() {
        bannerConfigId = getString(R.string.mock_banner_320x50_config_id)
        interstitialConfigId = getString(R.string.mock_interstitial_320_480_config_id)
    }

    override fun loadAd() {
        loadBanners()
        loadInterstitial()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null
}