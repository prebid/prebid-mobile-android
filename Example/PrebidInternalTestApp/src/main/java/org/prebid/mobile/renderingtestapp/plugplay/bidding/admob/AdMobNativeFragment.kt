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
import kotlinx.android.synthetic.main.events_admob_native.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.admob.PrebidNativeAdapter
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
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

    override val layoutRes = R.layout.fragment_admob_native

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
        configureOriginalPrebid()

        val nativeAdOptions = NativeAdOptions
            .Builder()
            .build()
        adLoader = AdLoader
            .Builder(requireContext(), adUnitId)
            .forNativeAd { ad: NativeAd ->
                btnAdLoaded?.isEnabled = true
                btnLoad?.isEnabled = true
                nativeAd = ad
                viewContainer?.let {
                    createCustomView(it, nativeAd!!)
                }
            }
            .withAdListener(object : AdListener() {

                override fun onAdImpression() {
                    btnAdShowed?.isEnabled = true
                }

                override fun onAdOpened() {
                    btnAdOpened?.isEnabled = true
                }

                override fun onAdClicked() {
                    btnAdClicked?.isEnabled = true
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    btnAdFailed?.isEnabled = true
                    btnLoad?.isEnabled = true
                    Log.e(TAG, "Error: ${adError.message}")
                }

            })
            .withNativeAdOptions(nativeAdOptions)
            .build()

        extras = Bundle()
        adUnit = MediationNativeAdUnit(configId, extras!!)
        configureNativeAdUnit(adUnit!!)
        return adUnit
    }

    override fun loadAd() {
        val adRequest = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidNativeAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { resultCode ->
            Log.d(TAG, "Fetch demand result: $resultCode")

            /** For mediation use loadAd() not loadAds() */
            adLoader?.loadAd(adRequest)
        }
    }

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER

    override fun onDestroyView() {
        super.onDestroyView()
        nativeAd?.destroy()
    }

    private fun resetAdEvents() {
        btnAdLoaded?.isEnabled = false
        btnAdFailed?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdShowed?.isEnabled = false
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