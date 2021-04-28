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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam

import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.listeners.BannerViewListener
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class GamBannerFragment : AdFragment(), BannerViewListener {
    private val TAG = GamBannerFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_banner

    protected var bannerView: BannerView? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad?.setOnClickListener {
            resetEventButtons()
            loadAd()
        }
    }

    override fun initAd(): Any? {
        val eventHandler = GamBannerEventHandler(requireContext(), adUnitId, *getGamAdSizeArray(AdSize(width, height)))
        bannerView = initBanner(
                configId,
                eventHandler)
        bannerView?.addAdditionalSizes(*getAdditionalPrebidBannerSizeArray())
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

    override fun onDestroyView() {
        super.onDestroyView()
        bannerView?.destroy()
    }

    override fun onAdFailed(view: BannerView?, exception: AdException?) {
        Log.d(TAG, "onAdFailed() called with: view = [$view], throwable = [$exception]")
        resetEventButtons()
        btnAdFailed?.isEnabled = true
        btnLoad?.isEnabled = true
    }

    override fun onAdDisplayed(bannerView: BannerView?) {
        btnAdDisplayed?.isEnabled = true
        Log.d(TAG, "onAdDisplayed() called with: bannerView = [$bannerView]")
    }

    override fun onAdLoaded(bannerView: BannerView?) {
        Log.d(TAG, "onAdLoaded() called with: view = [$view]")
        resetEventButtons()
        btnAdLoaded?.isEnabled = true
        btnLoad?.isEnabled = true
    }

    override fun onAdClicked(view: BannerView?) {
        Log.d(TAG, "onAdClicked() called with: view = [$view]")
        btnAdClicked?.isEnabled = true
    }

    override fun onAdClosed(view: BannerView?) {
        Log.d(TAG, "onAdClosed() called with: view = [$view]")
        btnAdClosed?.isEnabled = true
    }

    protected open fun initBanner(configId: String?, eventHandler: GamBannerEventHandler): BannerView {
        return BannerView(requireContext(),
                configId,
                eventHandler)
    }

    protected open fun getGamAdSizeArray(initialSize: AdSize) = arrayOf(initialSize)

    protected open fun getAdditionalPrebidBannerSizeArray() = emptyArray<AdSize>()
}