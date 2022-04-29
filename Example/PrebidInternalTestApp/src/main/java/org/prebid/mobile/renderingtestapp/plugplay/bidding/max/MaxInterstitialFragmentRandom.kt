package org.prebid.mobile.renderingtestapp.plugplay.bidding.max

import android.util.Log
import com.applovin.mediation.adapters.PrebidMaxMediationAdapter

class MaxInterstitialFragmentRandom : MaxInterstitialFragment() {

    companion object {
        private val TAG = MaxInterstitialFragmentRandom::class.simpleName
    }

    private val random = kotlin.random.Random.Default

    override fun loadAd() {
        adUnit?.fetchDemand {
            randomRemovingBidResponseFromCache()
            maxInterstitialAd?.loadAd()
        }
    }

    private fun randomRemovingBidResponseFromCache() {
        val randomValue = random.nextInt(0, 2)
        val doRemove = randomValue == 1
        if (doRemove) {
            Log.d(TAG, "Random removing response!")
            maxInterstitialAd?.setLocalExtraParameter(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, null)
        } else {
            Log.d(TAG, "Without removing.")
        }
    }

}