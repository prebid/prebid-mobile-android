package com.openx.internal_test_app.plugplay.bidding.mopub

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.display.MoPubBannerAdUnit
import com.openx.apollo.bidding.display.MoPubInterstitialAdUnit
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.bidding.base.BaseBannersWithInterstitialFragment
import com.openx.internal_test_app.utils.getAdDescription
import kotlinx.android.synthetic.main.fragment_interstitial_html_with_banners.*

class MopubBannersWithInterstitialFragment : BaseBannersWithInterstitialFragment() {

    private var moPubBannerAdUnitTop: MoPubBannerAdUnit? = null
    private var moPubBannerAdUnitBottom: MoPubBannerAdUnit? = null
    private var moPubInterstitialAdUnit: MoPubInterstitialAdUnit? = null

    private var mopubBannerViewTop: MoPubView? = null
    private var mopubBannerViewBottom: MoPubView? = null
    private var mopubInterstitial: MoPubInterstitial? = null

    override fun initUi(view: View, savedInstanceState: Bundle?) {
        MoPub.initializeSdk(requireContext(), SdkConfiguration.Builder("").build()) {
            super.initUi(view, savedInstanceState)
        }
    }

    override fun loadInterstitial() {
        val adUnitId = getString(R.string.mopub_interstitial_bidding_ad_unit_id_ok)
        tvInterstitialAdUnitDescription.text = "Interstitial ConfigId: ${interstitialConfigId}"

        moPubInterstitialAdUnit = MoPubInterstitialAdUnit(requireContext(), interstitialConfigId, AdSize(30, 30))
        mopubInterstitial = MoPubInterstitial(requireActivity(), adUnitId)
        mopubInterstitial?.interstitialAdListener = getInterstitialAdListener()

        fetchInterstitial()
    }

    override fun loadBanners() {
        val mopubAdUnitId = getString(R.string.mopub_banner_bidding_ad_unit_id_adapter)

        moPubBannerAdUnitTop = MoPubBannerAdUnit(requireContext(), bannerConfigId, AdSize(320, 50))
        moPubBannerAdUnitTop?.setRefreshInterval(REFRESH_BANNER_TOP_SEC)

        moPubBannerAdUnitBottom = MoPubBannerAdUnit(requireContext(), bannerConfigId, AdSize(320, 50))
        moPubBannerAdUnitBottom?.setRefreshInterval(REFRESH_BANNER_BOTTOM_SEC)

        mopubBannerViewTop = MoPubView(context)
        mopubBannerViewTop?.setAdUnitId(mopubAdUnitId)
        mopubBannerViewTop?.bannerAdListener = getBannerAdListener(bannerConfigId, REFRESH_BANNER_TOP_SEC, btnTopBannerAdShown)

        mopubBannerViewBottom = MoPubView(context)
        mopubBannerViewBottom?.setAdUnitId(mopubAdUnitId)
        mopubBannerViewBottom?.bannerAdListener = getBannerAdListener(bannerConfigId, REFRESH_BANNER_BOTTOM_SEC, btnBottomBannerAdShown)

        viewContainerTop?.addView(mopubBannerViewTop)
        viewContainerBottom?.addView(mopubBannerViewBottom)

        moPubBannerAdUnitTop?.fetchDemand(mopubBannerViewTop!!) {
            mopubBannerViewTop?.loadAd()
        }
        moPubBannerAdUnitBottom?.fetchDemand(mopubBannerViewBottom!!) {
            mopubBannerViewBottom?.loadAd()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        moPubInterstitialAdUnit?.destroy()
        moPubBannerAdUnitTop?.destroy()
        moPubBannerAdUnitBottom?.destroy()

        mopubInterstitial?.destroy()
        mopubBannerViewTop?.destroy()
        mopubBannerViewBottom?.destroy()
    }

    private fun fetchInterstitial() {
        if (mopubInterstitial != null) {
            moPubInterstitialAdUnit?.fetchDemand(mopubInterstitial!!) {
                mopubInterstitial?.load()
            }
        }
    }

    private fun getBannerAdListener(adUnitId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): MoPubView.BannerAdListener {
        return object : MoPubView.BannerAdListener {
            private var impressionCount: Int = 0

            init {
                impressionCounterButton?.text = adUnitId.getAdDescription(refreshIntervalSec, impressionCount)
            }

            override fun onBannerExpanded(banner: MoPubView?) {
                Log.d(TAG, "onBannerExpanded()")
            }

            override fun onBannerLoaded(banner: MoPubView) {
                Log.d(TAG, "onBannerLoaded()")
                impressionCount++
                impressionCounterButton?.text = adUnitId.getAdDescription(refreshIntervalSec, impressionCount)
            }

            override fun onBannerCollapsed(banner: MoPubView?) {
                Log.d(TAG, "onBannerCollapsed()")
            }

            override fun onBannerFailed(banner: MoPubView?, errorCode: MoPubErrorCode?) {
                Log.d(TAG, "onBannerFailed() $errorCode")
            }

            override fun onBannerClicked(banner: MoPubView?) {
                Log.d(TAG, "onBannerClicked()")
            }
        }
    }

    private fun getInterstitialAdListener(): MoPubInterstitial.InterstitialAdListener {
        return object : MoPubInterstitial.InterstitialAdListener {
            override fun onInterstitialLoaded(interstitial: MoPubInterstitial?) {
                Log.d(TAG, "onInterstitialLoaded()")
                btnLoad?.isEnabled = true
                btnLoad?.text = getString(R.string.text_show)
                btnLoad?.setOnClickListener { interstitial?.show() }
            }

            override fun onInterstitialShown(interstitial: MoPubInterstitial?) {
                Log.d(TAG, "onInterstitialShown()")
            }

            override fun onInterstitialFailed(interstitial: MoPubInterstitial?, errorCode: MoPubErrorCode?) {
                Log.e(TAG, " onInterstitialFailed(): $errorCode")
                btnLoad?.isEnabled = false
                btnLoad?.text = getString(R.string.title_preload_failed)
            }

            override fun onInterstitialDismissed(interstitial: MoPubInterstitial?) {
                Log.d(TAG, "onInterstitialDismissed()")
                fetchInterstitial()
            }

            override fun onInterstitialClicked(interstitial: MoPubInterstitial?) {
                Log.d(TAG, "onInterstitialClicked()")
            }
        }
    }
}