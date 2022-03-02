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

package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import android.os.Bundle
import android.view.View
import com.mopub.common.MoPub
import com.mopub.common.MoPubReward
import com.mopub.common.SdkConfiguration
import com.mopub.mediation.MoPubRewardedVideoMediationUtils
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubRewardedAdListener
import com.mopub.mobileads.MoPubRewardedAdManager
import com.mopub.mobileads.MoPubRewardedAds
import kotlinx.android.synthetic.main.events_mopub_rewarded.*
import kotlinx.android.synthetic.main.fragment_mopub_interstitial_video_rewarded.*
import org.prebid.mobile.rendering.bidding.display.MediationRewardedVideoAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

class MoPubRewardedVideoFragment: AdFragment() {
    override val layoutRes = R.layout.fragment_mopub_interstitial_video_rewarded

    private var rewardedAdUnit: MediationRewardedVideoAdUnit? = null
    private val keywordsMap = HashMap<String, String>()
    private val mListener = object : MoPubRewardedAdListener {

        override fun onRewardedAdLoadSuccess(adUnitId: String) {
            btnAdDidLoad.isEnabled = true
            btnLoad.isEnabled = true
            btnLoad.text = getString(R.string.text_show)
        }

        override fun onRewardedAdLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {
            btnAdFailed.isEnabled = true
            btnLoad.isEnabled = true
            btnLoad.text = getString(R.string.text_retry)
        }

        override fun onRewardedAdStarted(adUnitId: String) {
            btnAdVideoStarted.isEnabled = true
        }

        override fun onRewardedAdShowError(adUnitId: String, errorCode: MoPubErrorCode) {
            btnAdVideoPlaybackError.isEnabled = true
        }

        override fun onRewardedAdClicked(adUnitId: String) {
            btnAdClicked.isEnabled = true
        }

        override fun onRewardedAdClosed(adUnitId: String) {
            btnAdCollapsed.isEnabled = true
            btnLoad.isEnabled = true
            btnLoad.text = getString(R.string.text_retry)
        }

        override fun onRewardedAdCompleted(adUnitIds: Set<String?>, reward: MoPubReward) {
            btnAdCompleted.isEnabled = true
        }
    }

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        btnLoad.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        val mediationUtils = MoPubRewardedVideoMediationUtils(keywordsMap)
        rewardedAdUnit = MediationRewardedVideoAdUnit(
            requireContext(),
            configId,
            mediationUtils
        )
        return rewardedAdUnit
    }

    override fun loadAd() {
        val builder = SdkConfiguration.Builder(adUnitId)
        MoPubRewardedAdManager.init(requireActivity())
        MoPubRewardedAdManager.updateActivity(requireActivity())
        MoPubRewardedAds.setRewardedAdListener(mListener)
        MoPub.initializeSdk(requireContext(), builder.build()) {
            fetchAdUnit(adUnitId)
        }
    }

    override fun configuratorMode() = AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL

    override fun onDestroyView() {
        super.onDestroyView()
        rewardedAdUnit?.destroy()
    }

    private fun fetchAdUnit(adUnitId: String) {
        rewardedAdUnit?.fetchDemand {
            val keywordsString = convertMapToMoPubKeywords(keywordsMap)
            val params = MoPubRewardedAdManager.RequestParameters(keywordsString)

            MoPubRewardedAds.loadRewardedAd(adUnitId, params, null)
        }
    }

    private fun handleLoadButtonClick() {
        if (btnLoad.text == getString(R.string.text_show) && MoPubRewardedAds.hasRewardedAd(adUnitId)) {
            MoPubRewardedAds.showRewardedAd(adUnitId)
        }
        else if (btnLoad.text == getString(R.string.text_retry)) {
            resetEventDisplay()
            btnLoad.isEnabled = false
            btnLoad.text = "Loading..."
            loadAd()
        }
    }

    private fun resetEventDisplay() {
        btnAdDidLoad.isEnabled = false
        btnAdFailed.isEnabled = false
        btnAdVideoStarted.isEnabled = false
        btnAdVideoPlaybackError.isEnabled = false
        btnAdClicked.isEnabled = false
        btnAdCollapsed.isEnabled = false
        btnAdCompleted.isEnabled = false
    }

    private fun convertMapToMoPubKeywords(keywordMap: Map<String, String>): String? {
        val result = StringBuilder()
        for (key in keywordMap.keys) {
            result.append(key).append(":").append(keywordMap[key]).append(",")
        }
        if (result.isNotEmpty()) {
            result.delete(result.length - 1, result.length)
        }
        return result.toString()
    }
}