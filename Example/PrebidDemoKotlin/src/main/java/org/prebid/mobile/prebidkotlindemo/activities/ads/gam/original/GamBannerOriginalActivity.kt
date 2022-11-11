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
package org.prebid.mobile.prebidkotlindemo.activities.ads.gam.original

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import org.prebid.mobile.BannerAdUnit
import org.prebid.mobile.PrebidMobile
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.prebidkotlindemo.R
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityDemoBinding
import org.prebid.mobile.prebidkotlindemo.testcases.TestCase
import org.prebid.mobile.prebidkotlindemo.testcases.TestCaseRepository
import org.prebid.mobile.prebidkotlindemo.utils.Settings
import org.prebid.mobile.prebidkotlindemo.utils.ViewUtils

class GamBannerOriginalActivity : AppCompatActivity() {

    companion object {
        const val TAG = "GamBannerOriginal"
    }

    private lateinit var binding: ActivityDemoBinding
    private var testCase: TestCase = TestCaseRepository.lastTestCase

    private var adUnit: BannerAdUnit? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)
        ViewUtils.setTestCaseName(testCase.fullName, this)

        PrebidMobile.setStoredAuctionResponse("response-prebid-banner-320-50")
        createAd()
    }

    private fun createAd() {
        val adView = AdManagerAdView(this)
        adView.adUnitId = "/21808260008/prebid_demo_app_original_api_banner"
        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                Log.d(TAG, "Banner loaded!")

                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        adView.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {}
                })

            }
        }
        adView.setAdSizes(AdSize(320, 50))
        binding.frameAdWrapper.addView(adView)

        val request = AdManagerAdRequest.Builder().build()
        adUnit = BannerAdUnit("imp-prebid-banner-320-50", 320, 50)
        adUnit?.setAutoRefreshInterval(Settings.get().refreshTimeSeconds)
        adUnit?.fetchDemand(request) { resultCode ->
            Log.d(TAG, "Result code: $resultCode")
            adView.loadAd(request)
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        adUnit?.stopAutoRefresh()
        adUnit = null
    }

}
