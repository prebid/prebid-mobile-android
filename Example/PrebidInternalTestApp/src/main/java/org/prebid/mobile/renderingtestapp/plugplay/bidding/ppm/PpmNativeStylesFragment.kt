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

import org.prebid.mobile.rendering.utils.helpers.Utils
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.SourcePicker

open class PpmNativeStylesFragment : PpmBannerFragment() {
    override fun initAd(): Any? {
        super.initAd()
        SourcePicker.enableQaEndpoint(true)

        val nativeAdConfig = getNativeAdConfig()
        val nativeStylesCreative = when {
            getTitle().contains("No Creative", ignoreCase = true) -> null
            getTitle().contains("KEYS") -> Utils.loadStringFromFile(resources, R.raw.native_styles_creative_keys)
            else -> Utils.loadStringFromFile(resources, R.raw.native_styles_creative)
        }
        nativeAdConfig?.nativeStylesCreative = nativeStylesCreative

        bannerView?.setNativeAdConfiguration(nativeAdConfig)
        return bannerView
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        SourcePicker.enableQaEndpoint(false)
    }
}