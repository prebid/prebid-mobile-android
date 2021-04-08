package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import android.os.Bundle
import android.view.View
import com.mopub.common.MoPub
import com.mopub.common.MoPubReward
import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubRewardedVideoListener
import com.mopub.mobileads.MoPubRewardedVideoManager
import com.mopub.mobileads.MoPubRewardedVideos
import kotlinx.android.synthetic.main.events_mopub_rewarded.*
import kotlinx.android.synthetic.main.fragment_mopub_interstitial_video_rewarded.*
import org.prebid.mobile.rendering.bidding.display.MoPubRewardedVideoAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment
import java.util.*

class MoPubRewardedVideoFragment: AdFragment() {
    override val layoutRes = R.layout.fragment_mopub_interstitial_video_rewarded

    private var rewardedAdUnit: MoPubRewardedVideoAdUnit? = null
    private val keywordsMap = HashMap<String, String>()
    private val mListener = object : MoPubRewardedVideoListener {

        override fun onRewardedVideoLoadSuccess(adUnitId: String) {
            btnAdDidLoad.isEnabled = true
            btnLoad.isEnabled = true
            btnLoad.text = getString(R.string.text_show)
        }

        override fun onRewardedVideoLoadFailure(adUnitId: String, errorCode: MoPubErrorCode) {
            btnAdFailed.isEnabled = true
            btnLoad.isEnabled = true
            btnLoad.text = getString(R.string.text_retry)
        }

        override fun onRewardedVideoStarted(adUnitId: String) {
            btnAdVideoStarted.isEnabled = true
        }

        override fun onRewardedVideoPlaybackError(adUnitId: String, errorCode: MoPubErrorCode) {
            btnAdVideoPlaybackError.isEnabled = true
        }

        override fun onRewardedVideoClicked(adUnitId: String) {
            btnAdClicked.isEnabled = true
        }

        override fun onRewardedVideoClosed(adUnitId: String) {
            btnAdCollapsed.isEnabled = true
            btnLoad.isEnabled = true
            btnLoad.text = getString(R.string.text_retry)
        }

        override fun onRewardedVideoCompleted(adUnitIds: Set<String>, reward: MoPubReward) {
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
        rewardedAdUnit = MoPubRewardedVideoAdUnit(requireContext(), adUnitId, configId)
        return rewardedAdUnit
    }

    override fun loadAd() {
        val builder = SdkConfiguration.Builder(adUnitId)
        MoPubRewardedVideoManager.init(requireActivity())
        MoPubRewardedVideoManager.updateActivity(requireActivity())
        MoPubRewardedVideos.setRewardedVideoListener(mListener)
        MoPub.initializeSdk(requireContext(), builder.build()) {
            fetchAdUnit(adUnitId)
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        rewardedAdUnit?.destroy()
    }

    private fun fetchAdUnit(adUnitId: String) {
        rewardedAdUnit?.fetchDemand(keywordsMap) {
            val keywordsString = convertMapToMoPubKeywords(keywordsMap)
            val params = MoPubRewardedVideoManager.RequestParameters(keywordsString)

            MoPubRewardedVideos.loadRewardedVideo(adUnitId, params, null)
        }
    }

    private fun handleLoadButtonClick() {
        if (btnLoad.text == getString(R.string.text_show) && MoPubRewardedVideos.hasRewardedVideo(adUnitId)) {
            MoPubRewardedVideos.showRewardedVideo(adUnitId)
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