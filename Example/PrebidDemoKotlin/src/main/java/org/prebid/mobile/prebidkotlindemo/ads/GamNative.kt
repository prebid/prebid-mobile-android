package org.prebid.mobile.prebidkotlindemo.ads

import android.util.Log
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.*
import org.prebid.mobile.NativeEventTracker.EVENT_TRACKING_METHOD
import java.lang.Exception
import java.util.ArrayList

object GamNative {
    private const val TAG: String = "GamNative"

    private var nativeAdUnit: NativeAdUnit? = null

    fun create(
        wrapper: ViewGroup,
        adUnitId: String,
        configId: String?,
        autoRefreshTime: Int,
        storedAuctionResponse: String
    ) {
        nativeAdUnit = NativeAdUnit(configId!!)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        configureNativeAdUnit(nativeAdUnit!!)
        val gamView = AdManagerAdView(wrapper.context)
        gamView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                Log.d(TAG, "On ad loaded")
            }
        }
        gamView.adUnitId = adUnitId
        gamView.setAdSizes(AdSize.FLUID)
        wrapper.removeAllViews()
        wrapper.addView(gamView)
        val builder = AdManagerAdRequest.Builder()
        nativeAdUnit!!.setAutoRefreshInterval(autoRefreshTime)
        nativeAdUnit!!.fetchDemand(builder) {
            val request = builder.build()
            gamView.loadAd(request)
        }
    }

    fun destroy() {
        if (nativeAdUnit != null) {
            nativeAdUnit!!.stopAutoRefresh()
            nativeAdUnit = null
        }
    }


    private fun configureNativeAdUnit(adUnit: NativeAdUnit) {
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
        val methods = ArrayList<EVENT_TRACKING_METHOD>()
        methods.add(EVENT_TRACKING_METHOD.IMAGE)
        try {
            val tracker = NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods)
            adUnit.addEventTracker(tracker)
        } catch (e: Exception) {
            e.printStackTrace()
        }
        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        adUnit.addAsset(title)
        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        adUnit.addAsset(icon)
        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        adUnit.addAsset(image)
        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        adUnit.addAsset(data)
        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        adUnit.addAsset(body)
        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        adUnit.addAsset(cta)
    }
}