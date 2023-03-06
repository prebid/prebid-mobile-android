package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdError
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.prebid.mobile.admob.AdMobMediationInterstitialUtils
import org.prebid.mobile.admob.PrebidInterstitialAdapter
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentAdmobRewardedBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.utils.CommandLineArgumentParser
import java.util.*

open class AdMobInterstitialFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobInterstitial"
        const val ARG_IS_VIDEO = "${TAG}_isVideo"
    }

    protected var extras: Bundle? = null
    protected var interstitialAd: InterstitialAd? = null
    protected var adUnit: MediationInterstitialAdUnit? = null
    protected var isVideo = false

    protected val binding: FragmentAdmobRewardedBinding
        get() = getBinding()
    protected lateinit var events: Events

    override val layoutRes = R.layout.fragment_admob_rewarded

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        events = Events(view)

        binding.adIdLabel.text = getString(R.string.label_auid, configId)
        binding.btnLoad.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        extras = Bundle()
        val mediationUtils = AdMobMediationInterstitialUtils(extras)

        isVideo = arguments?.getBoolean(ARG_IS_VIDEO) ?: false
        var adUnitFormat = EnumSet.of(AdUnitFormat.DISPLAY)
        if (isVideo) adUnitFormat = EnumSet.of(AdUnitFormat.VIDEO)
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            adUnitFormat,
            mediationUtils
        )
        adUnit?.let { CommandLineArgumentParser.addAdUnitSpecificData(it) }
        if (!isVideo) {
            adUnit?.setMinSizePercentage(30, 30)
        }
        return adUnit
    }

    override fun loadAd() {
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidInterstitialAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            InterstitialAd.load(requireContext(), adUnitId, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    events.loaded(true)
                    binding.btnLoad.isEnabled = true
                    binding.btnLoad.text = getString(R.string.text_show)

                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = createFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    events.failed(true)
                    Log.e(TAG, adError.message)
                    interstitialAd = null
                }
            })
        }

    }

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    override fun onDestroy() {
        super.onDestroy()

        interstitialAd = null

        adUnit?.destroy()
        adUnit = null
    }

    private fun resetAdEvents() {
        events.loaded(false)
        events.showed(false)
        events.impression(false)
        events.dismissed(false)
        events.failedFullScreen(false)
        events.failed(false)
        events.clicked(false)
    }

    private fun handleLoadButtonClick() {
        val loadButton = binding.btnLoad ?: return
        if (loadButton.text == getString(R.string.text_show)) {
            interstitialAd?.show(requireActivity())
            loadButton.text = getString(R.string.text_retry)
        } else if (loadButton.text == getString(R.string.text_retry)) {
            resetAdEvents()
            loadButton.isEnabled = false
            loadButton.text = "Loading..."
            loadAd()
        }
    }

    protected fun createFullScreenContentCallback(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdClicked() {
                events.clicked(true)
            }

            override fun onAdImpression() {
                events.impression(true)
            }

            override fun onAdShowedFullScreenContent() {
                events.showed(true)
            }

            override fun onAdDismissedFullScreenContent() {
                events.dismissed(true)
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                events.failedFullScreen(true)
            }
        }
    }

    protected class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun impression(b: Boolean) = enable(R.id.btnAdImpression, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)

        fun showed(b: Boolean) = enable(R.id.btnAdShowed, b)
        fun dismissed(b: Boolean) = enable(R.id.btnAdDismissed, b)
        fun failedFullScreen(b: Boolean) = enable(R.id.btnAdFailedFullScreen, b)

    }

}