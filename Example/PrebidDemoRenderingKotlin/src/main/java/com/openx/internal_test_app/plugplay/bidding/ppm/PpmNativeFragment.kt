package com.openx.internal_test_app.plugplay.bidding.ppm

import android.os.Bundle
import android.util.Log
import android.view.View
import com.openx.apollo.bidding.data.FetchDemandResult
import com.openx.apollo.bidding.data.ntv.NativeAd
import com.openx.apollo.bidding.display.NativeAdUnit
import com.openx.apollo.bidding.listeners.NativeAdListener
import com.openx.apollo.models.ntv.NativeEventTracker
import com.openx.apollo.models.openrtb.bidRequests.assets.NativeAssetData
import com.openx.apollo.utils.ntv.NativeUtils
import com.openx.internal_test_app.AdFragment
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment
import com.openx.internal_test_app.utils.SourcePicker
import com.openx.internal_test_app.utils.loadImage
import kotlinx.android.synthetic.main.fragment_native.*
import kotlinx.android.synthetic.main.fragment_native.view.*
import kotlinx.android.synthetic.main.lyt_native_ad.*
import kotlinx.android.synthetic.main.lyt_native_ad_events.*
import kotlinx.android.synthetic.main.lyt_native_ppm_events.*

open class PpmNativeFragment : AdFragment(), NativeAdListener {
    private val TAG = PpmNativeFragment::class.java.simpleName

    override val layoutRes: Int = R.layout.fragment_native
    protected var nativeAdUnit: NativeAdUnit? = null
    protected var nativeAd: NativeAd? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(true)
        }

        if (layoutRes == R.layout.fragment_native) {
            layoutInflater.inflate(getEventButtonViewId(), contentFragmentNative, true)
        }
    }

    override fun initAd(): Any? {
        nativeAdUnit = NativeAdUnit(context, configId, getNativeAdConfig()!!)
        return nativeAdUnit
    }

    override fun loadAd() {
        nativeAdUnit?.fetchDemand {

            if (it.fetchDemandResult != FetchDemandResult.SUCCESS) {
                btnFetchDemandResultFailure?.isEnabled = true
                return@fetchDemand
            }

            btnFetchDemandResultSuccess?.isEnabled = true

            NativeUtils.findNativeAd(it) { nativeAd ->
                if (nativeAd == null) {
                    btnGetNativeAdResultFailure?.isEnabled = true
                    return@findNativeAd
                }
                btnGetNativeAdResultSuccess?.isEnabled = true

                inflateViewContent(nativeAd)
            }
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? = null

    override fun onDestroyView() {
        super.onDestroyView()
        nativeAdUnit?.destroy()
        nativeAd?.destroy()
        if (!SourcePicker.useMockServer) {
            SourcePicker.enableQaEndpoint(false)
        }
    }

    override fun onAdClicked(nativeAd: NativeAd?) {
        Log.d(TAG, "onAdClicked() called with: nativeAd = $nativeAd")
        btnAdClicked?.isEnabled = true
    }

    override fun onAdEvent(nativeAd: NativeAd?, eventType: NativeEventTracker.EventType?) {
        Log.d(TAG, "onAdEvent() called with: nativeAd = $nativeAd, eventType = $eventType")
        eventType ?: return

        when (eventType) {
            NativeEventTracker.EventType.IMPRESSION -> btnAdEventImpression?.isEnabled = true
            NativeEventTracker.EventType.VIEWABLE_MRC50 -> btnAdEventMrc50?.isEnabled = true
            NativeEventTracker.EventType.VIEWABLE_MRC100 -> btnAdEventMrc100?.isEnabled = true
            NativeEventTracker.EventType.VIEWABLE_VIDEO50 -> btnAdEventVideo50?.isEnabled = true
            else -> Log.d(TAG, "onAdEvent: event ignored $eventType")
        }
    }

    protected open fun getEventButtonViewId(): Int = R.layout.lyt_native_ppm_events

    protected open fun inflateViewContent(nativeAd: NativeAd?) {
        nativeAd ?: return

        this.nativeAd = nativeAd

        nativeAd.setNativeAdListener(this)

        nativeAd.registerView(adContainer, btnNativeAction)

        tvNativeTitle?.text = nativeAd.title
        tvNativeBody?.text = nativeAd.text
        tvNativeBrand?.text = nativeAd.getNativeAdDataList(NativeAssetData.DataType.SPONSORED).firstOrNull()?.value
        btnNativeAction?.text = nativeAd.callToAction

        loadImage(ivNativeMain, nativeAd.imageUrl)
        loadImage(ivNativeIcon, nativeAd.iconUrl)
    }
}