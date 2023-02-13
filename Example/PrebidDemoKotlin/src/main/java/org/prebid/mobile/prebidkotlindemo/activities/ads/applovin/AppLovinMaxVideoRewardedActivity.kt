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
package org.prebid.mobile.prebidkotlindemo.activities.ads.applovin

import android.os.Bundle
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxError
import com.applovin.mediation.MaxReward
import com.applovin.mediation.MaxRewardedAdListener
import com.applovin.mediation.adapters.prebid.utils.MaxMediationRewardedUtils
import com.applovin.mediation.ads.MaxRewardedAd
import org.prebid.mobile.api.mediation.MediationRewardedVideoAdUnit
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class AppLovinMaxVideoRewardedActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "897f2fc59d617715"
        const val CONFIG_ID = "imp-prebid-video-rewarded-320-480"
    }

    private var maxRewardedAd: MaxRewardedAd? = null
    private var adUnit: MediationRewardedVideoAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        maxRewardedAd = MaxRewardedAd.getInstance(AD_UNIT_ID, this)
        maxRewardedAd?.setListener(object : MaxRewardedAdListener {
            override fun onAdLoaded(ad: MaxAd?) {
                maxRewardedAd?.showAd()
            }

            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
            override fun onRewardedVideoStarted(ad: MaxAd?) {}
            override fun onRewardedVideoCompleted(ad: MaxAd?) {}
            override fun onUserRewarded(ad: MaxAd?, reward: MaxReward?) {}
        })

        val mediationUtils = MaxMediationRewardedUtils(maxRewardedAd)
        adUnit = MediationRewardedVideoAdUnit(
            this,
            CONFIG_ID,
            mediationUtils
        )

        adUnit?.fetchDemand {
            maxRewardedAd?.loadAd()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
        maxRewardedAd?.destroy()
    }

}
