package org.prebid.mobile.prebidkotlindemo.ads.inappadmob

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import org.prebid.mobile.*
import org.prebid.mobile.admob.PrebidNativeAdapter
import org.prebid.mobile.prebidkotlindemo.databinding.ViewNativeAdAdMobBinding


object InAppAdMobNative {

    const val TAG = "InAppAdMobNative"

    private var nativeAd: NativeAd? = null

    fun create(
        wrapper: ViewGroup,
        adUnitId: String,
        configId: String,
        storedAuctionResponse: String
    ) {
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        val nativeAdOptions = NativeAdOptions
            .Builder()
            .build()
        val adLoader = AdLoader
            .Builder(wrapper.context, adUnitId)
            .forNativeAd { ad: NativeAd ->
                nativeAd = ad
                createCustomView(wrapper, nativeAd!!)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e(TAG, "Error: ${adError.message}")
                }
            })
            .withNativeAdOptions(nativeAdOptions)
            .build()

        val extras = Bundle()
        val adRequest = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidNativeAdapter::class.java, extras)
            .build()

        val nativeAdUnit = NativeAdUnit(configId)
        configureNativeAdUnit(nativeAdUnit)
        nativeAdUnit.fetchDemand(extras) { resultCode ->
            Log.d(TAG, "Fetch demand result: $resultCode")

            /** For mediation use loadAd() not loadAds() */
            adLoader.loadAd(adRequest)
        }

    }

    fun destroy() {
        nativeAd?.destroy()
        nativeAd = null
    }

    private fun createCustomView(wrapper: ViewGroup, nativeAd: NativeAd) {
        wrapper.removeAllViews()
        val binding = ViewNativeAdAdMobBinding.inflate(LayoutInflater.from(wrapper.context))

        binding.apply {
            tvHeadline.text = nativeAd.headline
            tvBody.text = nativeAd.body
            imgIco.setImageDrawable(nativeAd.icon?.drawable)
            if (nativeAd.images.size > 0) {
                val image = nativeAd.images[0]
                val mediaContent = PrebidNativeAdMediaContent(image)
                imgMedia.setMediaContent(mediaContent)
            }
        }

        binding.viewNativeWrapper.apply {
            headlineView = binding.tvHeadline
            bodyView = binding.tvBody
            iconView = binding.imgIco
            mediaView = binding.imgMedia
            setNativeAd(nativeAd)
        }

        wrapper.addView(binding.root)
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

    class PrebidNativeAdMediaContent(private val image: NativeAd.Image) : MediaContent {
        override fun getAspectRatio(): Float {
            return 320f / 250
        }

        override fun getDuration(): Float {
            return 0f
        }

        override fun getCurrentTime(): Float {
            return 0f
        }

        override fun getVideoController(): VideoController {
            return VideoController()
        }

        override fun hasVideoContent(): Boolean {
            return false
        }

        override fun setMainImage(drawable: Drawable?) {}
        override fun getMainImage(): Drawable? {
            return image.drawable
        }
    }

}