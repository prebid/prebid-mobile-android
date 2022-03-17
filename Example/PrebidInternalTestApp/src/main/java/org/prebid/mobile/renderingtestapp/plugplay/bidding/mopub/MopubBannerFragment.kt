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
import com.mopub.common.MoPub
import com.mopub.common.SdkConfiguration
import com.mopub.mediation.MoPubBannerMediationUtils
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubView
import kotlinx.android.synthetic.main.events_mopub_banner.*
import kotlinx.android.synthetic.main.fragment_bidding_banner.*
import org.prebid.mobile.AdSize
import org.prebid.mobile.rendering.bidding.display.MediationBannerAdUnit
import org.prebid.mobile.renderingtestapp.AdFragment
import org.prebid.mobile.renderingtestapp.R
import org.prebid.mobile.renderingtestapp.plugplay.config.AdConfiguratorDialogFragment

open class MopubBannerFragment : AdFragment() {
    private val TAG = MopubBannerFragment::class.java.simpleName

    override val layoutRes = R.layout.fragment_bidding_banner_mopub

    protected var bannerView: MoPubView? = null
    protected var bannerAdUnit: MediationBannerAdUnit? = null

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

        val mediationUtils = MoPubBannerMediationUtils(bannerView)
        bannerAdUnit = MediationBannerAdUnit(
            requireContext(),
            configId,
            AdSize(width, height),
            mediationUtils
        )
        bannerAdUnit?.setRefreshInterval(refreshDelay)
        return bannerAdUnit
    }

    override fun loadAd() {
        MoPub.initializeSdk(requireContext(), SdkConfiguration.Builder(adUnitId).build()) {
            bannerAdUnit?.fetchDemand {
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