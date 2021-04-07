package com.openx.internal_test_app.plugplay.bidding.ppm

import android.util.Log
import android.widget.Button
import com.openx.apollo.bidding.data.AdSize
import com.openx.apollo.bidding.listeners.BannerViewListener
import com.openx.apollo.bidding.listeners.InterstitialAdUnitListener
import com.openx.apollo.bidding.parallel.BannerView
import com.openx.apollo.bidding.parallel.InterstitialAdUnit
import com.openx.apollo.errors.AdException
import com.openx.internal_test_app.R
import com.openx.internal_test_app.plugplay.bidding.base.BaseBannersWithInterstitialFragment
import com.openx.internal_test_app.utils.getAdDescription
import kotlinx.android.synthetic.main.fragment_interstitial_html_with_banners.*

open class PpmBannersWithInterstitialFragment : BaseBannersWithInterstitialFragment() {

    protected lateinit var interstitialAdUnit: InterstitialAdUnit
    protected lateinit var bannerViewTop: BannerView
    protected lateinit var bannerViewBottom: BannerView

    override fun loadInterstitial() {
        tvInterstitialAdUnitDescription.text = "Interstitial Config ID: $interstitialConfigId"
        interstitialAdUnit = initInterstitialAdUnit(interstitialConfigId)
        interstitialAdUnit.loadAd()
    }

    override fun loadBanners() {
        bannerViewTop = initBannerView(bannerConfigId, REFRESH_BANNER_TOP_SEC, btnTopBannerAdShown)
        bannerViewBottom = initBannerView(bannerConfigId, REFRESH_BANNER_BOTTOM_SEC, btnBottomBannerAdShown)

        bannerViewTop.loadAd()
        bannerViewBottom.loadAd()

        viewContainerTop?.addView(bannerViewTop)
        viewContainerBottom?.addView(bannerViewBottom)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        interstitialAdUnit.destroy()
        bannerViewBottom.destroy()
        bannerViewTop.destroy()
    }

    open fun initBannerView(configId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): BannerView {
        val bannerView = BannerView(requireContext(), configId, AdSize(BANNER_WIDTH, BANNER_HEIGHT))
        bannerView.setAutoRefreshDelay(refreshIntervalSec)
        bannerView.setBannerListener(getBannerAdListener(configId, refreshIntervalSec, impressionCounterButton))
        return bannerView
    }

    open fun initInterstitialAdUnit(configId: String): InterstitialAdUnit {
        val interstitialAdUnit = InterstitialAdUnit(requireContext(), configId, AdSize(30, 30))
        interstitialAdUnit.setInterstitialAdUnitListener(getInterstitialAdListener())
        return interstitialAdUnit
    }

    protected fun getBannerAdListener(configId: String, refreshIntervalSec: Int, impressionCounterButton: Button?): BannerViewListener {
        return object : BannerViewListener {
            private var impressionCount: Int = 0

            init {
                impressionCounterButton?.text = configId.getAdDescription(refreshIntervalSec, impressionCount)
            }

            override fun onAdLoaded(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdLoaded()")
                impressionCount++
                impressionCounterButton?.text = configId.getAdDescription(refreshIntervalSec, impressionCount)
            }

            override fun onAdDisplayed(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdDisplayed()")
            }

            override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
                Log.d(TAG, "Banner: onAdFailed()")
            }

            override fun onAdClicked(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdClicked()")
            }

            override fun onAdClosed(bannerView: BannerView?) {
                Log.d(TAG, "Banner: onAdClosed()")
            }
        }
    }

    protected fun getInterstitialAdListener(): InterstitialAdUnitListener {
        return object : InterstitialAdUnitListener {
            override fun onAdLoaded(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdLoaded()")
                btnLoad?.isEnabled = true
                btnLoad?.text = getString(R.string.text_show)
                btnLoad?.setOnClickListener { interstitialAdUnit?.show() }
            }

            override fun onAdDisplayed(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdDisplayed()")
            }

            override fun onAdFailed(interstitialAdUnit: InterstitialAdUnit?, exception: AdException?) {
                Log.d(TAG, "Interstitial: onAdFailed()")
            }

            override fun onAdClicked(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdClicked()")
            }

            override fun onAdClosed(interstitialAdUnit: InterstitialAdUnit?) {
                Log.d(TAG, "Interstitial: onAdClosed()")
                interstitialAdUnit?.loadAd()
            }

        }
    }
}