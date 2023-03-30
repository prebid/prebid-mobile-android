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

import org.prebid.mobile.AdSize
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.rendering.InterstitialAdUnit
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBidInterstitialFragment
import java.util.*

open class PpmInterstitialMultiformatFragment : BaseBidInterstitialFragment() {

    override fun initInterstitialAd(
        adUnitFormat: AdUnitFormat, adUnitId: String?,
        configId: String?, width: Int, height: Int
    ) {
        val context = requireContext()
        interstitialAdUnit =
            InterstitialAdUnit(
                requireContext(),
                listOf(
                    context.getString(R.string.imp_prebid_id_interstitial_320_480),
                    context.getString(R.string.imp_prebid_id_video_interstitial_320_480)
                ).shuffled().first(),
                EnumSet.of(AdUnitFormat.DISPLAY, AdUnitFormat.VIDEO)
            )
        interstitialAdUnit?.setInterstitialAdUnitListener(this)
        interstitialAdUnit?.setMinSizePercentage(AdSize(30, 30))
    }
}