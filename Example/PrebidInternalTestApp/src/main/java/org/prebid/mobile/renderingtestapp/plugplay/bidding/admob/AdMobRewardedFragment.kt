package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.os.Bundle
import android.util.Log
import android.view.View
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import kotlinx.android.synthetic.main.events_admob_rewarded.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.admob.AdMobRewardedMediationUtils
import org.prebid.mobile.admob.PrebidRewardedAdapter
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

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

        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        extras = Bundle()
        val mediationUtils = AdMobRewardedMediationUtils(extras)
        adUnit = MediationRewardedVideoAdUnit(activity, configId, mediationUtils)
        return adUnit
    }

    open override fun loadAd() {
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidRewardedAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            RewardedAd.load(requireContext(), adUnitId, request, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    btnAdLoaded?.isEnabled = true
                    btnLoad?.isEnabled = true
                    btnLoad?.text = getString(R.string.text_show)

                    rewardedAd = ad
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    btnAdFailed?.isEnabled = true
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
        btnAdLoaded?.isEnabled = false
        btnAdFailed?.isEnabled = false
    }

    private fun handleLoadButtonClick() {
        if (btnLoad?.text == getString(R.string.text_show)) {
            rewardedAd?.show(requireActivity()) { rewardItem ->
                val rewardAmount = rewardItem.amount
                val rewardType = rewardItem.type
                Log.d(TAG, "User earned the reward ($rewardAmount, $rewardType)")
            }
            btnLoad?.text = getString(R.string.text_retry)
            resetAdEvents()
        } else if (btnLoad.text == getString(R.string.text_retry)) {
            resetAdEvents()
            btnLoad?.isEnabled = false
            btnLoad?.text = "Loading..."
            loadAd()
        }
    }

}