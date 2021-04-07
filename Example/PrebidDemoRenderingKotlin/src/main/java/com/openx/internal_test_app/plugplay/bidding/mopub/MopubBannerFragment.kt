package com.openx.internal_test_app.plugplay.bidding.mopub

import android.os.Bundle
import android.util.Log
import android.view.View
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.display.MoPubBannerAdUnit
import com.openx.internal_test_app.AdFragment
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.config.AdConfiguratorDialogFragment
import kotlinx.android.synthetic.main.events_bids.btnAdFailed
import kotlinx.android.synthetic.main.events_mopub_banner.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*

open class MopubBannerFragment : AdFragment() {
    private val TAG = MopubBannerFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_banner_mopub

    protected var bannerView: MoPubView? = null
    protected var bannerAdUnit: MoPubBannerAdUnit? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        super.initUi(view, savedInstanceState)
        adIdLabel.text = getString(R.string.label_auid, configId)
        btnLoad?.setOnClickListener {
            resetAdEvents()
            it.isEnabled = false
            loadAd()
        }

        btnStopRefresh?.setOnClickListener {
            bannerAdUnit?.stopRefresh()
            resetEventButtons()
            btnLoad?.isEnabled = true
        }
    }

    override fun initAd(): Any? {
        bannerView = MoPubView(requireContext())
        bannerView?.setAdUnitId(adUnitId)
        bannerView?.bannerAdListener = listener
        viewContainer.addView(bannerView)

        bannerAdUnit = MoPubBannerAdUnit(requireContext(), configId, AdSize(width, height))
        bannerAdUnit?.setRefreshInterval(refreshDelay)
        return bannerAdUnit
    }

    override fun loadAd() {
        MoPub.initializeSdk(requireContext(), SdkConfiguration.Builder(adUnitId).build()) {
            bannerAdUnit?.fetchDemand(bannerView!!) {
                bannerView?.loadAd()
            }
        }
    }

    override fun configuratorMode(): AdConfiguratorDialogFragment.AdConfiguratorMode? {
        return AdConfiguratorDialogFragment.AdConfiguratorMode.BANNER
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bannerAdUnit?.destroy()
        bannerView?.destroy()
    }

    private fun resetAdEvents() {
        btnAdDidLoad?.isEnabled = false
        btnAdFailed?.isEnabled = false
        btnAdClicked?.isEnabled = false
        btnAdExpanded?.isEnabled = false
        btnAdCollapsed?.isEnabled = false
    }

    private val listener = object : MoPubView.BannerAdListener {
        override fun onBannerLoaded(banner: MoPubView) {
            Log.d(TAG, "onBannerLoaded")
            resetAdEvents()
            btnAdDidLoad?.isEnabled = true
            btnLoad?.isEnabled = true
        }

        override fun onBannerFailed(banner: MoPubView, errorCode: MoPubErrorCode) {
            Log.d(TAG, "onBannerFailed - $errorCode")
            resetAdEvents()
            btnAdFailed?.isEnabled = true
            btnLoad?.isEnabled = true
        }

        override fun onBannerClicked(banner: MoPubView) {
            Log.d(TAG, "onBannerClicked")
            btnAdClicked?.isEnabled = true
        }

        override fun onBannerExpanded(banner: MoPubView) {
            Log.d(TAG, "onBannerExpanded")
            btnAdExpanded?.isEnabled = true
        }

        override fun onBannerCollapsed(banner: MoPubView) {
            Log.d(TAG, "onBannerCollapsed")
            btnAdCollapsed?.isEnabled = true
        }
    }
}