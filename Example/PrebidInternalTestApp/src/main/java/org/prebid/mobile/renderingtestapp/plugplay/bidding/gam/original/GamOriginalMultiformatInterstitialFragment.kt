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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.os.Bundle
import android.view.View
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.AdSize
import org.prebid.mobile.BannerParameters
import org.prebid.mobile.VideoParameters
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.original.PrebidAdUnit
import org.prebid.mobile.api.original.PrebidRequest
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment

open class GamOriginalMultiformatInterstitialFragment : BaseBidInterstitialFragment() {

    companion object {
        private const val CONFIG_ID_BANNER = "prebid-ita-display-interstitial-320-480"
        private const val CONFIG_ID_VIDEO = "prebid-ita-video-interstitial-320-480-original-api"
    }

    private var prebidAdUnit: PrebidAdUnit? = null
    private var displayAdCallback: (() -> Unit)? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        binding.btnLoad.setOnClickListener {
            handleOriginalInterstitialClick()
        }
    }

    override fun initInterstitialAd(
        adUnitFormat: AdUnitFormat,
        adUnitId: String?,
        configId: String?,
        width: Int,
        height: Int
    ) {
        createAd()
    }

    private fun createAd() {
        prebidAdUnit = PrebidAdUnit(getCurrentConfigId())

        val gamRequest = AdManagerAdRequest.Builder().build()
        prebidAdUnit?.fetchDemand(gamRequest, createPrebidRequest()) {
            AdManagerInterstitialAd.load(
                requireContext(),
                adUnitId,
                gamRequest,
                createAdLoadCallback()
            )
        }
    }

    protected open fun getCurrentConfigId(): String {
        return listOf(CONFIG_ID_BANNER, CONFIG_ID_VIDEO).random()
    }

    protected open fun createPrebidRequest(): PrebidRequest {
        val bannerParameters = BannerParameters().apply {
            interstitialMinWidthPercentage = 80
            interstitialMinHeightPercentage = 80
        }

        val videoParameters = VideoParameters(listOf("video/mp4")).apply {
            adSize = AdSize(320, 480)
        }

        val prebidRequest = PrebidRequest()
        prebidRequest.setInterstitial(true)
        prebidRequest.setBannerParameters(bannerParameters)
        prebidRequest.setVideoParameters(videoParameters)
        return prebidRequest
    }

    private fun handleOriginalInterstitialClick() {
        when (binding.btnLoad.text) {
            getString(R.string.text_load) -> {
                binding.btnLoad.isEnabled = false
                resetEventButtons()
                createAd()
            }

            getString(R.string.text_show) -> {
                binding.btnLoad.text = getString(R.string.text_load)
                displayAdCallback?.invoke()
            }
        }
    }

    private fun createAdLoadCallback(): AdManagerInterstitialAdLoadCallback {
        return object : AdManagerInterstitialAdLoadCallback() {
            override fun onAdLoaded(adManagerInterstitialAd: AdManagerInterstitialAd) {
                super.onAdLoaded(adManagerInterstitialAd)
                events.loaded(true)
                displayAdCallback = {
                    adManagerInterstitialAd.show(requireActivity())
                }
                binding.btnLoad.setText(R.string.text_show)
                binding.btnLoad.isEnabled = true
            }

            override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                super.onAdFailedToLoad(loadAdError)
                events.failed(true)
                binding.btnLoad.isEnabled = true
            }
        }
    }

}