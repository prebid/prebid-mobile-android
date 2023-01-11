package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
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
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView
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

    override val layoutRes = R.layout.fragment_admob_rewarded

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        findView<TextView>(R.id.adIdLabel)?.text = getString(R.string.label_auid, configId)
        findView<Button>(R.id.btnLoad)?.setOnClickListener {
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
                    findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
                    findView<Button>(R.id.btnLoad)?.isEnabled = true
                    findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_show)

                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = createFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = true
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
        findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdShowed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdImpression)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDismissed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdFailedFullScreen)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        val loadButton = findView<Button>(R.id.btnLoad) ?: return
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
                findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
            }

            override fun onAdImpression() {
                findView<EventCounterView>(R.id.btnAdImpression)?.isEnabled = true
            }

            override fun onAdShowedFullScreenContent() {
                findView<EventCounterView>(R.id.btnAdShowed)?.isEnabled = true
            }

            override fun onAdDismissedFullScreenContent() {
                findView<EventCounterView>(R.id.btnAdDismissed)?.isEnabled = true
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                findView<EventCounterView>(R.id.btnAdFailedFullScreen)?.isEnabled = true
            }
        }
    }


}