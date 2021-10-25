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
package org.prebid.mobile.prebidkotlindemo

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.admanager.AdManagerAdRequest
import com.google.android.gms.ads.admanager.AdManagerAdView
import com.google.android.gms.ads.admanager.AdManagerInterstitialAd
import com.google.android.gms.ads.admanager.AdManagerInterstitialAdLoadCallback
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import org.prebid.mobile.*
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.prebidkotlindemo.Constants.MOPUB_BANNER_ADUNIT_ID_300x250
import org.prebid.mobile.prebidkotlindemo.Constants.MOPUB_BANNER_ADUNIT_ID_320x50
import org.prebid.mobile.prebidkotlindemo.databinding.ActivityDemoBinding

class DemoActivity : AppCompatActivity() {

    private var refreshCount: Int = 0
    private var adUnit: AdUnit? = null

    private var adServerName = ""
    private var adTypeName = ""
    private var adSizeName: String? = null
    private var adRefreshTime: Int? = null

    private lateinit var binding: ActivityDemoBinding
    private lateinit var resultCode: ResultCode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this, R.layout.activity_demo)

        parseArguments()
        createBanner()
    }

    private fun parseArguments() {
        intent.apply {
            adServerName = getStringExtra(Constants.AD_SERVER_NAME) ?: ""
            adTypeName = getStringExtra(Constants.AD_TYPE_NAME) ?: ""
            adSizeName = getStringExtra(Constants.AD_SIZE_NAME)
            val refreshExtra = getIntExtra(Constants.AUTO_REFRESH_NAME, -1)
            if (refreshExtra != -1) {
                adRefreshTime = refreshExtra
            }
        }
    }

    private fun createBanner() {
        when {
            adServerName == "DFP" && adTypeName == "Banner" -> {
                createDFPBanner()
            }
            adServerName == "DFP" && adTypeName == "Interstitial" -> {
                createDFPInterstitial()
            }
            adServerName == "MoPub" && adTypeName == "Banner" -> {
                val size = intent.getStringExtra(Constants.AD_SIZE_NAME) ?: ""
                createMoPubBanner(size)
            }
            adServerName == "MoPub" && adTypeName == "Interstitial" -> {
                createMoPubInterstitial()
            }
        }
    }

    private fun createDFPBanner() {
        val adWrapper = binding.frameAdWrapper
        adWrapper.removeAllViews()

        val adView = AdManagerAdView(this)
        val wAndH = adSizeName?.split("x".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray() ?: emptyArray()
        var width = 0
        var height = 0
        try {
            width = Integer.valueOf(wAndH[0])
            height = Integer.valueOf(wAndH[1])
        } catch (exception: Exception) {}

        if (width == 300 && height == 250) {
            adView.adUnitId = Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, width, height)
        } else if (width == 320 && height == 50) {
            adView.adUnitId = Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height)
        } else {
            Log.w("DemoActivity", "Wrong size. Using 320x50.")
            adView.adUnitId = Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height)
        }

        adView.adListener = object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                AdViewUtils.findPrebidCreativeSize(adView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        adView.setAdSizes(AdSize(width, height))
                    }

                    override fun failure(error: PbFindSizeError) {
                        Log.e("DemoActivity", "error: $error")
                    }
                })

            }
        }

        adView.setAdSizes(AdSize(width, height))
        adWrapper.addView(adView)

        val builder = AdManagerAdRequest.Builder()
        adUnit?.setAutoRefreshPeriodMillis(adRefreshTime ?: 0)
        adUnit?.fetchDemand(builder) { resultCode ->
            Log.d("DemoActivity", resultCode.toString())
            this@DemoActivity.resultCode = resultCode
            adView.loadAd(builder.build())
            refreshCount++
        }
    }

    private fun createDFPInterstitial() {
        val builder = AdManagerAdRequest.Builder()
        val request = builder.build()
        adUnit = InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL)
        adUnit?.setAutoRefreshPeriodMillis(adRefreshTime ?: 0)
        adUnit?.fetchDemand(request) { resultCode ->
            this@DemoActivity.resultCode = resultCode

            val adLoadCallback = object : AdManagerInterstitialAdLoadCallback() {
                override fun onAdLoaded(adManagerInterstitialAd: AdManagerInterstitialAd) {
                    super.onAdLoaded(adManagerInterstitialAd)
                    adManagerInterstitialAd.show(this@DemoActivity)
                }

                override fun onAdFailedToLoad(loadAdError: LoadAdError) {
                    super.onAdFailedToLoad(loadAdError)

                    val builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        AlertDialog.Builder(this@DemoActivity, android.R.style.Theme_Material_Dialog_Alert)
                    } else {
                        AlertDialog.Builder(this@DemoActivity)
                    }
                    builder.setTitle("Failed to load DFP interstitial ad")
                        .setMessage("Error: $loadAdError")
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show()
                }
            }
            AdManagerInterstitialAd.load(
                this@DemoActivity,
                "/5300653/pavliuchyk_test_adunit_1x1_puc",
                request,
                adLoadCallback
            )

            refreshCount++
        }

    }

    private fun createMoPubBanner(size: String) {
        val adWrapper = binding.frameAdWrapper
        adWrapper.removeAllViews()
        val wAndH = size.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val width = Integer.valueOf(wAndH[0])
        val height = Integer.valueOf(wAndH[1])
        val adView = MoPubView(this)
        if (width == 300 && height == 250) {
            adView.adUnitId = MOPUB_BANNER_ADUNIT_ID_300x250
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, 300, 250)
        } else {
            adView.adUnitId = MOPUB_BANNER_ADUNIT_ID_320x50
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, 320, 50)
        }
        adView.minimumWidth = width
        adView.minimumHeight = height
        adWrapper.addView(adView)

        adUnit?.setAutoRefreshPeriodMillis(intent.getIntExtra(Constants.AUTO_REFRESH_NAME, 0))
        adUnit?.fetchDemand(adView) { resultCode ->
            this@DemoActivity.resultCode = resultCode
            adView.loadAd()
            refreshCount++
        }
    }

    private fun createMoPubInterstitial() {
        val interstitial = MoPubInterstitial(this, Constants.MOPUB_INTERSTITIAL_ADUNIT_ID)
        interstitial.interstitialAdListener = object : MoPubInterstitial.InterstitialAdListener {
            override fun onInterstitialLoaded(interstitial: MoPubInterstitial) {
                interstitial.show()
            }

            override fun onInterstitialFailed(interstitial: MoPubInterstitial, errorCode: MoPubErrorCode) {
                val builder: AlertDialog.Builder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    AlertDialog.Builder(this@DemoActivity, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    AlertDialog.Builder(this@DemoActivity)
                }
                builder.setTitle("Failed to load MoPub interstitial ad")
                    .setMessage("Error code: $errorCode")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }

            override fun onInterstitialShown(interstitial: MoPubInterstitial) {}
            override fun onInterstitialClicked(interstitial: MoPubInterstitial) {}
            override fun onInterstitialDismissed(interstitial: MoPubInterstitial) {}
        }
        adUnit = InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL)
        val millis = intent.getIntExtra(Constants.AUTO_REFRESH_NAME, 0)
        adUnit?.setAutoRefreshPeriodMillis(millis)
        adUnit?.fetchDemand(interstitial) { resultCode ->
            this@DemoActivity.resultCode = resultCode
            interstitial.load()
            refreshCount++
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (adUnit != null) {
            adUnit?.stopAutoRefresh()
            adUnit = null
        }
    }

}
