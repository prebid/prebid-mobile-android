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
import android.util.Log
import android.view.View
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import org.prebid.mobile.AdUnit
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment
import java.util.*

open class GamOriginalInterstitialFragment : BaseBidInterstitialFragment() {
    companion object {
        private const val TAG = "GamOriginalInterstitial"
    }

    private var adUnit: AdUnit? = null
    private var displayAdCallback: (() -> Unit)? = null

    open fun createAdUnit(adUnitFormat: AdUnitFormat): AdUnit {
        val formats = if (adUnitFormat == AdUnitFormat.VIDEO) {
            EnumSet.of(AdUnitFormat.VIDEO)
        } else {
            EnumSet.of(AdUnitFormat.BANNER)
        }
        return InterstitialAdUnit(configId, formats).apply {
            this.setMinSizePercentage(30, 30)
        }
    }

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
        adUnit = createAdUnit(adUnitFormat)
        createAd()
    }

    private fun createAd() {
        val request = AdManagerAdRequest.Builder().build()
        if (adUnit is InterstitialAdUnit) {
            adUnit?.setAutoRefreshInterval(refreshDelay)
        }
        adUnit?.fetchDemand(request) {
            val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(adManagerInterstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(adManagerInterstitialAd)
                    Log.d(TAG, "onAdLoaded() called")
                    events.loaded(true)
                    displayAdCallback = {
                        adManagerInterstitialAd.show(requireActivity())
                    }
                    binding.btnLoad.setText(R.string.text_show)
                    binding.btnLoad.isEnabled = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.d(TAG, "onAdFailed() called with: exception = [$loadAdError]")
                    events.failed(true)
                    binding.btnLoad.isEnabled = true
                }
            }

            AdManagerInterstitialAd.load(
                requireContext(),
                adUnitId,
                request,
                adLoadCallback
            )
        }
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

}