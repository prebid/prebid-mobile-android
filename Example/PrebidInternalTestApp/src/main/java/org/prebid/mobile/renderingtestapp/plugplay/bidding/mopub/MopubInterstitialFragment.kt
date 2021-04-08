package org.prebid.mobile.renderingtestapp.plugplay.bidding.mopub

import android.os.Bundle
import android.view.View
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import kotlinx.android.synthetic.main.events_mopub_interstitial.*
import kotlinx.android.synthetic.main.fragment_mopub_interstitial.*
import org.prebid.mobile.rendering.bidding.data.AdSize
import org.prebid.mobile.rendering.bidding.display.MoPubInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

class MopubInterstitialFragment : AdFragment() {

    override val layoutRes: Int = R.layout.fragment_mopub_interstitial

    private lateinit var mopubInterstitialAdUnit: MoPubInterstitialAdUnit
    private lateinit var moPubInterstitial: MoPubInterstitial
    private val mListener = object : MoPubInterstitial.InterstitialAdListener {
        override fun onInterstitialLoaded(interstitial: MoPubInterstitial) {
            btnAdDidLoad?.isEnabled = true
            btnLoad?.isEnabled = true
            btnLoad?.text = getString(R.string.text_show)
        }

        override fun onInterstitialFailed(interstitial: MoPubInterstitial, errorCode: MoPubErrorCode) {
            btnAdFailed?.isEnabled = true
            btnLoad?.isEnabled = true
            btnLoad?.text = getString(R.string.text_retry)
        }

        override fun onInterstitialShown(interstitial: MoPubInterstitial) {
            btnAdDisplayed?.isEnabled = true
        }

        override fun onInterstitialClicked(interstitial: MoPubInterstitial) {
            btnAdClicked?.isEnabled = true
        }

        override fun onInterstitialDismissed(interstitial: MoPubInterstitial) {
            btnAdDismissed?.isEnabled = true
            btnLoad?.isEnabled = true
            btnLoad?.text = getString(R.string.text_retry)
        }
    }

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        btnLoad.setOnClickListener {
            handleLoadButtonClick()
        }
    }

    override fun initAd(): Any? {
        initInterstitialView(adUnitId, configId, width, height, getTitle())
        return mopubInterstitialAdUnit
    }

    override fun loadAd() {
        MoPub.initializeSdk(requireContext(), SdkConfiguration.Builder(adUnitId).build()) {
            mopubInterstitialAdUnit.fetchDemand(moPubInterstitial) {
                moPubInterstitial.load()
            }
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mopubInterstitialAdUnit.destroy()
        moPubInterstitial.destroy()
    }

    private fun initInterstitialView(adUnitId: String, configId: String, minWidthPerc: Int, minHeightPerc: Int, title: String) {
        moPubInterstitial = MoPubInterstitial(requireActivity(), adUnitId)
        moPubInterstitial.interstitialAdListener = mListener

        val isVideo = (title.contains("Video", true) && !title.contains("MRAID", true))
        mopubInterstitialAdUnit = if (isVideo) {
            MoPubInterstitialAdUnit(requireContext(), configId, AdUnitFormat.VIDEO)
        }
        else {
            MoPubInterstitialAdUnit(requireContext(), configId, AdSize(minWidthPerc, minHeightPerc))
        }
    }

    private fun handleLoadButtonClick() {
        if (btnLoad?.text == getString(R.string.text_show) && moPubInterstitial.isReady) {
            moPubInterstitial.show()
        }
        else if (btnLoad.text == getString(R.string.text_retry)) {
            resetEventDisplay()
            btnLoad?.isEnabled = false
            btnLoad?.text = "Loading..."
            loadAd()
        }
    }

    private fun resetEventDisplay() {
        btnAdDidLoad?.isEnabled = false
        btnAdFailed?.isEnabled = false
        btnAdDisplayed?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdDismissed?.isEnabled = false
    }
}