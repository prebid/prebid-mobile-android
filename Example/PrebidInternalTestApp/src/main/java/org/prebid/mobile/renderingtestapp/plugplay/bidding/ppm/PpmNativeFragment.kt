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

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import kotlinx.android.synthetic.main.fragment_native.*
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_in_app_events.*
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.PrebidNativeAdEventListener
import org.prebid.mobile.api.data.FetchDemandResult
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.loadImage

open class PpmNativeFragment : AdFragment() {

    private val TAG = PpmNativeFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_native
    protected var nativeAdUnit: MediationNativeAdUnit? = null
    protected var extras = Bundle()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (layoutRes == R.layout.fragment_native) {
            layoutInflater.inflate(getEventButtonViewId(), contentFragmentNative, true)
        }
    }

    override fun initAd(): Any? {
        configureOriginalPrebid()

        nativeAdUnit = MediationNativeAdUnit(configId, extras)
        configureNativeAdUnit(nativeAdUnit!!)
        return nativeAdUnit
    }

    override fun loadAd() {
        nativeAdUnit?.fetchDemand {
            if (it != FetchDemandResult.SUCCESS) {
                btnFetchDemandResultFailure?.isEnabled = true
                return@fetchDemand
            }
            btnFetchDemandResultSuccess?.isEnabled = true

            val nativeAd = NativeAdProvider.getNativeAd(extras)
            if (nativeAd == null) {
                btnGetNativeAdResultFailure?.isEnabled = true
                return@fetchDemand
            }

            btnGetNativeAdResultSuccess?.isEnabled = true
            inflateViewContent(nativeAd)
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        nativeAdUnit?.destroy()
    }

    protected open fun getEventButtonViewId(): Int = R.layout.lyt_native_in_app_events

    protected open fun inflateViewContent(nativeAd: PrebidNativeAd) {
        nativeAd.registerViewList(
            adContainer,
            listOf(
                tvNativeTitle,
                tvNativeBody,
                tvNativeBrand,
                btnNativeAction,
                ivNativeMain,
                ivNativeIcon
            ),
            createNativeListener()
        )

        tvNativeTitle?.text = nativeAd.title
        tvNativeBody?.text = nativeAd.description
        tvNativeBrand?.text = nativeAd.sponsoredBy
        btnNativeAction?.text = nativeAd.callToAction

        if (nativeAd.imageUrl.isNotBlank()) {
            loadImage(ivNativeMain, nativeAd.imageUrl)
        }
        if (nativeAd.iconUrl.isNotBlank()) {
            loadImage(ivNativeIcon, nativeAd.iconUrl)
        }
    }

    protected fun createNativeListener(): PrebidNativeAdEventListener {
        return object : PrebidNativeAdEventListener {
            override fun onAdClicked() {
                btnAdClicked?.isEnabled = true
            }

            override fun onAdImpression() {
                doInMainThread {
                    btnAdImpression?.isEnabled = true
                }
            }

            override fun onAdExpired() {
                btnAdExpired?.isEnabled = true
            }
        }
    }

    private fun doInMainThread(function: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.postAtFrontOfQueue(function)
    }

}