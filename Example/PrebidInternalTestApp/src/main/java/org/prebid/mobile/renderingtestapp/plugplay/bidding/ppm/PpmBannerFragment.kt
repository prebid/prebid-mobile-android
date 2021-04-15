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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.ppm

import android.os.Bundle
import android.view.View
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.listeners.BannerViewListener
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class PpmBannerFragment : AdFragment(), BannerViewListener {
    private val TAG = PpmBannerFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_banner

    protected var bannerView: BannerView? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        adIdLabel?.text = getString(R.string.label_auid, configId)
        btnLoad.setOnClickListener {
            resetEventButtons()
            it.isEnabled = false
            loadAd()
        }

        btnStopRefresh?.setOnClickListener {
            bannerView?.stopRefresh()
            resetEventButtons()
            btnLoad?.isEnabled = true
        }
    }

    override fun initAd(): Any? {
        bannerView = BannerView(requireContext(), configId, AdSize(width, height))
        bannerView?.setAutoRefreshDelay(refreshDelay)
        bannerView?.setBannerListener(this)
        viewContainer.addView(bannerView)
        return bannerView
    }

    override fun loadAd() {
        bannerView?.loadAd()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
        resetEventButtons()
        btnAdFailed?.isEnabled = true
        btnLoad?.isEnabled = true
    }

    override fun onAdLoaded(bannerView: BannerView?) {
        resetEventButtons()
        btnAdLoaded?.isEnabled = true
        btnLoad?.isEnabled = true
    }

    override fun onAdClicked(bannerView: BannerView?) {
        btnAdClicked?.isEnabled = true
    }

    override fun onAdClosed(bannerView: BannerView?) {
        btnAdClosed?.isEnabled = true
    }

    override fun onAdDisplayed(bannerView: BannerView?) {
        btnAdDisplayed?.isEnabled = true
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerView?.destroy()
    }
}