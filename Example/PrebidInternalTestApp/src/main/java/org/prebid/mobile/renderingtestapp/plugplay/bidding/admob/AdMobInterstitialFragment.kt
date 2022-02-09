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
import kotlinx.android.synthetic.main.events_admob_rewarded.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.admob.AdMobInterstitialMediationUtils
import org.prebid.mobile.admob.PrebidInterstitialAdapter
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class AdMobInterstitialFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobInterstitial"
    }

    protected var extras: Bundle? = null
    protected var interstitialAd: InterstitialAd? = null
    protected var adUnit: MediationInterstitialAdUnit? = null

    override val layoutRes = R.layout.fragment_admob_rewarded

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        extras = Bundle()
        val mediationUtils = AdMobInterstitialMediationUtils(extras)
        adUnit = MediationInterstitialAdUnit(activity, configId, AdUnitFormat.DISPLAY, mediationUtils)
        return adUnit
    }

    override fun loadAd() {
        val request = AdRequest
            .Builder()
            .addCustomEventExtrasBundle(PrebidInterstitialAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            InterstitialAd.load(requireContext(), adUnitId, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    btnAdLoaded?.isEnabled = true
                    btnLoad?.isEnabled = true
                    btnLoad?.text = getString(R.string.text_show)

                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = createFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    btnAdFailed?.isEnabled = true
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
        btnAdLoaded?.isEnabled = false
        btnAdShowed?.isEnabled = false
        btnAdImpression?.isEnabled = false
        btnAdDismissed?.isEnabled = false
        btnAdFailedFullScreen?.isEnabled = false
        btnAdFailed?.isEnabled = false
        btnAdClicked?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (btnLoad?.text == getString(R.string.text_show)) {
            interstitialAd?.show(requireActivity())
            btnLoad?.text = getString(R.string.text_retry)
            resetAdEvents()
        } else if (btnLoad.text == getString(R.string.text_retry)) {
            resetAdEvents()
            btnLoad?.isEnabled = false
            btnLoad?.text = "Loading..."
            loadAd()
        }
    }

    protected fun createFullScreenContentCallback(): FullScreenContentCallback {
        return object : FullScreenContentCallback() {
            override fun onAdClicked() {
                btnAdClicked?.isEnabled = true
            }

            override fun onAdImpression() {
                btnAdImpression?.isEnabled = true
            }

            override fun onAdShowedFullScreenContent() {
                btnAdShowed?.isEnabled = true
            }

            override fun onAdDismissedFullScreenContent() {
                btnAdDismissed?.isEnabled = true
            }

            override fun onAdFailedToShowFullScreenContent(p0: AdError) {
                btnAdFailedFullScreen?.isEnabled = true
            }
        }
    }


}