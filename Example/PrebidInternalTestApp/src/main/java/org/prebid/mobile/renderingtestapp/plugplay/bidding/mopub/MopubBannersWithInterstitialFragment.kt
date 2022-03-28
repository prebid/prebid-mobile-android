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
import android.util.Log
import android.view.View
import android.widget.Button
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mediation.MoPubBannerMediationUtils
import com.mopub.mediation.MoPubInterstitialMediationUtils
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import kotlinx.android.synthetic.main.fragment_interstitial_html_with_banners.*
import org.prebid.mobile.AdSize
import org.prebid.mobile.rendering.bidding.display.MediationBannerAdUnit
import org.prebid.mobile.rendering.bidding.display.MediationInterstitialAdUnit
import org.prebid.mobile.rendering.bidding.enums.AdUnitFormat
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.bidding.base.BaseBannersWithInterstitialFragment
import org.prebid.mobile.renderingtestapp.utils.getAdDescription
import java.util.*

class MopubBannersWithInterstitialFragment : BaseBannersWithInterstitialFragment() {

    private var mediationBannerAdUnitTop: MediationBannerAdUnit? = null
    private var mediationBannerAdUnitBottom: MediationBannerAdUnit? = null
    private var mediationInterstitialAdUnit: MediationInterstitialAdUnit? = null

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

        mopubInterstitial = MoPubInterstitial(requireActivity(), adUnitId)
        mopubInterstitial?.interstitialAdListener = getInterstitialAdListener()

        val mediationUtils = MoPubInterstitialMediationUtils(mopubInterstitial)
        mediationInterstitialAdUnit =
            MediationInterstitialAdUnit(
                requireContext(),
                interstitialConfigId,
                EnumSet.of(AdUnitFormat.DISPLAY),
                mediationUtils
            )
        mediationInterstitialAdUnit?.setMinSizePercentage(30, 30)

        fetchInterstitial()
    }

    override fun loadBanners() {
        val mopubAdUnitId = getString(R.string.mopub_banner_bidding_ad_unit_id_adapter)

        mopubBannerViewTop = MoPubView(context)
        mopubBannerViewTop?.setAdUnitId(mopubAdUnitId)
        mopubBannerViewTop?.bannerAdListener =
            getBannerAdListener(bannerConfigId, REFRESH_BANNER_TOP_SEC, btnTopBannerAdShown)

        mopubBannerViewBottom = MoPubView(context)
        mopubBannerViewBottom?.setAdUnitId(mopubAdUnitId)
        mopubBannerViewBottom?.bannerAdListener =
            getBannerAdListener(bannerConfigId, REFRESH_BANNER_BOTTOM_SEC, btnBottomBannerAdShown)

        val topMediationUtils = MoPubBannerMediationUtils(mopubBannerViewTop)
        mediationBannerAdUnitTop = MediationBannerAdUnit(
            requireContext(),
            bannerConfigId,
            AdSize(320, 50),
            topMediationUtils
        )
        mediationBannerAdUnitTop?.setRefreshInterval(REFRESH_BANNER_TOP_SEC)

        val bottomMediationUtils = MoPubBannerMediationUtils(mopubBannerViewBottom)
        mediationBannerAdUnitBottom =
            MediationBannerAdUnit(
                requireContext(),
                bannerConfigId,
                AdSize(320, 50),
                bottomMediationUtils
            )
        mediationBannerAdUnitBottom?.setRefreshInterval(REFRESH_BANNER_BOTTOM_SEC)

        viewContainerTop?.addView(mopubBannerViewTop)
        viewContainerBottom?.addView(mopubBannerViewBottom)

        mediationBannerAdUnitTop?.fetchDemand {
            mopubBannerViewTop?.loadAd()
        }
        mediationBannerAdUnitBottom?.fetchDemand {
            mopubBannerViewBottom?.loadAd()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        mediationInterstitialAdUnit?.destroy()
        mediationBannerAdUnitTop?.destroy()
        mediationBannerAdUnitBottom?.destroy()

        mopubInterstitial?.destroy()
        mopubBannerViewTop?.destroy()
        mopubBannerViewBottom?.destroy()
    }

    private fun fetchInterstitial() {
        if (mopubInterstitial != null) {
            mediationInterstitialAdUnit?.fetchDemand {
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