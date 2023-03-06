package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdLoader
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.nativead.NativeAd
import com.google.android.gms.ads.nativead.NativeAdOptions
import org.prebid.mobile.admob.PrebidNativeAdapter
import org.prebid.mobile.api.mediation.MediationNativeAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentAdmobNativeBinding
import org.prebid.mobile.renderingtestapp.databinding.ViewNativeAdBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.utils.CommandLineArgumentParser

class AdMobNativeFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobNative"
    }

    protected var extras: Bundle? = null
    protected var nativeAd: NativeAd? = null
    protected var adUnit: MediationNativeAdUnit? = null
    protected var adLoader: AdLoader? = null

    private val binding: FragmentAdmobNativeBinding
        get() = getBinding()
    private lateinit var events: Events

    override val layoutRes = R.layout.fragment_admob_native

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
        configureOriginalPrebid()

        val nativeAdOptions = NativeAdOptions
            .Builder()
            .build()
        adLoader = AdLoader
            .Builder(requireContext(), adUnitId)
            .forNativeAd { ad: NativeAd ->
                events.loaded(true)
                binding.btnLoad.isEnabled = true
                nativeAd = ad
                binding.viewContainer.let {
                    createCustomView(it, nativeAd!!)
                }
            }
            .withAdListener(object : AdListener() {

                override fun onAdImpression() {
                    events.showed(true)
                }

                override fun onAdOpened() {
                    events.opened(true)
                }

                override fun onAdClicked() {
                    events.clicked(true)
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    events.failed(true)
                    binding.btnLoad.isEnabled = true
                    Log.e(TAG, "Error: ${adError.message}")
                }

            })
            .withNativeAdOptions(nativeAdOptions)
            .build()

        extras = Bundle()
        adUnit = MediationNativeAdUnit(configId, extras!!)
        configureNativeAdUnit(adUnit!!)
        adUnit?.let { CommandLineArgumentParser.addAdUnitSpecificData(it) }
        return adUnit
    }

    override fun loadAd() {
        val adRequest = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidNativeAdapter::class.java, extras!!)
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
        events.loaded(false)
        events.failed(false)
        events.clicked(false)
        events.showed(false)
    }

    private fun createCustomView(wrapper: ViewGroup, nativeAd: NativeAd) {
        wrapper.removeAllViews()
        val binding = ViewNativeAdBinding.inflate(LayoutInflater.from(wrapper.context))

        binding.apply {
            tvHeadline.text = nativeAd.headline
            tvBody.text = nativeAd.body
            imgIco.setImageDrawable(nativeAd.icon?.drawable)
            if (nativeAd.images.size > 0) {
                imgMedia.mediaContent = nativeAd.mediaContent
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

    private class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun opened(b: Boolean) = enable(R.id.btnAdOpened, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

        fun showed(b: Boolean) = enable(R.id.btnAdShowed, b)

    }

}