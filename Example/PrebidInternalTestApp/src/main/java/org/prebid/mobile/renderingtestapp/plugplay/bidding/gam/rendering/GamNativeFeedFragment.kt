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
import com.google.android.gms.ads.AdLoader
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseFeedFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter
import org.prebid.mobile.renderingtestapp.utils.adapters.NativeGamFeedAdapter


class GamNativeFeedFragment : BaseFeedFragment() {

    override fun initAd() {
        super.initAd()
        configureOriginalPrebid()
    }

    override fun initFeedAdapter(): BaseFeedAdapter {
        val extras = Bundle()
        val nativeAdUnit = MediationNativeAdUnit(configId, extras)
        configureNativeAdUnit(nativeAdUnit)
        val customFormatId = arguments?.getString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "") ?: ""
        val adLoader = createCustomFormatAdLoader(customFormatId)

        return NativeGamFeedAdapter(requireContext(), nativeAdUnit, adLoader, extras)
    }

    private fun createCustomFormatAdLoader(customFormatId: String) = AdLoader
        .Builder(requireContext(), adUnitId)
        .forCustomFormatAd(
            customFormatId,
            { formatAd ->
                (getAdapter() as? NativeGamFeedAdapter)?.handleCustomFormatAd(formatAd)
            },
            null
        )
        .build()

}