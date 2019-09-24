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
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.FrameLayout
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.doubleclick.PublisherAdRequest
import com.google.android.gms.ads.doubleclick.PublisherAdView
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd
import com.mopub.mobileads.MoPubErrorCode
import com.mopub.mobileads.MoPubInterstitial
import com.mopub.mobileads.MoPubView
import org.prebid.mobile.*
import org.prebid.mobile.addendum.AdViewUtils
import org.prebid.mobile.addendum.PbFindSizeError
import org.prebid.mobile.prebidkotlindemo.Constants.MOPUB_BANNER_ADUNIT_ID_300x250
import org.prebid.mobile.prebidkotlindemo.Constants.MOPUB_BANNER_ADUNIT_ID_320x50

class DemoActivity : AppCompatActivity() {
    internal var refreshCount: Int = 0
    internal var adUnit: AdUnit? = null
    lateinit var resultCode: ResultCode

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        refreshCount = 0
        setContentView(R.layout.activity_demo)
        val intent = intent
        if ("DFP" == intent.getStringExtra(Constants.AD_SERVER_NAME) && "Banner" == intent.getStringExtra(Constants.AD_TYPE_NAME)) {
            createDFPBanner(intent.getStringExtra(Constants.AD_SIZE_NAME))
        } else if ("DFP" == intent.getStringExtra(Constants.AD_SERVER_NAME) && "Interstitial" == intent.getStringExtra(
                Constants.AD_TYPE_NAME
            )
        ) {
            createDFPInterstitial()
        } else if ("MoPub" == intent.getStringExtra(Constants.AD_SERVER_NAME) && "Banner" == intent.getStringExtra(
                Constants.AD_TYPE_NAME
            )
        ) {
            createMoPubBanner(intent.getStringExtra(Constants.AD_SIZE_NAME))
        } else if ("MoPub" == intent.getStringExtra(Constants.AD_SERVER_NAME) && "Interstitial" == intent.getStringExtra(
                Constants.AD_TYPE_NAME
            )
        ) {
            createMoPubInterstitial()
        }
    }

    internal fun createDFPBanner(size: String) {
        val adFrame = findViewById(R.id.adFrame) as FrameLayout
        adFrame.removeAllViews()
        val dfpAdView = PublisherAdView(this)
        val wAndH = size.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val width = Integer.valueOf(wAndH[0])
        val height = Integer.valueOf(wAndH[1])
        if (width == 300 && height == 250) {
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES)
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, width, height)
        } else if (width == 320 && height == 50) {
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES)
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height)
        } else {
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES)
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height)
        }

        dfpAdView.setAdListener(object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()

                AdViewUtils.findPrebidCreativeSize(dfpAdView, object : AdViewUtils.PbFindSizeListener {
                    override fun success(width: Int, height: Int) {
                        dfpAdView.setAdSizes(AdSize(width, height))

                    }

                    override fun failure(error: PbFindSizeError) {
                        Log.d("MyTag", "error: $error")
                    }
                })

            }
        })

        dfpAdView.setAdSizes(AdSize(width, height))
        adFrame.addView(dfpAdView)
        val builder = PublisherAdRequest.Builder()

        val request = builder.build()

        //region PrebidMobile Mobile API 1.0 usage
        val millis = intent.getIntExtra(Constants.AUTO_REFRESH_NAME, 0)
        adUnit!!.setAutoRefreshPeriodMillis(millis)
        adUnit!!.fetchDemand(request, object : OnCompleteListener {
            override fun onComplete(resultCode: ResultCode) {
                this@DemoActivity.resultCode = resultCode
                dfpAdView.loadAd(request)
                refreshCount++
            }
        })
        //endregion
    }

    internal fun createDFPInterstitial() {
        val interstitialAd = PublisherInterstitialAd(this)
        interstitialAd.setAdUnitId(Constants.DFP_INTERSTITIAL_ADUNIT_ID)
        interstitialAd.setAdListener(object : AdListener() {
            override fun onAdLoaded() {
                super.onAdLoaded()
                interstitialAd.show()
            }

            override fun onAdFailedToLoad(i: Int) {
                super.onAdFailedToLoad(i)
                val builder: AlertDialog.Builder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = AlertDialog.Builder(this@DemoActivity, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    builder = AlertDialog.Builder(this@DemoActivity)
                }
                builder.setTitle("Failed to load DFP interstitial ad")
                    .setMessage("Error code: $i")
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }
        })
        adUnit = InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL)
        val millis = intent.getIntExtra(Constants.AUTO_REFRESH_NAME, 0)
        adUnit!!.setAutoRefreshPeriodMillis(millis)
        val builder = PublisherAdRequest.Builder()
        val request = builder.build()
        adUnit!!.fetchDemand(request, object : OnCompleteListener {
            override fun onComplete(resultCode: ResultCode) {
                this@DemoActivity.resultCode = resultCode
                interstitialAd.loadAd(request)
                refreshCount++
            }
        })

    }

    internal fun createMoPubBanner(size: String) {
        val adFrame = findViewById(R.id.adFrame) as FrameLayout
        adFrame.removeAllViews()
        val wAndH = size.split("x".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        val width = Integer.valueOf(wAndH[0])
        val height = Integer.valueOf(wAndH[1])
        val adView = MoPubView(this)
        if (width == 300 && height == 250) {
            adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_300x250)
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, 300, 250)
        } else {
            adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_320x50)
            adUnit = BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, 320, 50)
        }
        adView.setMinimumWidth(width)
        adView.setMinimumHeight(height)
        adFrame.addView(adView)

        adUnit!!.setAutoRefreshPeriodMillis(intent.getIntExtra(Constants.AUTO_REFRESH_NAME, 0))
        adUnit!!.fetchDemand(adView, object : OnCompleteListener {
            override fun onComplete(resultCode: ResultCode) {
                this@DemoActivity.resultCode = resultCode
                adView.loadAd()
                refreshCount++
            }
        })
    }

    internal fun createMoPubInterstitial() {
        val interstitial = MoPubInterstitial(this, Constants.MOPUB_INTERSTITIAL_ADUNIT_ID)
        interstitial.setInterstitialAdListener(object : MoPubInterstitial.InterstitialAdListener {
            override fun onInterstitialLoaded(interstitial: MoPubInterstitial) {
                interstitial.show()
            }

            override fun onInterstitialFailed(interstitial: MoPubInterstitial, errorCode: MoPubErrorCode) {
                val builder: AlertDialog.Builder
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = AlertDialog.Builder(this@DemoActivity, android.R.style.Theme_Material_Dialog_Alert)
                } else {
                    builder = AlertDialog.Builder(this@DemoActivity)
                }
                builder.setTitle("Failed to load MoPub interstitial ad")
                    .setMessage("Error code: " + errorCode.toString())
                    .setIcon(android.R.drawable.ic_dialog_alert)
                    .show()
            }

            override fun onInterstitialShown(interstitial: MoPubInterstitial) {

            }

            override fun onInterstitialClicked(interstitial: MoPubInterstitial) {

            }

            override fun onInterstitialDismissed(interstitial: MoPubInterstitial) {

            }
        })
        adUnit = InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL)
        val millis = intent.getIntExtra(Constants.AUTO_REFRESH_NAME, 0)
        adUnit!!.setAutoRefreshPeriodMillis(millis)
        adUnit!!.fetchDemand(interstitial, object : OnCompleteListener {
            override fun onComplete(resultCode: ResultCode) {
                this@DemoActivity.resultCode = resultCode
                interstitial.load()
                refreshCount++
            }
        })
    }

    override fun onDestroy() {
        super.onDestroy()
        if (adUnit != null) {
            adUnit!!.stopAutoRefresh()
            adUnit = null
        }
    }

    internal fun stopAutoRefresh() {
        if (adUnit != null) {
            adUnit!!.stopAutoRefresh()
        }
    }
}
