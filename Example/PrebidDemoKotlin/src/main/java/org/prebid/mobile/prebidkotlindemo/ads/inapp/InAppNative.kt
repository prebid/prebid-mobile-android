package org.prebid.mobile.prebidkotlindemo.ads.inapp

import android.app.Activity
import android.os.Bundle
import org.prebid.mobile.*
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import android.content.Intent
import android.net.Uri

import android.widget.TextView

import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView

import android.widget.LinearLayout

import org.prebid.mobile.PrebidNativeAd
import org.prebid.mobile.prebidkotlindemo.utils.DownloadImageTask
import org.prebid.mobile.rendering.utils.ntv.NativeAdProvider


object InAppNative {
    private var nativeAdUnit: MediationNativeAdUnit? = null

    fun create(configId: String, wrapper:ViewGroup,activity: Activity,storedAuctionResponse: String) {
        val extras = Bundle()
        nativeAdUnit = MediationNativeAdUnit(configId, extras)
        PrebidMobile.setStoredAuctionResponse(storedAuctionResponse)
        configureNativeAdUnit(nativeAdUnit!!)
        nativeAdUnit?.fetchDemand {
            inflatePrebidNativeAd(NativeAdProvider.getNativeAd(extras)!!,activity,wrapper)
        }
    }

    fun destroy() {
        nativeAdUnit?.destroy()
        nativeAdUnit = null
        PrebidMobile.setStoredAuctionResponse(null)
    }

    private fun inflatePrebidNativeAd(ad: PrebidNativeAd,activity: Activity,wrapper:ViewGroup) {
        val nativeContainer = LinearLayout(activity)
        nativeContainer.orientation = LinearLayout.VERTICAL
        val iconAndTitle = LinearLayout(activity)
        iconAndTitle.orientation = LinearLayout.HORIZONTAL
        val icon = ImageView(activity)
        icon.layoutParams = LinearLayout.LayoutParams(160, 160)
        icon.loadImage(ad.iconUrl)
        iconAndTitle.addView(icon)
        val title = TextView(activity)
        title.textSize = 20f
        title.text = ad.title
        iconAndTitle.addView(title)
        nativeContainer.addView(iconAndTitle)
        val image = ImageView(activity)
        image.layoutParams = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        image.loadImage(ad.imageUrl)
        nativeContainer.addView(image)
        val description = TextView(activity)
        description.textSize = 18f
        description.text = ad.description
        nativeContainer.addView(description)
        val cta = Button(activity)
        cta.text = ad.callToAction
        cta.setOnClickListener {
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://openx.com"))
            activity.startActivity(browserIntent)
        }
        nativeContainer.addView(cta)
        wrapper.addView(nativeContainer)
    }

    private fun ImageView.loadImage(imageUrl:String){
        DownloadImageTask(this).execute(imageUrl)
    }

    private fun configureNativeAdUnit(nativeAdUnit: MediationNativeAdUnit) {
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
}