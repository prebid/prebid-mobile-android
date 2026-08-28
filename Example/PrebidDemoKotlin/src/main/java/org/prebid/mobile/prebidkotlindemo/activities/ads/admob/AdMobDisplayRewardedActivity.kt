/*
 *    Copyright 2018-2019 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.prebid.mobile.prebidkotlindemo.activities.ads.admob

import android.os.Bundle
import android.util.Log
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import org.prebid.mobile.admob.AdMobMediationRewardedUtils
import org.prebid.mobile.admob.PrebidRewardedAdapter
import org.prebid.mobile.api.data.AdUnitFormat
import org.prebid.mobile.api.data.FetchDemandResult
import org.prebid.mobile.api.mediation.MediationRewardedAdUnit
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import java.util.EnumSet

class AdMobDisplayRewardedActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "ca-app-pub-1875909575462531/1908212572"
        const val CONFIG_ID = "prebid-demo-banner-rewarded-time"
    }

    private var rewardedAd: RewardedAd? = null
    private var adUnit: MediationRewardedAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val extras = Bundle()
        val request = AdRequest
            .Builder()
            .addNetworkExtrasBundle(PrebidRewardedAdapter::class.java, extras)
            .build()
        val mediationUtils = AdMobMediationRewardedUtils(extras)
        adUnit = MediationRewardedAdUnit(
            this,
            CONFIG_ID,
            EnumSet.of(AdUnitFormat.BANNER),
            mediationUtils
        )
        adUnit?.fetchDemand { result ->
            Log.d("AdMobRewarded", "Prebid demand fetch result: $result")
            if (result != FetchDemandResult.SUCCESS) {
                Log.d("AdMobRewarded", "Continuing with AdMob waterfall fallback.")
            }

            RewardedAd.load(this, AD_UNIT_ID, request, object : RewardedAdLoadCallback() {
                override fun onAdLoaded(ad: RewardedAd) {
                    Log.d("AdMobRewarded", "Ad was loaded.")
                    rewardedAd = ad
                    rewardedAd?.show(this@AdMobDisplayRewardedActivity) {
                        Log.d("AdExample", "User earned reward: ${it.amount} ${it.type}")
                    }
                }

                override fun onAdFailedToLoad(adError: LoadAdError) {
                    Log.e("AdMobRewarded", adError.message)
                    rewardedAd = null
                }
            })
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }

}
