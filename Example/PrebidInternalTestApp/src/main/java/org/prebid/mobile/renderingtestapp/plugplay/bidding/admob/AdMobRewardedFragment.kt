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
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.prebid.mobile.admob.AdMobMediationRewardedUtils
import org.prebid.mobile.admob.PrebidRewardedAdapter
import org.prebid.mobile.api.mediation.MediationRewardedVideoAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.databinding.FragmentAdmobRewardedBinding
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.utils.BaseEvents
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class AdMobRewardedFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobRewarded"
    }

    protected var extras: Bundle? = null
    protected var rewardedAd: RewardedAd? = null
    protected var adUnit: MediationRewardedVideoAdUnit? = null

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
        val mediationUtils = AdMobMediationRewardedUtils(extras)
        adUnit = MediationRewardedVideoAdUnit(
            activity,
            configId,
            mediationUtils
        )
        return adUnit
    }

    override fun loadAd() {
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidRewardedAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            RewardedAd.load(requireContext(), adUnitId, request, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    events.loaded(true)
                    binding.btnLoad.isEnabled = true
                    binding.btnLoad.text = getString(R.string.text_show)

                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = createFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    events.failed(true)
                    Log.e(TAG, adError.message)
                    rewardedAd = null
                }
            })
        }

    }

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    override fun onDestroy() {
        super.onDestroy()

        rewardedAd = null

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
        if (binding.btnLoad.text == getString(R.string.text_show)) {
            rewardedAd?.show(requireActivity()) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward ($rewardAmount, $rewardType)")
            }
            binding.btnLoad.text = getString(R.string.text_retry)
        } else if (binding.btnLoad.text == getString(R.string.text_retry)) {
            resetAdEvents()
            binding.btnLoad.isEnabled = false
            binding.btnLoad.text = "Loading..."
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