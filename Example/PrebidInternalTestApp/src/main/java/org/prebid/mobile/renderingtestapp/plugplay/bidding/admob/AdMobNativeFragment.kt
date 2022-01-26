package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.*
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import kotlinx.android.synthetic.main.events_admob_rewarded.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.*
import org.prebid.mobile.admob.PrebidNativeAdapter
import org.prebid.mobile.rendering.bidding.display.MediationNativeAdUnit
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.ViewNativeAdBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

class AdMobNativeFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobNative"
    }

    protected var extras: Bundle? = null
    protected var nativeAd: NativeAd? = null
    protected var adUnit: MediationNativeAdUnit? = null
    protected var adLoader: AdLoader? = null

    override val layoutRes = R.layout.fragment_admob_rewarded

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad?.setOnClickListener {
            resetAdEvents()
            it.isEnabled = false
            loadAd()
        }
    }

    override fun initAd(): Any? {
        val hostUrl = PrebidRenderingSettings.getBidServerHost().hostUrl
        val accountId = PrebidRenderingSettings.getAccountId()

        val host = Host.CUSTOM
        host.hostUrl = hostUrl
        PrebidMobile.setPrebidServerHost(host)
        PrebidMobile.setPrebidServerAccountId(accountId)
        PrebidMobile.setApplicationContext(requireContext())

        val nativeAdOptions = NativeAdOptions
            .Builder()
            .build()
        adLoader = AdLoader
            .Builder(requireContext(), adUnitId)
            .forNativeAd { ad: NativeAd ->
                btnAdLoaded.isEnabled = true
                nativeAd = ad
                createCustomView(viewContainer, nativeAd!!)
            }
            .withAdListener(object : AdListener() {
                override fun onAdFailedToLoad(adError: LoadAdError) {
                    btnAdFailed.isEnabled = true
                    Log.e(TAG, "Error: ${adError.message}")
                }
            })
            .withNativeAdOptions(nativeAdOptions)
            .build()

        extras = Bundle()
        return adUnit
    }

    override fun loadAd() {
        val adRequest = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidNativeAdapter::class.java, extras!!)
            .build()

        val nativeAdUnit = NativeAdUnit(configId)
        configureNativeAdUnit(nativeAdUnit)
        nativeAdUnit.fetchDemand(extras!!) { resultCode ->
            Log.d(TAG, "Fetch demand result: $resultCode")

            /** For mediation use loadAd() not loadAds() */
            adLoader?.loadAd(adRequest)
        }
    }

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

    override fun onDestroyView() {
        super.onDestroyView()
        adUnit?.destroy()
        nativeAd?.destroy()
    }

    private fun resetAdEvents() {
        btnAdLoaded.isEnabled = false
        btnAdFailed.isEnabled = false
    }

    private fun createCustomView(wrapper: ViewGroup, nativeAd: NativeAd) {
        wrapper.removeAllViews()
        val binding = ViewNativeAdBinding.inflate(LayoutInflater.from(wrapper.context))

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

        val icon = NativeImageAsset()
        icon.imageType = NativeImageAsset.IMAGE_TYPE.ICON
        icon.wMin = 20
        icon.hMin = 20
        icon.isRequired = true
        nativeAdUnit.addAsset(icon)

        val image = NativeImageAsset()
        image.imageType = NativeImageAsset.IMAGE_TYPE.MAIN
        image.hMin = 200
        image.wMin = 200
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