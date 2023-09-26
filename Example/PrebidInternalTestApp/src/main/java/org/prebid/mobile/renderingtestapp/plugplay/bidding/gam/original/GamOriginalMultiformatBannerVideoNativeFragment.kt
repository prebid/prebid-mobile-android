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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.original

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.formats.AdManagerAdViewOptions
import com.google.android.gms.ads.formats.OnAdManagerAdViewLoadedListener
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAd.OnNativeAdLoadedListener
import com.google.android.gms.ads.nativead.NativeCustomFormatAd
import com.google.android.gms.ads.nativead.NativeCustomFormatAd.OnCustomFormatAdLoadedListener
import com.google.common.collect.Lists
import org.prebid.mobile.*
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.api.original.PrebidAdUnit
import org.prebid.mobile.api.original.PrebidRequest
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingMultiformatBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.utils.loadImage

open class GamOriginalMultiformatBannerVideoNativeFragment : AdFragment() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-multiformat"
        const val CONFIG_ID_BANNER = "prebid-ita-banner-320-50"
        const val CONFIG_ID_NATIVE = "prebid-ita-banner-native-styles"
        const val CONFIG_ID_VIDEO = "prebid-ita-video-outstream-original-api"
        const val CUSTOM_FORMAT_ID = "12304464"
    }

    override val layoutRes = R.layout.fragment_bidding_multiformat

    private lateinit var events: Events
    private var prebidAdUnit: PrebidAdUnit? = null
    private val params = arrayOf(Params.BANNER, Params.VIDEO, Params.NATIVE)
    private val binding: FragmentBiddingMultiformatBinding get() = getBinding()


    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        events = Events(view)
        initButtons()
    }

    override fun initAd() = Unit

    override fun loadAd() {}

    private fun load() {
        val configIds = mutableListOf<String>()
        val prebidRequest = PrebidRequest()

        if (Params.BANNER.activated) {
            prebidRequest.setBannerParameters(createBannerParameters())
            configIds.add(CONFIG_ID_BANNER)
        }

        if (Params.VIDEO.activated) {
            prebidRequest.setVideoParameters(createVideoParameters())
            configIds.add(CONFIG_ID_VIDEO)
        }

        if (Params.NATIVE.activated) {
            prebidRequest.setNativeParameters(createNativeParameters())
            configIds.add(CONFIG_ID_NATIVE)
        }

        val configId = configIds.random()
        prebidAdUnit = PrebidAdUnit(configId)

        val gamRequest = AdManagerAdRequest.Builder().build()
        prebidAdUnit?.fetchDemand(gamRequest, prebidRequest) {
            loadGam(gamRequest)
        }
    }

    private fun loadGam(gamRequest: AdManagerAdRequest) {
        val onBannerLoaded = OnAdManagerAdViewLoadedListener { adView ->
            showBannerAd(adView)
        }

        val onNativeLoaded = OnNativeAdLoadedListener { nativeAd ->
            showNativeAd(nativeAd, binding.viewContainer)
        }

        val onCustomAdLoaded = OnCustomFormatAdLoadedListener { customNativeAd ->
            showCustomNativeAd(customNativeAd)
        }

        val adLoader = AdLoader.Builder(requireContext(), AD_UNIT_ID)
            .forAdManagerAdView(onBannerLoaded, AdSize.BANNER, AdSize.MEDIUM_RECTANGLE)
            .forNativeAd(onNativeLoaded)
            .forCustomFormatAd(CUSTOM_FORMAT_ID, onCustomAdLoaded, null)
            .withAdListener(AdListenerWithToast(events, onAdLoadingFinished = { binding.btnLoad.isEnabled = true }))
            .withAdManagerAdViewOptions(AdManagerAdViewOptions.Builder().build())
            .build()

        adLoader.loadAd(gamRequest)
    }


    private fun createBannerParameters(): BannerParameters {
        return BannerParameters()
    }

    private fun createVideoParameters(): VideoParameters {
        return VideoParameters(listOf("video/mp4"))
    }

    private fun createNativeParameters(): NativeParameters {
        val assets = mutableListOf<NativeAsset>()

        val title = NativeTitleAsset()
        title.setLength(90)
        title.isRequired = true
        assets.add(title)

        val icon = NativeImageAsset(20, 20, 20, 20)
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.isRequired = true
        assets.add(icon)

        val image = NativeImageAsset(200, 200, 200, 200)
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.isRequired = true
        assets.add(image)

        val data = NativeDataAsset()
        data.len = 90
        data.dataType = NativeDataAsset.DATA_TYPE.SPONSORED
        data.isRequired = true
        assets.add(data)

        val body = NativeDataAsset()
        body.isRequired = true
        body.dataType = NativeDataAsset.DATA_TYPE.DESC
        assets.add(body)

        val cta = NativeDataAsset()
        cta.isRequired = true
        cta.dataType = NativeDataAsset.DATA_TYPE.CTATEXT
        assets.add(cta)

        val nativeParameters = NativeParameters(assets)
        nativeParameters.addEventTracker(
            NativeEventTracker(
                NativeEventTracker.EVENT_TYPE.IMPRESSION,
                arrayListOf(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE)
            )
        )
        nativeParameters.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC)
        nativeParameters.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED)
        nativeParameters.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL)

        return nativeParameters
    }

    private fun showBannerAd(adView: AdManagerAdView) {
        binding.viewContainer.addView(adView)
        AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
            override fun success(width: Int, height: Int) {
                adView.setAdSizes(AdSize(width, height))
            }

            override fun failure(error: PbFindSizeError) {}
        })
    }

    private fun showNativeAd(ad: NativeAd, wrapper: ViewGroup) {
        val nativeContainer = View.inflate(wrapper.context, R.layout.layout_native, null)

        val icon = nativeContainer.findViewById<ImageView>(R.id.imgIcon)
        val iconUrl = ad.icon?.uri?.toString()
        if (iconUrl != null) {
            loadImage(icon, iconUrl)
        }

        val title = nativeContainer.findViewById<TextView>(R.id.tvTitle)
        title.text = ad.headline

        val image = nativeContainer.findViewById<ImageView>(R.id.imgImage)
        val imageUrl = ad.images.getOrNull(0)?.uri?.toString()
        if (imageUrl != null) {
            loadImage(image, imageUrl)
        }

        val description = nativeContainer.findViewById<TextView>(R.id.tvDesc)
        description.text = ad.body

        val cta = nativeContainer.findViewById<Button>(R.id.btnCta)
        cta.text = ad.callToAction

        wrapper.addView(nativeContainer)
    }

    private fun showCustomNativeAd(customNativeAd: NativeCustomFormatAd) {
        AdViewUtils.findNative(customNativeAd, object : PrebidNativeAdListener {
            override fun onPrebidNativeLoaded(ad: PrebidNativeAd) {
                inflatePrebidNativeAd(ad)
            }

            override fun onPrebidNativeNotFound() {
                Log.e("PrebidAdViewUtils", "Find native failed: native not found")
            }

            override fun onPrebidNativeNotValid() {
                Log.e("PrebidAdViewUtils", "Find native failed: native not valid")
            }
        })
    }

    private fun inflatePrebidNativeAd(ad: PrebidNativeAd) {
        val nativeContainer = View.inflate(requireContext(), R.layout.layout_native, null)

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

        ad.registerView(nativeContainer, Lists.newArrayList(icon, title, image, description, cta), null)

        binding.viewContainer.addView(nativeContainer)
    }

    private fun initButtons() {
        binding.btnLoad.isEnabled = true
        binding.btnLoad.setOnClickListener {
            binding.viewContainer.removeAllViews()
            binding.btnLoad.isEnabled = false
            resetEventButtons()
            load()
        }

        listOf(
            binding.btnBannerParams,
            binding.btnVideoParams,
            binding.btnNativeParams
        ).forEachIndexed { i, toggleButton ->
            toggleButton.isChecked = true
            toggleButton.setOnClickListener {
                val newValue = toggleButton.isChecked
                toggleButton.isChecked = newValue
                params[i].activated = newValue

                val notEnoughActivations = params.count { it.activated } < 2
                if (notEnoughActivations) {
                    binding.btnLoad.isEnabled = false
                } else {
                    binding.btnLoad.isEnabled = true
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        prebidAdUnit?.destroy()
    }

    private class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun impression(b: Boolean) = enable(R.id.btnAdImpression, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

    }

    private enum class Params(var activated: Boolean = true) {

        BANNER, VIDEO, NATIVE

    }

    private class AdListenerWithToast(
        private val events: Events,
        private val onAdLoadingFinished: () -> Unit
    ) : AdListener() {

        override fun onAdLoaded() {
            super.onAdLoaded()
            events.loaded(true)
            onAdLoadingFinished()
        }

        override fun onAdImpression() {
            super.onAdImpression()
            events.impression(true)
        }

        override fun onAdClicked() {
            super.onAdClicked()
            events.clicked(true)
        }

        override fun onAdFailedToLoad(adError: LoadAdError) {
            super.onAdFailedToLoad(adError);
            events.failed(true)
            onAdLoadingFinished()
        }

    }

}