package org.prebid.mobile.prebidkotlindemo.ads.inappmax

import android.app.Activity
import android.util.Log
import android.view.ViewGroup
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdRevenueListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.nativeAds.MaxNativeAdListener
import com.applovin.mediation.nativeAds.MaxNativeAdLoader
import com.applovin.mediation.nativeAds.MaxNativeAdView
import com.applovin.mediation.nativeAds.MaxNativeAdViewBinder
import org.prebid.mobile.*
import org.prebid.mobile.prebidkotlindemo.R

object InAppMaxNative {

    private val TAG = InAppMaxNative::class.simpleName

    private lateinit var nativeAdLoader: MaxNativeAdLoader

    fun create(
        activity: Activity,
        wrapper: ViewGroup,
        adUnitId: String,
        configId: String
    ) {
        nativeAdLoader = MaxNativeAdLoader(adUnitId, activity)
        nativeAdLoader.setNativeAdListener(createNativeAdListener(wrapper))
        nativeAdLoader.setRevenueListener(createRevenueListener())

        val nativeAdUnit = NativeAdUnit(configId)
        configureNativeAdUnit(nativeAdUnit)
        nativeAdUnit.fetchDemand(nativeAdLoader) {
            nativeAdLoader.loadAd(createNativeAdView(activity))
        }
    }


    fun destroy() {
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
        return MaxNativeAdView(binder, activity)
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

    private fun createNativeAdListener(wrapper: ViewGroup): MaxNativeAdListener {
        return object : MaxNativeAdListener() {
            override fun onNativeAdLoaded(nativeAdView: MaxNativeAdView?, nativeAd: MaxAd?) {
                Log.d(TAG, "Native ad loaded")

                wrapper.removeAllViews()
                wrapper.addView(nativeAdView)
            }

            override fun onNativeAdClicked(p0: MaxAd?) {
                Log.d(TAG, "Clicked on native ad")
            }

            override fun onNativeAdLoadFailed(p0: String?, p1: MaxError?) {
                Log.e(TAG, "Failed to load: $p0")
            }
        }
    }

    private fun createRevenueListener(): MaxAdRevenueListener {
        return MaxAdRevenueListener {
            Log.d(TAG, "On revenue paid")
        }
    }

}