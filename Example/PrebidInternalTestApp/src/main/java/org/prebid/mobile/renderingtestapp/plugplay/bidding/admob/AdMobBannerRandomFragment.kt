package org.prebid.mobile.renderingtestapp.plugplay.bidding.admob

import android.util.Log
import org.prebid.mobile.admob.PrebidBannerAdapter
import org.prebid.mobile.rendering.bidding.display.BidResponseCache
import kotlin.random.Random

class AdMobBannerRandomFragment : AdMobBannerFragment() {

    private val random = Random.Default

    override fun loadAd() {
        adUnit?.fetchDemand { result ->
            Log.d("Prebid", "Fetch demand result: $result")
            randomRemovingBidResponseFromCache()
            bannerView?.loadAd(adRequest!!)
        }
    }

    private fun randomRemovingBidResponseFromCache() {
        val randomValue = random.nextInt(0, 2)
        val doRemove = randomValue == 1
        if (doRemove) {
            Log.d("RandomAdMobBanner", "Random removing response!")
            val responseId = adRequestExtras?.getString(PrebidBannerAdapter.EXTRA_RESPONSE_ID) ?: ""
            if (responseId.isNotBlank()) {
                BidResponseCache.getInstance().popBidResponse(responseId)
            }
        } else {
            Log.d("RandomAdMobBanner", "Without removing.")
        }
    }

}