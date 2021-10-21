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

import com.google.android.gms.ads.AdLoader
import org.prebid.mobile.rendering.bidding.display.NativeAdUnit
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseFeedFragment
import org.prebid.mobile.renderingtestapp.utils.adapters.BaseFeedAdapter
import org.prebid.mobile.renderingtestapp.utils.adapters.NativeGamFeedAdapter


class GamNativeFeedFragment : BaseFeedFragment() {
    override fun initFeedAdapter(): BaseFeedAdapter {
        val nativeAdUnit = NativeAdUnit(context, configId, getNativeAdConfig()!!)
        val customFormatId = arguments?.getString(GamNativeFragment.ARG_CUSTOM_FORMAT_ID, "") ?: ""
        val adLoader = createCustomFormatAdLoader(customFormatId)

        return NativeGamFeedAdapter(requireContext(), nativeAdUnit, adLoader)
    }

    private fun createCustomFormatAdLoader(customFormatId: String) = AdLoader.Builder(requireContext(), adUnitId)
            .forCustomFormatAd(customFormatId, { formatAd ->
                (getAdapter() as? NativeGamFeedAdapter)?.handleCustomFormatAd(formatAd)
            }, null)
            .build()
}