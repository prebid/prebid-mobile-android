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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mediation.MoPubNativeMediationUtils
import com.mopub.nativeads.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import kotlinx.android.synthetic.main.lyt_native_mopub_events.*
import org.prebid.mobile.*
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult
import org.prebid.mobile.rendering.bidding.display.MediationNativeAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.SourcePicker

open class MopubNativeFragment : AdFragment() {
    override val layoutRes: Int = R.layout.fragment_native_mopub

    protected var mopubNative: MoPubNative? = null
    protected var mopubNativeAdUnit: MediationNativeAdUnit? = null
    protected var keywordsContainer = HashMap<String, String>()
    protected var requestParametersBuilder: RequestParameters.Builder? = null
    protected lateinit var adapterHelper: AdapterHelper
    protected var nativeAd: NativeAd? = null

    protected open var nativeNetworkListener = object : MoPubNative.MoPubNativeNetworkListener {
        override fun onNativeLoad(nativeAd: NativeAd?) {
            btnLoad?.isEnabled = true
            btnNativeAdLoaded?.isEnabled = true
            nativeAd?.setMoPubNativeEventListener(nativeEventListener)
            this@MopubNativeFragment.nativeAd = nativeAd
            val view = adapterHelper.getAdView(null, viewContainer, nativeAd)
            viewContainer.removeAllViews()
            viewContainer.addView(view)
        }

        override fun onNativeFail(errorCode: NativeErrorCode?) {
            Log.d("MoPubNativeFragment", "Error: $errorCode")
            btnNativeAdFailed?.isEnabled = true
            btnLoad?.isEnabled = true
        }

    }

    protected val nativeEventListener = object : NativeAd.MoPubNativeEventListener {

        override fun onImpression(view: View?) {
            btnAdEventImpression?.isEnabled = true
        }

        override fun onClick(view: View?) {
            btnAdClicked?.isEnabled = true
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return super.onCreateView(inflater, container, savedInstanceState)
    }

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        btnLoad?.setOnClickListener {
            resetEventButtons()
            it.isEnabled = false
            loadAd()
        }
    }

    override fun initAd(): Any? {
        configureOriginalPrebid()

        adapterHelper = AdapterHelper(requireContext(), 0, 3);
        mopubNative = MoPubNative(requireContext(), adUnitId, nativeNetworkListener)
        val viewBinder = ViewBinder.Builder(R.layout.lyt_native_ad)
            .titleId(R.id.tvNativeTitle)
            .textId(R.id.tvNativeBody)
            .sponsoredTextId(R.id.tvNativeBrand)
            .mainImageId(R.id.ivNativeMain)
            .iconImageId(R.id.ivNativeIcon)
            .callToActionId(R.id.btnNativeAction)
            .build()

        mopubNative?.registerAdRenderer(PrebidNativeAdRenderer(viewBinder, createPrebidListener()))
        mopubNative?.registerAdRenderer(MoPubStaticNativeAdRenderer(viewBinder))

        requestParametersBuilder = RequestParameters.Builder()
        val mediationUtils = MoPubNativeMediationUtils(keywordsContainer, mopubNative)
        mopubNativeAdUnit = MediationNativeAdUnit(
            configId,
            mediationUtils
        )
        configureNativeAdUnit()
        return mopubNativeAdUnit
    }

    override fun loadAd() {
        MoPub.initializeSdk(requireContext(), SdkConfiguration.Builder(adUnitId).build()) {
            mopubNativeAdUnit?.fetchDemand {
                if (it == FetchDemandResult.SUCCESS) {
                    Log.d("MoPubNativeFragment", "Fetch demand result: $it")
                    btnFetchDemandResultSuccess?.isEnabled = true
                } else {
                    btnFetchDemandResultFailure?.isEnabled = true
                }
                val requestParameters = RequestParameters.Builder()
                    .keywords(convertMapToMoPubKeywords(keywordsContainer))
                    .build()
                mopubNative?.makeRequest(requestParameters)
            }
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        mopubNative?.destroy()
//        mopubNativeAdUnit?.destroy()
        nativeAd?.destroy()
    }

    private fun convertMapToMoPubKeywords(keywordMap: Map<String, String>): String? {
        val result = StringBuilder()
        for (key in keywordMap.keys) {
            result.append(key).append(":").append(keywordMap[key]).append(",")
        }
        if (result.isNotEmpty()) {
            result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }

    private fun createPrebidListener() = object : PrebidNativeAdEventListener {

        override fun onAdClicked() {
            btnAdClicked?.isEnabled = true
        }

        override fun onAdImpression() {
            doInMainThread {
                btnAdEventImpression?.isEnabled = true
            }
        }

        override fun onAdExpired() {}

    }

    private fun configureNativeAdUnit() {
        mopubNativeAdUnit?.apply {
            setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
            setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
            setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)

            val methods = arrayListOf(
                NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE,
                NativeEventTracker.EVENT_TRACKING_METHOD.JS
            )
            val eventTracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            addEventTracker(eventTracker)

            val assetTitle = NativeTitleAsset()
            assetTitle.setLength(90)
            assetTitle.isRequired = true
            addAsset(assetTitle)

            val assetIcon = NativeImageAsset()
            assetIcon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
            assetIcon.wMin = 20
            assetIcon.hMin = 20
            assetIcon.isRequired = true
            addAsset(assetIcon)

            val image = NativeImageAsset()
            image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
            image.hMin = 200
            image.wMin = 200
            image.isRequired = true
            addAsset(image)

            val data = NativeDataAsset()
            data.len = 90
            data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
            data.isRequired = true
            addAsset(data)

            val body = NativeDataAsset()
            body.isRequired = true
            body.dataType = NativeDataAsset.DATA_TYPE.DESC
            addAsset(body)

            val cta = NativeDataAsset()
            cta.isRequired = true
            cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
            addAsset(cta)
        }
    }

    private fun doInMainThread(function: () -> Unit) {
        val handler = Handler(Looper.getMainLooper())
        handler.postAtFrontOfQueue(function)
    }

}