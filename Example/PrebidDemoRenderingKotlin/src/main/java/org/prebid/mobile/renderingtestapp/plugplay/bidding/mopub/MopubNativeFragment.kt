package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.nativeads.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import kotlinx.android.synthetic.main.lyt_native_mopub_events.*
import org.prebid.mobile.rendering.bidding.data.FetchDemandResult
import org.prebid.mobile.rendering.bidding.display.MoPubNativeAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.SourcePicker

open class MopubNativeFragment : AdFragment() {
    override val layoutRes: Int = R.layout.fragment_native_mopub

    protected var mopubNative: MoPubNative? = null
    protected var mopubNativeAdUnit: MoPubNativeAdUnit? = null
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
            btnNativeAdFailed?.isEnabled = true
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

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(true)
        }
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
        mopubNative?.registerAdRenderer(ApolloNativeAdRenderer(viewBinder))
        mopubNative?.registerAdRenderer(MoPubStaticNativeAdRenderer(viewBinder))
        mopubNativeAdUnit = MoPubNativeAdUnit(requireContext(), configId, getNativeAdConfig())
        return mopubNativeAdUnit
    }

    override fun loadAd() {
        MoPub.initializeSdk(requireContext(), SdkConfiguration.Builder(adUnitId).build()) {
            val keywordsContainer = HashMap<String, String>()
            mopubNativeAdUnit?.fetchDemand(keywordsContainer, mopubNative!!) {
                if (it == FetchDemandResult.SUCCESS) {
                    btnFetchDemandResultSuccess?.isEnabled = true
                }
                else {
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
        mopubNativeAdUnit?.destroy()
        nativeAd?.destroy()
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(false)
        }
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
}