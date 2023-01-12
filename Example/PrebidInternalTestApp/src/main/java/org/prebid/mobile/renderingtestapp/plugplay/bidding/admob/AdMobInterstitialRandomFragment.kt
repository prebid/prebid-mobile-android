package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.util.Log
import android.widget.Button
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.interstitial.InterstitialAd
import com.google.android.gms.ads.interstitial.InterstitialAdLoadCallback
import org.prebid.mobile.admob.PrebidInterstitialAdapter
import org.prebid.mobile.rendering.bidding.display.BidResponseCache
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView
import kotlin.random.Random

class AdMobInterstitialRandomFragment : AdMobInterstitialFragment() {

    companion object {
        private const val TAG = "AdMobInterstitial"
    }

    private val random = Random.Default

    override fun loadAd() {
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidInterstitialAdapter::class.java, extras!!)
            .build()

        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")

            randomRemovingBidResponseFromCache()

            InterstitialAd.load(requireContext(), adUnitId, request, object : InterstitialAdLoadCallback() {
                override fun onAdLoaded(ad: InterstitialAd) {
                    events.loaded(true)
                    binding.btnLoad.isEnabled = true
                    binding.btnLoad.text = getString(R.string.text_show)

                    interstitialAd = ad
                    interstitialAd?.fullScreenContentCallback = createFullScreenContentCallback()
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    events.failed(true)
                    Log.e(TAG, adError.message)
                    interstitialAd = null
                }
            })
        }
    }

    private fun randomRemovingBidResponseFromCache() {
        val randomValue = random.nextInt(0, 2)
        val doRemove = randomValue == 1
        if (doRemove) {
            Log.d("RandomAdMobRewarded", "Random removing response!")
            val responseId = extras?.getString(PrebidInterstitialAdapter.EXTRA_RESPONSE_ID) ?: ""
            if (responseId.isNotBlank()) {
                BidResponseCache.getInstance().popBidResponse(responseId)
            }
        } else {
            Log.d("RandomAdMobRewarded", "Without removing.")
        }
    }

}