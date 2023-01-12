package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.util.Log
import android.widget.Button
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.prebid.mobile.admob.PrebidRewardedAdapter
import org.prebid.mobile.rendering.bidding.display.BidResponseCache
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView
import kotlin.random.Random

class AdMobRewardedRandomFragment : AdMobRewardedFragment() {

    companion object {
        private const val TAG = "AdMobRewarded"
    }

    private val random = Random.Default

    override fun loadAd() {
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidRewardedAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            randomRemovingBidResponseFromCache()

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

    private fun randomRemovingBidResponseFromCache() {
        val randomValue = random.nextInt(0, 2)
        val doRemove = randomValue == 1
        if (doRemove) {
            Log.d("RandomAdMobRewarded", "Random removing response!")
            val responseId = extras?.getString(PrebidRewardedAdapter.EXTRA_RESPONSE_ID) ?: ""
            if (responseId.isNotBlank()) {
                BidResponseCache.getInstance().popBidResponse(responseId)
            }
        } else {
            Log.d("RandomAdMobRewarded", "Without removing.")
        }
    }


}