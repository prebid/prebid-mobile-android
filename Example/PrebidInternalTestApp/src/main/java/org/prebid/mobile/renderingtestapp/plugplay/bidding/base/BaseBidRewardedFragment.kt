/*
 *    Copyright 2018-2021 Prebid.org, Inc.
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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.base

import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.fragment_bidding_interstitial.*
import org.prebid.mobile.rendering.bidding.listeners.RewardedAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.RewardedAdUnit
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

abstract class BaseBidRewardedFragment : AdFragment(), RewardedAdUnitListener {
    private val TAG = BaseBidRewardedFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_interstitial
    protected var rewardedAdUnit: RewardedAdUnit? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        btnLoad?.setOnClickListener {
            handleLoadInterstitialClick()
        }
    }

    abstract fun initRewardedAd(adUnitId: String?, configId: String?)

    override fun initAd(): Any? {
        initRewardedAd(adUnitId, configId)
        return rewardedAdUnit
    }

    override fun loadAd() {
        rewardedAdUnit?.loadAd()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rewardedAdUnit?.destroy()
    }

    override fun onAdFailed(rewardedAdUnit: RewardedAdUnit?, exception: AdException?) {
        Log.d(TAG, "onAdFailed() called with: rewardedAdUnit = [$rewardedAdUnit], exception = [$exception]")
        btnAdFailed?.isEnabled = true
        btnLoad?.isEnabled = true
    }

    override fun onAdDisplayed(rewardedAdUnit: RewardedAdUnit?) {
        Log.d(TAG, "onAdDisplayed() called with: rewardedAdUnit = [$rewardedAdUnit]")
        btnAdDisplayed?.isEnabled = true
    }

    override fun onAdClosed(rewardedAdUnit: RewardedAdUnit?) {
        Log.d(TAG, "onAdClosed() called with: rewardedAdUnit = [$rewardedAdUnit]")
        btnAdClosed?.isEnabled = true
    }

    override fun onAdClicked(rewardedAdUnit: RewardedAdUnit?) {
        Log.d(TAG, "onAdClicked() called with: rewardedAdUnit = [$rewardedAdUnit]")
        btnAdClicked?.isEnabled = true
    }

    override fun onAdLoaded(rewardedAdUnit: RewardedAdUnit?) {
        Log.d(TAG, "onAdLoaded() called with: reward = [${rewardedAdUnit?.userReward}]")
        btnAdLoaded?.isEnabled = true
        btnLoad?.setText(R.string.text_show)
        btnLoad?.isEnabled = true
    }

    override fun onUserEarnedReward(rewardedAdUnit: RewardedAdUnit?) {
        Log.d(TAG, "onUserEarnedReward() called with: reward = [${rewardedAdUnit?.userReward}]")
    }

    private fun handleLoadInterstitialClick() {
        when (btnLoad?.text) {
            getString(R.string.text_load) -> {
                btnLoad?.isEnabled = false
                resetEventButtons()
                loadAd()
            }
            getString(R.string.text_show) -> {
                btnLoad?.text = getString(R.string.text_load)
                rewardedAdUnit?.show()
            }
        }
    }
}