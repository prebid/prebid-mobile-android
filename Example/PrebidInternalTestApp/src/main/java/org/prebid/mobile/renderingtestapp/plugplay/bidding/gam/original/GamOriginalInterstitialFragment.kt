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

import android.util.Log
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.fragment_bidding_interstitial.*
import org.prebid.mobile.AdSize
import org.prebid.mobile.InterstitialAdUnit
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.eventhandlers.GamInterstitialEventHandler
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment
import java.util.*

open class GamOriginalInterstitialFragment : BaseBidInterstitialFragment() {
    var adUnit: InterstitialAdUnit? = null
    private val TAG = GamOriginalBannerFragment::class.simpleName
    override fun initInterstitialAd(adUnitFormat: AdUnitFormat, adUnitId: String?, configId: String?, width: Int, height: Int) {
        val requestBuilder = AdManagerAdRequest.Builder()
        val request = requestBuilder.build()

        adUnit = InterstitialAdUnit(configId!!, 30, 30)
        adUnit?.setAutoRefreshInterval(refreshDelay)
        adUnit?.fetchDemand(request) {
            val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(adManagerInterstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(adManagerInterstitialAd)
                    adManagerInterstitialAd.show(requireActivity())
                    Log.d(TAG, "onAdLoaded() called")
                    btnAdLoaded?.isEnabled = true
                    btnLoad?.setText(R.string.text_show)
                    btnLoad?.isEnabled = true
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Log.d(TAG, "onAdFailed() called with: exception = [$loadAdError]")
                    btnAdFailed?.isEnabled = true
                    btnLoad?.isEnabled = true
                }
            }
            AdManagerInterstitialAd.load(requireContext(), adUnitId!!, request, adLoadCallback)
        }
    }
}