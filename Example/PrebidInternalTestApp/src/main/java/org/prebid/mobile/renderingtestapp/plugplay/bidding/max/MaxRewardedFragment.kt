package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.os.Bundle
import android.util.Log
import android.view.View
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.adapters.prebid.utils.MaxMediationRewardedUtils
import com.applovin.mediation.ads.MaxRewardedAd
import kotlinx.android.synthetic.main.events_max_rewarded.*
import kotlinx.android.synthetic.main.fragment_bidding_rewarded_applovin_max.*
import org.prebid.mobile.api.mediation.MediationRewardedVideoAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

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

        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad.setOnClickListener {
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
        btnAdLoaded?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdLoadFailed?.isEnabled = false
        btnAdDisplayFailed?.isEnabled = false
        btnAdDisplayed?.isEnabled = false
        btnAdHidden?.isEnabled = false
        btnRewardedVideoStarted?.isEnabled = false
        btnRewardedVideoCompleted?.isEnabled = false
        btnUserRewarded?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (btnLoad?.text == getString(R.string.text_show)) {
            maxRewardedAd?.showAd()
            btnLoad?.text = getString(R.string.text_retry)
        } else if (btnLoad.text == getString(R.string.text_retry)) {
            resetAdEvents()
            btnLoad?.isEnabled = false
            btnLoad?.text = "Loading..."
            loadAd()
        }
    }

    private fun createListener(): MaxRewardedAdListener {
        return object : MaxRewardedAdListener {
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

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                btnRewardedVideoStarted?.isEnabled = true
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                btnRewardedVideoCompleted?.isEnabled = true
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                btnUserRewarded?.isEnabled = true
            }
        }
    }

}