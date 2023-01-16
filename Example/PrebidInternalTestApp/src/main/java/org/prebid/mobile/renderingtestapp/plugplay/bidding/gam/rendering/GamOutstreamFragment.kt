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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.rendering

import android.os.Bundle
import android.util.Log
import android.view.View
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.data.VideoPlacementType
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.eventhandlers.GamBannerEventHandler
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingBannerVideoBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents

class GamOutstreamFragment : AdFragment(), BannerViewListener {

    private val TAG = GamOutstreamFragment::class.java.simpleName

    override val layoutRes: Int = R.layout.fragment_bidding_banner_video

    protected var bannerView: BannerView? = null
    protected lateinit var events: Events
    protected val binding: FragmentBiddingBannerVideoBinding
        get() = getBinding()


    fun initBanner(configId: String?, eventHandler: GamBannerEventHandler): BannerView {
        val bannerView = BannerView(requireContext(), configId, eventHandler)
        bannerView.videoPlacementType = VideoPlacementType.IN_BANNER
        return bannerView
    }

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        events = Events(view)
        binding.adIdLabel.text = getString(R.string.label_auid, configId)
        binding.btnLoad.setOnClickListener {
            resetEventButtons()
            loadAd()
        }
    }

    override fun initAd(): Any? {
        val eventHandler = GamBannerEventHandler(requireContext(), adUnitId, *getGamAdSizeArray(AdSize(width, height)))
        bannerView = initBanner(
            configId,
            eventHandler
        )
        bannerView?.addAdditionalSizes(*getAdditionalPrebidBannerSizeArray())
        bannerView?.setAutoRefreshDelay(refreshDelay)
        bannerView?.setBannerListener(this)
        binding.viewContainer.addView(bannerView)
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
        events.failed(true)
        binding.btnLoad.isEnabled = true
    }

    override fun onAdDisplayed(bannerView: BannerView?) {
        events.displayed(true)
        Log.d(TAG, "onAdDisplayed() called with: bannerView = [$bannerView]")
    }

    override fun onAdLoaded(bannerView: BannerView?) {
        Log.d(TAG, "onAdLoaded() called with: view = [$view]")
        resetEventButtons()
        events.loaded(true)
        binding.btnLoad.isEnabled = true
    }

    override fun onAdClicked(view: BannerView?) {
        Log.d(TAG, "onAdClicked() called with: view = [$view]")
        events.clicked(true)
    }

    override fun onAdClosed(view: BannerView?) {
        Log.d(TAG, "onAdClosed() called with: view = [$view]")
        events.closed(true)
    }

    protected open fun getGamAdSizeArray(initialSize: AdSize) = arrayOf(initialSize)

    protected open fun getAdditionalPrebidBannerSizeArray() = emptyArray<AdSize>()

    protected class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun impression(b: Boolean) = enable(R.id.btnAdImpression, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun closed(b: Boolean) = enable(R.id.btnAdClosed, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)
        fun displayed(b: Boolean) = enable(R.id.btnAdDisplayed, b)

    }

}