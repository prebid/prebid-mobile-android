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
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

open class AdMobRewardedFragment : AdFragment() {

    companion object {
        private const val TAG = "AdMobRewarded"
    }

    protected var extras: Bundle? = null
    protected var rewardedAd: RewardedAd? = null
    protected var adUnit: MediationRewardedVideoAdUnit? = null

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
                    findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
                    findView<Button>(R.id.btnLoad)?.isEnabled = true
                    findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_show)

                    rewardedAd = ad
                    rewardedAd?.fullScreenContentCallback = createFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = true
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
        findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdShowed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdImpression)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdDismissed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdFailedFullScreen)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = false
        findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (findView<Button>(R.id.btnLoad)?.text == getString(R.string.text_show)) {
            rewardedAd?.show(requireActivity()) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward ($rewardAmount, $rewardType)")
            }
            findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_retry)
        } else if (findView<Button>(R.id.btnLoad)?.text == getString(R.string.text_retry)) {
            resetAdEvents()
            findView<Button>(R.id.btnLoad)?.isEnabled = false
            findView<Button>(R.id.btnLoad)?.text = "Loading..."
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