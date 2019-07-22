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

package org.prebid.mobile.app;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import static org.prebid.mobile.app.Constants.MOPUB_BANNER_ADUNIT_ID_300x250;
import static org.prebid.mobile.app.Constants.MOPUB_BANNER_ADUNIT_ID_320x50;

public class DemoActivity extends AppCompatActivity {
    int refreshCount;
    AdUnit adUnit;
    ResultCode resultCode;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshCount = 0;
        setContentView(R.layout.activity_demo);
        Intent intent = getIntent();
        if ("DFP".equals(intent.getStringExtra(Constants.AD_SERVER_NAME)) && "Banner".equals(intent.getStringExtra(Constants.AD_TYPE_NAME))) {
            createDFPBanner(intent.getStringExtra(Constants.AD_SIZE_NAME));
        } else if ("DFP".equals(intent.getStringExtra(Constants.AD_SERVER_NAME)) && "Interstitial".equals(intent.getStringExtra(Constants.AD_TYPE_NAME))) {
            createDFPInterstitial();
        } else if ("MoPub".equals(intent.getStringExtra(Constants.AD_SERVER_NAME)) && "Banner".equals(intent.getStringExtra(Constants.AD_TYPE_NAME))) {
            createMoPubBanner(intent.getStringExtra(Constants.AD_SIZE_NAME));
        } else if ("MoPub".equals(intent.getStringExtra(Constants.AD_SERVER_NAME)) && "Interstitial".equals(intent.getStringExtra(Constants.AD_TYPE_NAME))) {
            createMoPubInterstitial();
        }
    }

    void createDFPBanner(String size) {
        FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        final PublisherAdView dfpAdView = new PublisherAdView(this);
        String[] wAndH = size.split("x");
        int width = Integer.valueOf(wAndH[0]);
        int height = Integer.valueOf(wAndH[1]);
        if (width == 300 && height == 250) {
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES);
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, width, height);
        } else if (width == 320 && height == 50) {
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES);
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height);
        } else {
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES);
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height);
        }

        dfpAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                AdViewUtils.findPrebidCreativeSize(dfpAdView, new AdViewUtils.PbFindSizeListener() {
                    @Override
                    public void success(int width, int height) {
                        dfpAdView.setAdSizes(new AdSize(width, height));

                    }

                    @Override
                    public void failure(@NonNull PbFindSizeError error) {
                        Log.d("MyTag", "error: " + error);
                    }
                });

            }
        });

        dfpAdView.setAdSizes(new AdSize(width, height));
        adFrame.addView(dfpAdView);
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();

        final PublisherAdRequest request = builder.build();

        //region PrebidMobile Mobile API 1.0 usage
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                dfpAdView.loadAd(request);
                refreshCount++;
            }
        });
        //endregion
    }

    void createDFPInterstitial() {
        final PublisherInterstitialAd interstitialAd = new PublisherInterstitialAd(this);
        interstitialAd.setAdUnitId(Constants.DFP_INTERSTITIAL_ADUNIT_ID);
        interstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                interstitialAd.show();
            }

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(DemoActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(DemoActivity.this);
                }
                builder.setTitle("Failed to load DFP interstitial ad")
                        .setMessage("Error code: " + i)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
        adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        final PublisherAdRequest request = builder.build();
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                interstitialAd.loadAd(request);
                refreshCount++;
            }
        });

    }

    void createMoPubBanner(String size) {
        FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        String[] wAndH = size.split("x");
        int width = Integer.valueOf(wAndH[0]);
        int height = Integer.valueOf(wAndH[1]);
        final MoPubView adView = new MoPubView(this);
        if (width == 300 && height == 250) {
            adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_300x250);
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, 300, 250);
        } else {
            adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_320x50);
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, 320, 50);
        }
        adView.setMinimumWidth(width);
        adView.setMinimumHeight(height);
        adFrame.addView(adView);

        adUnit.setAutoRefreshPeriodMillis(getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0));
        adUnit.fetchDemand(adView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                adView.loadAd();
                refreshCount++;
            }
        });
    }

    void createMoPubInterstitial() {
        final MoPubInterstitial interstitial = new MoPubInterstitial(this, Constants.MOPUB_INTERSTITIAL_ADUNIT_ID);
        interstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                interstitial.show();
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                AlertDialog.Builder builder;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new AlertDialog.Builder(DemoActivity.this, android.R.style.Theme_Material_Dialog_Alert);
                } else {
                    builder = new AlertDialog.Builder(DemoActivity.this);
                }
                builder.setTitle("Failed to load MoPub interstitial ad")
                        .setMessage("Error code: " + errorCode.toString())
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }

            @Override
            public void onInterstitialShown(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialClicked(MoPubInterstitial interstitial) {

            }

            @Override
            public void onInterstitialDismissed(MoPubInterstitial interstitial) {

            }
        });
        adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(interstitial, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                interstitial.load();
                refreshCount++;
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

    void stopAutoRefresh() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
    }
}
