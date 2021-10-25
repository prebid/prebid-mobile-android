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
import androidx.preference.PreferenceManager
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
import org.prebid.mobile.rendering.bidding.listeners.BannerViewListener
import org.prebid.mobile.rendering.bidding.parallel.BannerView
import org.prebid.mobile.rendering.errors.AdException
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings

class DemoActivity : AppCompatActivity() {

    companion object {
        const val TAG = "DemoActivity"
    }

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

    override fun onDestroy() {
        super.onDestroy()
        if (adUnit != null) {
            adUnit?.stopAutoRefresh()
            adUnit = null
        }
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
            adServerName == "In-App" -> {
                createInAppRenderingBanner()
            }
            else -> {
                Log.e(TAG, "Can't create MoPubBanner")
            }
        }
    }

    private fun createDFPBanner() {
        val adWrapper = binding.frameAdWrapper
        adWrapper.removeAllViews()

        val adView = AdManagerAdView(this)
        val (width, height) = parseSizes()
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
                        Log.e(TAG, "error: $error")
                    }
                })

            }
        }

        adView.setAdSizes(AdSize(width, height))
        adWrapper.addView(adView)

        val builder = AdManagerAdRequest.Builder()
        adUnit?.setAutoRefreshPeriodMillis(adRefreshTime ?: 0)
        adUnit?.fetchDemand(builder) { resultCode ->
            Log.d(TAG, resultCode.toString())
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

        val (width, height) = parseSizes()
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

    private fun createInAppRenderingBanner() {
        val host = org.prebid.mobile.rendering.bidding.enums.Host.CUSTOM
        host.hostUrl = "https://prebid.openx.net/openrtb2/auction"
        PrebidRenderingSettings.setBidServerHost(host)
        PrebidRenderingSettings.setAccountId("0689a263-318d-448b-a3d4-b02e8a709d9d")

        // Using fake GDPR
        PreferenceManager.getDefaultSharedPreferences(this).edit().apply {
            putInt("IABTCF_gdprApplies", 0)
            putInt("IABTCF_CmpSdkID", 123)
            apply()
        }

        val adWrapper = binding.frameAdWrapper
        adWrapper.removeAllViews()

        val (width, height) = parseSizes()
        val adView = BannerView(
            this,
            "50699c03-0910-477c-b4a4-911dbe2b9d42",
            org.prebid.mobile.rendering.bidding.data.AdSize(width, height)
        )
        adView.setAutoRefreshDelay(adRefreshTime ?: 0)
        adWrapper.addView(adView)
        adView.setBannerListener(object : BannerViewListener {
            override fun onAdLoaded(bannerView: BannerView?) {
                Log.d(TAG, "On ad loaded!")
            }

            override fun onAdDisplayed(bannerView: BannerView?) {
                Log.d(TAG, "On ad displayed!")
            }

            override fun onAdFailed(bannerView: BannerView?, exception: AdException?) {
                Log.d(TAG, "On ad failed!")
            }

            override fun onAdClicked(bannerView: BannerView?) {
                Log.d(TAG, "On ad clicked!")
            }

            override fun onAdClosed(bannerView: BannerView?) {
                Log.d(TAG, "On ad closed!")
            }
        })

        adView.loadAd()
    }

    private fun parseSizes(): List<Int> {
        val wAndH = adSizeName?.split("x".toRegex())?.dropLastWhile { it.isEmpty() }?.toTypedArray() ?: emptyArray()
        var width = 0
        var height = 0
        try {
            width = Integer.valueOf(wAndH[0])
            height = Integer.valueOf(wAndH[1])
        } catch (exception: Exception) {
        }
        return listOf(width, height)
    }

}
