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

import android.view.View
import android.widget.Button
import androidx.annotation.IdRes
import org.prebid.mobile.NativeData
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

class PpmNativeLinksFragment : PpmNativeFragment() {

    override val layoutRes: Int = R.layout.fragment_native_links

    override fun inflateViewContent(nativeAd: PrebidNativeAd) {
        findView<EventCounterView>(R.id.btnAdDisplayed).isEnabled = true

        nativeAd.registerViewList(
            findView(R.id.adContainer),
            listOf(
                findView(R.id.btnNativeLinkRoot),
                findView(R.id.btnNativeDeeplinkOk),
                findView(R.id.btnNativeDeeplinkFallback),
                findView(R.id.btnNativeLinkUrl)
            ),
            createNativeListener()
        )

        findView<Button>(R.id.btnNativeLinkRoot).text = nativeAd.callToAction
        setupButton(nativeAd, findView(R.id.btnNativeDeeplinkFallback), NativeData.Type.SPONSORED_BY)
        setupButton(nativeAd, findView(R.id.btnNativeDeeplinkOk), NativeData.Type.DESCRIPTION)
        setupButton(nativeAd, findView(R.id.btnNativeLinkUrl), NativeData.Type.RATING)
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

    private fun <T : View> findView(@IdRes idRes: Int): T {
        return binding.root.findViewById(idRes)
    }

}