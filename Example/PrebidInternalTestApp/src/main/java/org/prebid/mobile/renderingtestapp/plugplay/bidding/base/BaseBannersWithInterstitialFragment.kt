/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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