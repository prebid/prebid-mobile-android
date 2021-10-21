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

import android.widget.Button
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.lyt_native_ad_links.*
import org.prebid.mobile.rendering.bidding.data.ntv.NativeAd
import org.prebid.mobile.rendering.models.openrtb.bidRequests.assets.NativeAssetData
import org.prebid.mobile.renderingtestapp.R

class PpmNativeLinksFragment : PpmNativeFragment() {
    override val layoutRes: Int = R.layout.fragment_native_links

    override fun inflateViewContent(nativeAd: NativeAd?) {
        if (nativeAd == null) {
            btnAdFailed?.isEnabled = true
            return
        }

        btnAdDisplayed?.isEnabled = true
        this.nativeAd = nativeAd

        nativeAd.setNativeAdListener(this)

        nativeAd.registerView(adContainer, btnNativeLinkRoot)

        btnNativeLinkRoot.text = nativeAd.callToAction
        setupButton(nativeAd, btnNativeDeeplinkFallback, NativeAssetData.DataType.SPONSORED)
        setupButton(nativeAd, btnNativeDeeplinkOk, NativeAssetData.DataType.DESC)
        setupButton(nativeAd, btnNativeLinkUrl, NativeAssetData.DataType.RATING)
    }

    private fun setupButton(nativeAd: NativeAd?, button: Button, dataType: NativeAssetData.DataType) {
        val nativeData = nativeAd?.getNativeAdDataList(dataType)?.firstOrNull()
        if (nativeData != null) {
            button.text = nativeData.value
            nativeAd.registerClickView(button, nativeData)
        }
    }
}