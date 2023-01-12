package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.RelativeLayout
import android.widget.TextView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import org.prebid.mobile.*
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingNativeApplovinMaxBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class MaxNativeFragment : AdFragment() {

    companion object {
        private val TAG = MaxNativeFragment::class.simpleName
    }

    private lateinit var nativeAdLoader: MaxNativeAdLoader
    private lateinit var nativeAdUnit: NativeAdUnit

    override val layoutRes = R.layout.fragment_bidding_native_applovin_max

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

    private val binding: FragmentBiddingNativeApplovinMaxBinding
        get() = getBinding()
    private lateinit var events: Events

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        events = Events(view)
        binding.adIdLabel.text = getString(R.string.label_auid, configId)
        binding.btnLoad.setOnClickListener {
            resetAdEvents()
            it.isEnabled = false
            loadAd()
        }
    }

    override fun initAd(): Any? {
        nativeAdLoader = MaxNativeAdLoader(adUnitId, requireActivity())
        nativeAdLoader.setNativeAdListener(createNativeAdListener(binding.viewContainer!!))
        nativeAdLoader.setRevenueListener(createRevenueListener())

        nativeAdUnit = NativeAdUnit(configId)
        configureNativeAdUnit(nativeAdUnit)
        return nativeAdUnit
    }

    override fun loadAd() {
        nativeAdUnit.fetchDemand(nativeAdLoader) {
            nativeAdLoader.loadAd(createNativeAdView(requireActivity()))
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        nativeAdLoader.destroy()
    }

    private fun createNativeAdView(activity: Activity): MaxNativeAdView {
        val binder = MaxNativeAdViewBinder.Builder(R.layout.view_native_ad_max)
            .setTitleTextViewId(R.id.tvHeadline)
            .setBodyTextViewId(R.id.tvBody)
            .setIconImageViewId(R.id.imgIco)
            .setMediaContentViewGroupId(R.id.frameMedia)
            .setCallToActionButtonId(R.id.btnCallToAction)
            .build()
        val maxNativeAdView = MaxNativeAdView(binder, activity)
        maxNativeAdView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        return maxNativeAdView
    }


    private fun configureNativeAdUnit(nativeAdUnit: NativeAdUnit) {
        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        val methods: ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> = ArrayList()
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            nativeAdUnit.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }

        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        nativeAdUnit.addAsset(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        nativeAdUnit.addAsset(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        nativeAdUnit.addAsset(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        nativeAdUnit.addAsset(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        nativeAdUnit.addAsset(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        nativeAdUnit.addAsset(cta)
    }

    private fun resetAdEvents() {
        events.nativeAdLoaded(false)
        events.nativeAdClicked(false)
        events.nativeAdLoadFailed(false)
        events.revenuePaid(false)
    }

    private fun createNativeAdListener(wrapper: ViewGroup): MaxNativeAdListener {
        return object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAd: MaxAd?) {
                wrapper.removeAllViews()
                wrapper.addView(nativeAdView)

                events.nativeAdLoaded(true)
                binding.btnLoad.isEnabled = true
            }

            override fun onNativeAdClicked(p0: MaxAd?) {
                events.nativeAdClicked(true)
            }

            override fun onNativeAdLoadFailed(p0: String?, p1: MaxError?) {
                events.nativeAdLoadFailed(true)
                binding.btnLoad.isEnabled = true

                Log.e(TAG, "On native ad load failed: ${p1?.message}")
            }
        }
    }

    private fun createRevenueListener(): MaxAdRevenueListener {
        return MaxAdRevenueListener {
            events.revenuePaid(true)
        }
    }

    protected class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun impression(b: Boolean) = enable(R.id.btnAdImpression, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

        fun displayed(b: Boolean) = enable(R.id.btnAdDisplayed, b)
        fun nativeAdLoaded(b: Boolean) = enable(R.id.btnNativeAdLoaded, b)
        fun nativeAdClicked(b: Boolean) = enable(R.id.btnNativeAdClicked, b)
        fun nativeAdLoadFailed(b: Boolean) = enable(R.id.btnNativeAdLoadFailed, b)
        fun revenuePaid(b: Boolean) = enable(R.id.btnAdRevenuePaid, b)

    }

}