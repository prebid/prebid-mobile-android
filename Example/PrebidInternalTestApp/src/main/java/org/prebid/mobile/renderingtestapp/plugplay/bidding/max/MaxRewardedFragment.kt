package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.adapters.prebid.utils.MaxMediationRewardedUtils
import com.applovin.mediation.ads.MaxRewardedAd
import org.prebid.mobile.api.mediation.MediationRewardedVideoAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class MaxRewardedFragment : AdFragment() {

    companion object {
        private const val TAG = "MaxRewardedFragment"
    }

    protected var maxRewardedAd: MaxRewardedAd? = null
    protected var adUnit: MediationRewardedVideoAdUnit? = null

    override val layoutRes = R.layout.fragment_bidding_rewarded_applovin_max

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        findView<TextView>(R.id.adIdLabel)?.text = getString(R.string.label_auid, configId)
        findView<Button>(R.id.btnLoad)?.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        maxRewardedAd = MaxRewardedAd.getInstance(adUnitId, activity)
        maxRewardedAd?.setListener(createListener())

        val mediationUtils = MaxMediationRewardedUtils(
            maxRewardedAd
        )
        adUnit = MediationRewardedVideoAdUnit(
            activity,
            configId,
            mediationUtils
        )
        return adUnit
    }

    override fun loadAd() {
        adUnit?.fetchDemand {
            maxRewardedAd?.loadAd()
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        maxRewardedAd?.destroy()
        adUnit?.destroy()
    }

    private fun resetAdEvents() {
        findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdLoadFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDisplayFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDisplayed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdHidden)?.isEnabled = false
        findView<EventCounterView>(R.id.btnRewardedVideoStarted)?.isEnabled = false
        findView<EventCounterView>(R.id.btnRewardedVideoCompleted)?.isEnabled = false
        findView<EventCounterView>(R.id.btnUserRewarded)?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (findView<Button>(R.id.btnLoad)?.text == getString(R.string.text_show)) {
            maxRewardedAd?.showAd()
            findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_retry)
        } else if (findView<Button>(R.id.btnLoad)?.text == getString(R.string.text_retry)) {
            resetAdEvents()
            findView<Button>(R.id.btnLoad)?.isEnabled = false
            findView<Button>(R.id.btnLoad)?.text = "Loading..."
            loadAd()
        }
    }

    private fun createListener(): MaxRewardedAdListener {
        return object : MaxRewardedAdListener {
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

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnRewardedVideoStarted)?.isEnabled = true
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                findView<EventCounterView>(R.id.btnRewardedVideoCompleted)?.isEnabled = true
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                findView<EventCounterView>(R.id.btnUserRewarded)?.isEnabled = true
            }
        }
    }

}