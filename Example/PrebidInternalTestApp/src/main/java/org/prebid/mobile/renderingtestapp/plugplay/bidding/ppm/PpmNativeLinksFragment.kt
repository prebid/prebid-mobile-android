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
import org.prebid.mobile.NativeData
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.renderingtestapp.R

class PpmNativeLinksFragment : PpmNativeFragment() {

    override val layoutRes: Int = R.layout.fragment_native_links

    override fun inflateViewContent(nativeAd: PrebidNativeAd) {
        btnAdDisplayed?.isEnabled = true

        nativeAd.registerViewList(
            adContainer,
            listOf(
                btnNativeLinkRoot,
                btnNativeDeeplinkOk,
                btnNativeDeeplinkFallback,
                btnNativeLinkUrl
            ),
            createNativeListener()
        )

        btnNativeLinkRoot.text = nativeAd.callToAction
        setupButton(nativeAd, btnNativeDeeplinkFallback, NativeData.Type.SPONSORED_BY)
        setupButton(nativeAd, btnNativeDeeplinkOk, NativeData.Type.DESCRIPTION)
        setupButton(nativeAd, btnNativeLinkUrl, NativeData.Type.RATING)
    }

    private fun setupButton(
        nativeAd: PrebidNativeAd,
        button: Button,
        dataType: NativeData.Type
    ) {
        val nativeData = nativeAd.dataList.find { it.type == dataType }
        if (nativeData != null) {
            button.text = nativeData.value
        }
    }

}