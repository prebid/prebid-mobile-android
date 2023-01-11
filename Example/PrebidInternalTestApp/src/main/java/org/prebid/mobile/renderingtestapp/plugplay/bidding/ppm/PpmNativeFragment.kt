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
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.PrebidNativeAdEventListener
import org.prebid.mobile.api.data.FetchDemandResult
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.loadImage
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class PpmNativeFragment : AdFragment() {

    private val TAG = PpmNativeFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_native
    protected var nativeAdUnit: MediationNativeAdUnit? = null
    protected var extras = Bundle()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (layoutRes == R.layout.fragment_native) {
            layoutInflater.inflate(
                getEventButtonViewId(),
                findView<ConstraintLayout>(R.id.contentFragmentNative)!!,
                true
            )
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
                findView<EventCounterView>(R.id.btnFetchDemandResultFailure)?.isEnabled = true
                return@fetchDemand
            }
            findView<EventCounterView>(R.id.btnFetchDemandResultSuccess)?.isEnabled = true

            val nativeAd = NativeAdProvider.getNativeAd(extras)
            if (nativeAd == null) {
                findView<EventCounterView>(R.id.btnGetNativeAdResultFailure)?.isEnabled = true
                return@fetchDemand
            }

            findView<EventCounterView>(R.id.btnGetNativeAdResultSuccess)?.isEnabled = true
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
            findView(R.id.adContainer),
            listOf(
                findView<View>(R.id.tvNativeTitle),
                findView<View>(R.id.tvNativeBody),
                findView<View>(R.id.tvNativeBrand),
                findView<View>(R.id.btnNativeAction),
                findView<View>(R.id.ivNativeMain),
                findView<View>(R.id.ivNativeIcon)
            ),
            createNativeListener()
        )

        findView<TextView>(R.id.tvNativeTitle)?.text = nativeAd.title
        findView<TextView>(R.id.tvNativeBody)?.text = nativeAd.description
        findView<TextView>(R.id.tvNativeBrand)?.text = nativeAd.sponsoredBy
        findView<Button>(R.id.btnNativeAction)?.text = nativeAd.callToAction

        if (nativeAd.imageUrl.isNotBlank()) {
            loadImage(findView<ImageView>(R.id.ivNativeMain)!!, nativeAd.imageUrl)
        }
        if (nativeAd.iconUrl.isNotBlank()) {
            loadImage(findView<ImageView>(R.id.ivNativeIcon)!!, nativeAd.iconUrl)
        }
    }

    protected fun createNativeListener(): PrebidNativeAdEventListener {
        return object : PrebidNativeAdEventListener {
            override fun onAdClicked() {
                findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
            }

            override fun onAdImpression() {
                doInMainThread {
                    findView<EventCounterView>(R.id.btnAdImpression)?.isEnabled = true
                }
            }

            override fun onAdExpired() {
                findView<EventCounterView>(R.id.btnAdExpired)?.isEnabled = true
            }
        }
    }

    private fun doInMainThread(function: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.postAtFrontOfQueue(function)
    }

}