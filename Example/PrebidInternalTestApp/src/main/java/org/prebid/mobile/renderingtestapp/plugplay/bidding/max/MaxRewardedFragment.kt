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
import org.prebid.mobile.renderingtestapp.databinding.FragmentBiddingRewardedApplovinMaxBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class MaxRewardedFragment : AdFragment() {

    companion object {
        private const val TAG = "MaxRewardedFragment"
    }

    protected var maxRewardedAd: MaxRewardedAd? = null
    protected var adUnit: MediationRewardedVideoAdUnit? = null

    override val layoutRes = R.layout.fragment_bidding_rewarded_applovin_max

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    private val binding: FragmentBiddingRewardedApplovinMaxBinding
        get() = getBinding()
    private lateinit var events: Events

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        events = Events(view)
        binding.adIdLabel.text = getString(R.string.label_auid, configId)
        binding.btnLoad.setOnClickListener {
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
        events.loaded(false)
        events.clicked(false)
        events.loadFailed(false)
        events.displayFailed(false)
        events.displayed(false)
        events.hidden(false)
        events.rewardedVideoStarted(false)
        events.rewardedVideoCompleted(false)
        events.userRewarded(false)
    }

    private fun handleLoadButtonClick() {
        if (binding.btnLoad.text == getString(R.string.text_show)) {
            maxRewardedAd?.showAd()
            binding.btnLoad.text = getString(R.string.text_retry)
        } else if (binding.btnLoad.text == getString(R.string.text_retry)) {
            resetAdEvents()
            binding.btnLoad.isEnabled = false
            binding.btnLoad.text = "Loading..."
            loadAd()
        }
    }

    private fun createListener(): MaxRewardedAdListener {
        return object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                events.loaded(true)
                binding.btnLoad.isEnabled = true
                binding.btnLoad.text = getString(R.string.text_show)
            }

            override fun onAdClicked(ad: MaxAd?) {
                events.clicked(true)
            }

            override fun onAdDisplayed(ad: MaxAd?) {
                events.displayed(true)
            }

            override fun onAdHidden(ad: MaxAd?) {
                events.hidden(true)
            }

            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                events.loadFailed(true)

                binding.btnLoad.isEnabled = true
                Log.d(TAG, "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {
                events.displayFailed(true)

                Log.d(TAG, "onAdDisplayFailed(): ${error?.message}")
            }

            override fun onRewardedVideoStarted(ad: MaxAd?) {
                events.rewardedVideoStarted(true)
            }

            override fun onRewardedVideoCompleted(ad: MaxAd?) {
                events.rewardedVideoCompleted(true)
            }

            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {
                events.userRewarded(true)
            }
        }
    }

    protected class Events(parentView: View) : BaseEvents(parentView) {

        fun loaded(b: Boolean) = enable(R.id.btnAdLoaded, b)
        fun impression(b: Boolean) = enable(R.id.btnAdImpression, b)
        fun clicked(b: Boolean) = enable(R.id.btnAdClicked, b)
        fun failed(b: Boolean) = enable(R.id.btnAdFailed, b)
        fun userRewarded(b: Boolean) = enable(R.id.btnUserRewarded, b)

        fun displayed(b: Boolean) = enable(R.id.btnAdDisplayed, b)
        fun hidden(b: Boolean) = enable(R.id.btnAdHidden, b)
        fun loadFailed(b: Boolean) = enable(R.id.btnAdLoadFailed, b)
        fun displayFailed(b: Boolean) = enable(R.id.btnAdDisplayFailed, b)
        fun rewardedVideoStarted(b: Boolean) = enable(R.id.btnRewardedVideoStarted, b)
        fun rewardedVideoCompleted(b: Boolean) = enable(R.id.btnRewardedVideoCompleted, b)

    }

}