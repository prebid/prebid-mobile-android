package org.prebid.mobile.renderingtestapp.plugplay.bidding.base

import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

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
        bannerConfigId = getString(R.string.mock_config_id_banner_320x50)
        interstitialConfigId = getString(R.string.mock_config_id_interstitial_320_480)
    }

    override fun loadAd() {
        loadBanners()
        loadInterstitial()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null
}