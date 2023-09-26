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
import android.view.View
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
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
import java.lang.ref.WeakReference

open class GamOriginalMultiformatBannerVideoNativeStylesFragment : AdFragment() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-multiformat-native-styles"
        const val CONFIG_ID_BANNER = "prebid-ita-banner-320-50"
        const val CONFIG_ID_NATIVE = "prebid-ita-banner-native-styles"
        const val CONFIG_ID_VIDEO = "prebid-ita-video-outstream-original-api"
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
        val gamView = AdManagerAdView(requireContext())
        gamView.adUnitId = AD_UNIT_ID
        gamView.setAdSizes(AdSize.FLUID, AdSize.BANNER, AdSize.MEDIUM_RECTANGLE)
        gamView.loadAd(gamRequest)
        gamView.adListener = AdListenerWithToast(events, gamView) {
            binding.btnLoad.isEnabled = true
        }
        binding.viewContainer.addView(gamView)

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
        adView: AdManagerAdView,
        private val onAdLoadingFinished: () -> Unit,
    ) : AdListener() {

        private val weakAdView = WeakReference(adView)

        override fun onAdLoaded() {
            super.onAdLoaded()
            events.loaded(true)
            onAdLoadingFinished()
            AdViewUtils.findPrebidCreativeSize(weakAdView.get(), object : AdViewUtils.PbFindSizeListener {
                override fun success(width: Int, height: Int) {
                    weakAdView.get()?.setAdSizes(AdSize(width, height))
                }

                override fun failure(error: PbFindSizeError) {}
            })
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