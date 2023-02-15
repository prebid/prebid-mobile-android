/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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
package org.prebid.mobile.prebidkotlindemo.activities.ads.applovin

import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import android.widget.FrameLayout
import com.applovin.mediation.MaxAd
import com.applovin.mediation.MaxAdViewAdListener
import com.applovin.mediation.MaxError
import com.applovin.mediation.adapters.prebid.utils.MaxMediationBannerUtils
import com.applovin.mediation.ads.MaxAdView
import org.prebid.mobile.AdSize
import org.prebid.mobile.api.mediation.MediationBannerAdUnit
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.activities.BaseAdActivity

class AppLovinMaxDisplayBanner320x50Activity : BaseAdActivity() {

    companion object {
        const val AD_UNIT_ID = "3d8a0bcbb6d571d5"
        const val CONFIG_ID = "imp-prebid-banner-320-50"
        const val WIDTH = 320
        const val HEIGHT = 50
    }

    private var adView: MaxAdView? = null
    private var adUnit: MediationBannerAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        createAd()
    }

    private fun createAd() {
        adView = MaxAdView(AD_UNIT_ID, this)
        adView?.setListener(object : MaxAdViewAdListener {
            override fun onAdLoadFailed(adUnitId: String?, error: MaxError?) {
                Log.d("AppLovinMaxBanner", "onAdLoadFailed(): ${error?.message}")
            }

            override fun onAdLoaded(ad: MaxAd?) {}
            override fun onAdHidden(ad: MaxAd?) {}
            override fun onAdClicked(ad: MaxAd?) {}
            override fun onAdExpanded(ad: MaxAd?) {}
            override fun onAdCollapsed(ad: MaxAd?) {}
            override fun onAdDisplayed(ad: MaxAd?) {}
            override fun onAdDisplayFailed(ad: MaxAd?, error: MaxError?) {}
        })
        adView?.layoutParams = FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            resources.getDimensionPixelSize(R.dimen.banner_height)
        )

        adWrapperView.addView(adView)

        val mediationUtils = MaxMediationBannerUtils(adView)
        adUnit = MediationBannerAdUnit(
            this,
            CONFIG_ID,
            AdSize(WIDTH, HEIGHT),
            mediationUtils
        )
        adUnit?.setRefreshInterval(refreshTimeSeconds)
        adUnit?.fetchDemand {
            adView?.loadAd()
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.destroy()
        adView?.destroy()
    }

}
