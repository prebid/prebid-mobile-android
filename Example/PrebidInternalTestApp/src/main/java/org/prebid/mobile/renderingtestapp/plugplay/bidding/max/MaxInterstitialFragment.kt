package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationInterstitialUtils
import com.applovin.mediation.ads.MaxInterstitialAd
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.mediation.MediationInterstitialAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView
import java.util.*

open class MaxInterstitialFragment : AdFragment() {

    companion object {
        private const val TAG = "MaxInterstitialFragment"

        public const val ARG_IS_VIDEO = TAG + "IsVideo"
    }

    protected var maxInterstitialAd: MaxInterstitialAd? = null
    protected var adUnit: MediationInterstitialAdUnit? = null
    protected var isVideo = false

    override val layoutRes = R.layout.fragment_bidding_interstitial_applovin_max

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        findView<TextView>(R.id.adIdLabel)?.text = getString(R.string.label_auid, configId)
        findView<Button>(R.id.btnLoad)?.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        isVideo = arguments?.getBoolean(ARG_IS_VIDEO) ?: false

        maxInterstitialAd = MaxInterstitialAd(adUnitId, activity)
        maxInterstitialAd?.setListener(createListener())

        val mediationUtils = MaxMediationInterstitialUtils(maxInterstitialAd)
        var adUnitFormats = EnumSet.of(AdUnitFormat.DISPLAY)
        if (isVideo) {
            adUnitFormats = EnumSet.of(AdUnitFormat.VIDEO)
        }
        adUnit = MediationInterstitialAdUnit(
            activity,
            configId,
            adUnitFormats,
            mediationUtils
        )
        adUnit?.setMinSizePercentage(30, 30)
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
        findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdLoadFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDisplayFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDisplayed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdHidden)?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (findView<Button>(R.id.btnLoad)?.text == getString(R.string.text_show)) {
            maxInterstitialAd?.showAd()
            findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_retry)
        } else if (findView<Button>(R.id.btnLoad)?.text == getString(R.string.text_retry)) {
            resetAdEvents()
            findView<Button>(R.id.btnLoad)?.isEnabled = false
            findView<Button>(R.id.btnLoad)?.text = "Loading..."
            loadAd()
        }
    }

    protected fun createListener(): MaxAdListener {
        return object : MaxAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
                findView<Button>(R.id.btnLoad)?.isEnabled = true
                findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_show)
            }

            override fun onAdClicked(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdDisplayed)?.isEnabled = true
            }

            override fun onAdHidden(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnAdHidden)?.isEnabled = true
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                findView<EventCounterView>(R.id.btnAdLoadFailed)?.isEnabled = true

                findView<Button>(R.id.btnLoad)?.isEnabled = true
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                findView<EventCounterView>(R.id.btnAdDisplayFailed)?.isEnabled = true

                Log.d(TAG, "onAdDisplayFailed(): ${error?.message}")
            }
        }
    }

}