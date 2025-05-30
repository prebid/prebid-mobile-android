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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.rendering

import android.os.Bundle
import android.util.Log
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.RewardedAdUnit
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener
import org.prebid.mobile.eventhandlers.GamRewardedEventHandler
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity
import org.prebid.mobile.rendering.interstitial.rewarded.Reward

class GamRenderingApiVideoRewardedActivity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "/21808260008/prebid-demo-app-original-api-video-interstitial"
        const val CONFIG_ID = "prebid-demo-video-rewarded-endcard-time-close-button"
    }

    private var adUnit: RewardedAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {    
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        val eventHandler = GamRewardedEventHandler(this, AD_UNIT_ID)
        adUnit = RewardedAdUnit(this, CONFIG_ID, eventHandler)
        adUnit?.setRewardedAdUnitListener(object : RewardedAdUnitListener {
            override fun onAdLoaded(rewardedAdUnit: RewardedAdUnit?) {
                adUnit?.show()
            }

            override fun onAdDisplayed(rewardedAdUnit: RewardedAdUnit?) {}
            override fun onAdFailed(rewardedAdUnit: RewardedAdUnit?, exception: AdException?) {}
            override fun onAdClicked(rewardedAdUnit: RewardedAdUnit?) {}
            override fun onAdClosed(rewardedAdUnit: RewardedAdUnit?) {}
            override fun onUserEarnedReward(rewardedAdUnit: RewardedAdUnit?, reward: Reward?) {
                Log.d("AdExample", "User earned reward: $reward")
            }
        })
        adUnit?.loadAd()
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
    }

}
