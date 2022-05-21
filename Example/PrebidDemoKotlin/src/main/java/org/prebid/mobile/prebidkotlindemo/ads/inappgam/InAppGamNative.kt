package org.prebid.mobile.prebidkotlindemo.ads.inappgam

import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import org.prebid.mobile.*
import org.prebid.mobile.NativeEventTracker.EVENT_TRACKING_METHOD
import org.prebid.mobile.addendum.AdViewUtils
import java.lang.Exception
import java.util.ArrayList
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.utils.DownloadImageTask
import org.prebid.mobile.rendering.bidding.data.bid.Prebid

object InAppGamNative {
    private const val TAG: String = "InAppGamNative"

    private var adView: AdManagerAdView? = null
    private var unifiedNativeAd: NativeAd? = null
    private var adUnit: NativeAdUnit? = null
    private var adLoader: AdLoader? = null

    fun create(
        wrapper: ViewGroup,
        adUnitId: String,
        configId: String?,
        customFormatId: String,
        storedAuctionResponse: String
    ) {
        adUnit = NativeAdUnit(configId!!)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        configureNativeAdUnit(adUnit!!)
        val adRequest = AdManagerAdRequest.Builder().build()
        adLoader = createAdLoader(wrapper, adUnitId, customFormatId)
        adUnit!!.fetchDemand(adRequest) {
            adLoader!!.loadAd(adRequest)
        }
    }

    fun destroy() {
        if (adView != null) {
            adView!!.destroy()
            adView = null
        }
        if (unifiedNativeAd != null) {
            unifiedNativeAd!!.destroy()
            unifiedNativeAd = null
        }
        if (adUnit != null) {
            adUnit!!.stopAutoRefresh()
            adUnit = null
        }
        adLoader = null
    }

    private fun inflatePrebidNativeAd(
        ad: PrebidNativeAd,
        wrapper: ViewGroup
    ) {
        val nativeContainer = View.inflate(wrapper.context, R.layout.layout_native, null)
        ad.registerView(nativeContainer, object : PrebidNativeAdEventListener {
            override fun onAdClicked() {}

            override fun onAdImpression() {}

            override fun onAdExpired() {}
        })
        val icon = nativeContainer.findViewById<ImageView>(R.id.imgIcon)
        loadImage(icon, ad.iconUrl)
        val title = nativeContainer.findViewById<TextView>(R.id.tvTitle)
        title.text = ad.title
        val image = nativeContainer.findViewById<ImageView>(R.id.imgImage)
        loadImage(image, ad.imageUrl)
        val description = nativeContainer.findViewById<TextView>(R.id.tvDesc)
        description.text = ad.description
        val cta = nativeContainer.findViewById<Button>(R.id.btnCta)
        cta.text = ad.callToAction
        wrapper.addView(nativeContainer)
    }

    private fun createAdLoader(
        wrapper: ViewGroup,
        adUnitId: String,
        customFormatId: String
    ): AdLoader? {
        val onGamAdLoaded = OnAdManagerAdViewLoadedListener { adManagerAdView: AdManagerAdView? ->
            Log.d(TAG, "Gam loaded")
            adView = adManagerAdView
            wrapper.addView(adManagerAdView)
        }
        val onUnifiedAdLoaded =
            NativeAd.OnNativeAdLoadedListener { unifiedNativeAd: NativeAd ->
                Log.d(TAG, "Unified native loaded")
                InAppGamNative.unifiedNativeAd = unifiedNativeAd
            }
        val onCustomAdLoaded =
            NativeCustomFormatAd.OnCustomFormatAdLoadedListener { nativeCustomTemplateAd: NativeCustomFormatAd? ->
                Log.d(TAG, "Custom ad loaded")
                AdViewUtils.findNative(
                    nativeCustomTemplateAd!!,
                    object : PrebidNativeAdListener {
                        override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {
                            inflatePrebidNativeAd(ad, wrapper)
                        }

                        override fun onPrebidNativeNotFound() {
                            Log.e(TAG, "onPrebidNativeNotFound")
                            // inflate nativeCustomTemplateAd
                        }

                        override fun onPrebidNativeNotValid() {
                            Log.e(TAG, "onPrebidNativeNotFound")
                            // show your own content
                        }
                    })
            }
        return AdLoader.Builder(wrapper.context, adUnitId)
            .forAdManagerAdView(onGamAdLoaded, AdSize.BANNER)
            .forNativeAd(onUnifiedAdLoaded)
            .forCustomFormatAd(customFormatId, onCustomAdLoaded,
                { _: NativeCustomFormatAd?, _: String? -> })
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)
                    Toast.makeText(wrapper.context, "DFP onAdFailedToLoad", Toast.LENGTH_SHORT)
                        .show()
                }
            })
            .build()
    }

    private fun configureNativeAdUnit(adUnit: NativeAdUnit) {
        adUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        adUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        adUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)
        val methods = ArrayList<EVENT_TRACKING_METHOD>()
        methods.add(EVENT_TRACKING_METHOD.IMAGE)
        methods.add(EVENT_TRACKING_METHOD.JS)
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

    private fun loadImage(
        image: ImageView,
        url: String
    ) {
        DownloadImageTask(image).execute(url)
    }
}