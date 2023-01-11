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
import android.widget.Button
import org.prebid.mobile.api.exceptions.AdException
import org.prebid.mobile.api.rendering.RewardedAdUnit
import org.prebid.mobile.api.rendering.listeners.RewardedAdUnitListener
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import org.prebid.mobile.renderingtestapp.widgets.EventCounterView

abstract class BaseBidRewardedFragment : AdFragment() {

    private val TAG = BaseBidRewardedFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_interstitial
    protected var rewardedAdUnit: RewardedAdUnit? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        findView<Button>(R.id.btnLoad)?.setOnClickListener {
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

    private fun handleLoadInterstitialClick() {
        when (findView<Button>(R.id.btnLoad)?.text) {
            getString(R.string.text_load) -> {
                findView<Button>(R.id.btnLoad)?.isEnabled = false
                resetEventButtons()
                loadAd()
            }

            getString(R.string.text_show) -> {
                findView<Button>(R.id.btnLoad)?.text = getString(R.string.text_load)
                rewardedAdUnit?.show()
            }
        }
    }

    protected fun createRewardedAdUnitListener() = object :
        RewardedAdUnitListener {

        override fun onAdLoaded(rewardedAdUnit: RewardedAdUnit?) {
            Log.d(TAG, "onAdLoaded() called with: reward = [${rewardedAdUnit?.userReward}]")
            findView<EventCounterView>(R.id.btnAdLoaded)?.isEnabled = true
            findView<Button>(R.id.btnLoad)?.setText(R.string.text_show)
            findView<Button>(R.id.btnLoad)?.isEnabled = true
        }

        override fun onAdDisplayed(rewardedAdUnit: RewardedAdUnit?) {
            Log.d(TAG, "onAdDisplayed() called with: rewardedAdUnit = [$rewardedAdUnit]")
            findView<EventCounterView>(R.id.btnAdDisplayed)?.isEnabled = true
        }

        override fun onAdFailed(rewardedAdUnit: RewardedAdUnit?, exception: AdException?) {
            Log.d(TAG, "onAdFailed() called with: rewardedAdUnit = [$rewardedAdUnit], exception = [$exception]")
            findView<EventCounterView>(R.id.btnAdFailed)?.isEnabled = true
            findView<Button>(R.id.btnLoad)?.isEnabled = true
        }

        override fun onAdClicked(rewardedAdUnit: RewardedAdUnit?) {
            Log.d(TAG, "onAdClicked() called with: rewardedAdUnit = [$rewardedAdUnit]")
            findView<EventCounterView>(R.id.btnAdClicked)?.isEnabled = true
        }

        override fun onAdClosed(rewardedAdUnit: RewardedAdUnit?) {
            Log.d(TAG, "onAdClosed() called with: rewardedAdUnit = [$rewardedAdUnit]")
            findView<EventCounterView>(R.id.btnAdClosed)?.isEnabled = true
        }

        override fun onUserEarnedReward(rewardedAdUnit: RewardedAdUnit?) {
            Log.d(TAG, "onUserEarnedReward() called with: reward = [${rewardedAdUnit?.userReward}]")
        }

    }
}