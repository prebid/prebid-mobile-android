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

import android.util.Log
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.BannerView
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.api.rendering.listeners.BannerViewListener
import org.prebid.mobile.api.rendering.listeners.InterstitialAdUnitListener
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentInterstitialHtmlWithBannersBinding
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBannersWithInterstitialFragment
import org.prebid.mobile.renderingtestapp.utils.getAdDescription

open class PpmBannersWithInterstitialFragment : BaseBannersWithInterstitialFragment() {

    protected lateinit var interstitialAdUnit: InterstitialAdUnit
    protected lateinit var bannerViewTop: BannerView
    protected lateinit var bannerViewBottom: BannerView

    private val binding: FragmentInterstitialHtmlWithBannersBinding
        get() = getBinding()

    override fun loadInterstitial() {
        binding.tvInterstitialAdUnitDescription.text = "Interstitial Config ID: $interstitialConfigId"
        interstitialAdUnit = initInterstitialAdUnit(interstitialConfigId)
        interstitialAdUnit.loadAd()
    }

    override fun loadBanners() {
        bannerViewTop =
            initBannerView(bannerConfigId, REFRESH_BANNER_TOP_SEC, binding.btnTopBannerAdShown)
        bannerViewBottom =
            initBannerView(bannerConfigId, REFRESH_BANNER_BOTTOM_SEC, binding.btnBottomBannerAdShown)

        bannerViewTop.loadAd()
        bannerViewBottom.loadAd()

        binding.viewContainerTop.addView(bannerViewTop)
        binding.viewContainerBottom.addView(bannerViewBottom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        interstitialAdUnit.destroy()
        bannerViewBottom.destroy()
        bannerViewTop.destroy()
    }

    open fun initBannerView(configId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): BannerView {
        val bannerView = BannerView(
            requireContext(),
            configId,
            AdSize(BANNER_WIDTH, BANNER_HEIGHT)
        )
        bannerView.setAutoRefreshDelay(refreshIntervalSec)
        bannerView.setBannerListener(getBannerAdListener(configId, refreshIntervalSec, impressionCounterButton))
        return bannerView
    }

    open fun initInterstitialAdUnit(configId: String): InterstitialAdUnit {
        val interstitialAdUnit =
            InterstitialAdUnit(requireContext(), configId)
        interstitialAdUnit.setMinSizePercentage(AdSize(30, 30))
        interstitialAdUnit.setInterstitialAdUnitListener(getInterstitialAdListener())
        return interstitialAdUnit
    }

    protected fun getBannerAdListener(configId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): BannerViewListener {
        return object : BannerViewListener {
            private var impressionCount: Int = 0

            init {
                impressionCounterButton?.text = configId.getAdDescription(refreshIntervalSec, impressionCount)
            }

            override fun onAdLoaded(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdLoaded()")
                impressionCount++
                impressionCounterButton?.text = configId.getAdDescription(refreshIntervalSec, impressionCount)
            }

            override fun onAdDisplayed(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdDisplayed()")
            }

            override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
                Log.d(TAG, "Banner: onAdFailed()")
            }

            override fun onAdClicked(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdClicked()")
            }

            override fun onAdClosed(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdClosed()")
            }
        }
    }

    protected fun getInterstitialAdListener(): InterstitialAdUnitListener {
        return object : InterstitialAdUnitListener {
            override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdLoaded()")
                binding.btnLoad.isEnabled = true
                binding.btnLoad.text = getString(R.string.text_show)
                binding.btnLoad.setOnClickListener { interstitialAdUnit?.show() }
            }

            override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdDisplayed()")
            }

            override fun onAdFailed(interstitialAdUnit: InterstitialAdUnit?, exception: AdException?) {
                Log.d(TAG, "Interstitial: onAdFailed()")
            }

            override fun onAdClicked(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdClicked()")
            }

            override fun onAdClosed(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdClosed()")
                interstitialAdUnit?.loadAd()
            }

        }
    }
}