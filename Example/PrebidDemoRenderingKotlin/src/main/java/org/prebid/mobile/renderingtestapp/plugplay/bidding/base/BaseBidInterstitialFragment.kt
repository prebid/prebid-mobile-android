package org.prebid.mobile.renderingtestapp.plugplay.bidding.base

import android.os.Bundle
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.events_bids.*
import kotlinx.android.synthetic.main.fragment_bidding_interstitial.*
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.rendering.bidding.listeners.InterstitialAdUnitListener
import org.prebid.mobile.rendering.bidding.parallel.InterstitialAdUnit
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.gam.GamInterstitialFragment
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

abstract class BaseBidInterstitialFragment : AdFragment(), InterstitialAdUnitListener {
    private val TAG = GamInterstitialFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_interstitial
    protected var interstitialAdUnit: InterstitialAdUnit? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)

        btnLoad?.setOnClickListener {
            handleLoadInterstitialClick()
        }
    }

    abstract fun initInterstitialAd(adUnitFormat: AdUnitFormat,
                                    adUnitId: String?, configId: String?,
                                    width: Int, height: Int)

    override fun initAd(): Any? {
        initInterstitialAd(getAdUnitIdentifierTypeBasedOnTitle(getTitle()), adUnitId, configId, width, height)
        return interstitialAdUnit
    }

    override fun loadAd() {
        interstitialAdUnit?.loadAd()
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.INTERSTITIAL
    }

    override fun onDestroyView() {
        super.onDestroyView()
        interstitialAdUnit?.destroy()
    }

    override fun onAdFailed(interstitial: InterstitialAdUnit?, exception: AdException?) {
        Log.d(TAG, "onAdFailed() called with: interstitial = [$interstitial], exception = [$exception]")
        btnAdFailed?.isEnabled = true
        btnLoad?.isEnabled = true
    }

    override fun onAdDisplayed(oxbInterstitialAdUnit: InterstitialAdUnit?) {
        Log.d(TAG, "onAdDisplayed() called with: oxbInterstitialAdUnit = [$oxbInterstitialAdUnit]")
        btnAdDisplayed?.isEnabled = true
    }

    override fun onAdClosed(interstitial: InterstitialAdUnit?) {
        Log.d(TAG, "onAdClosed() called with: interstitial = [$interstitial]")
        btnAdClosed?.isEnabled = true
    }

    override fun onAdClicked(interstitial: InterstitialAdUnit?) {
        Log.d(TAG, "onAdClicked() called with: interstitial = [$interstitial]")
        btnAdClicked?.isEnabled = true
    }

    override fun onAdLoaded(oxbInterstitialAdUnit: InterstitialAdUnit?) {
        Log.d(TAG, "onAdLoaded() called with: oxbInterstitialAdUnit = [$oxbInterstitialAdUnit]")
        btnAdLoaded?.isEnabled = true
        btnLoad?.setText(R.string.text_show)
        btnLoad?.isEnabled = true
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
                interstitialAdUnit?.show()
            }
        }
    }

    private fun getAdUnitIdentifierTypeBasedOnTitle(title: String): AdUnitFormat {
        return if (title.contains("Video Interstitial", ignoreCase = true) && !title.contains("MRAID 2.0", ignoreCase = true)) {
            AdUnitFormat.VIDEO
        }
        else {
            AdUnitFormat.DISPLAY
        }
    }
}