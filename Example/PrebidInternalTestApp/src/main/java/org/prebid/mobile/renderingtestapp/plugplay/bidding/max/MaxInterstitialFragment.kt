package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.os.Bundle
import android.util.Log
import android.view.View
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxInterstitialMediationUtils
import com.applovin.mediation.ads.MaxInterstitialAd
import kotlinx.android.synthetic.main.events_max_interstitial.*
import kotlinx.android.synthetic.main.fragment_bidding_interstitial_applovin_max.*
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import java.util.*

open class MaxInterstitialFragment : AdFragment() {

    companion object {
        private const val TAG = "MaxInterstitialFragment"
    }

    protected var maxInterstitialAd: MaxInterstitialAd? = null
    protected var adUnit: MediationInterstitialAdUnit? = null

    override val layoutRes = R.layout.fragment_bidding_interstitial_applovin_max

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        maxInterstitialAd = MaxInterstitialAd(adUnitId, activity)
        maxInterstitialAd?.setListener(createListener())

        val mediationUtils =
            MaxInterstitialMediationUtils(
                maxInterstitialAd
            )
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            EnumSet.of(AdUnitFormat.DISPLAY),
            mediationUtils
        )
        return adUnit
    }

    override fun loadAd() {
        adUnit?.fetchDemand {
            maxInterstitialAd?.loadAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        maxInterstitialAd?.destroy()
        adUnit?.destroy()
    }

    private fun resetAdEvents() {
        btnAdLoaded?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdLoadFailed?.isEnabled = false
        btnAdDisplayFailed?.isEnabled = false
        btnAdDisplayed?.isEnabled = false
        btnAdHidden?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (btnLoad?.text == getString(R.string.text_show)) {
            maxInterstitialAd?.showAd()
            btnLoad?.text = getString(R.string.text_retry)
        } else if (btnLoad.text == getString(R.string.text_retry)) {
            resetAdEvents()
            btnLoad?.isEnabled = false
            btnLoad?.text = "Loading..."
            loadAd()
        }
    }

    protected fun createListener(): MaxAdListener {
        return object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                btnAdLoaded?.isEnabled = true
                btnLoad?.isEnabled = true
                btnLoad?.text = getString(R.string.text_show)
            }

            override fun onAdClicked(ad: MaxAd?) {
                btnAdClicked?.isEnabled = true
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                btnAdDisplayed?.isEnabled = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                btnAdHidden?.isEnabled = true
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                btnAdLoadFailed?.isEnabled = true

                btnLoad.isEnabled = true
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                btnAdDisplayFailed?.isEnabled = true

                Log.d(TAG, "onAdDisplayFailed(): ${error?.message}")
            }
        }
    }

}