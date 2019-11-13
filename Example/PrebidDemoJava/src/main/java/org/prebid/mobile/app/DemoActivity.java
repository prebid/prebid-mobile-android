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
import android.widget.Toast;

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
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.VideoInterstitialAdUnit;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import static org.prebid.mobile.app.Constants.MOPUB_BANNER_ADUNIT_ID_300x250;
import static org.prebid.mobile.app.Constants.MOPUB_BANNER_ADUNIT_ID_320x50;

public class DemoActivity extends AppCompatActivity {
    int refreshCount;
    AdUnit adUnit;
    ResultCode resultCode;

    private PublisherAdView amBanner;
    private PublisherInterstitialAd amInterstitial;

    private MoPubInterstitial mpInterstitial;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshCount = 0;
        setContentView(R.layout.activity_demo);
        Intent intent = getIntent();

        String adTypeName = intent.getStringExtra(Constants.AD_TYPE_NAME);
        String adServerName = intent.getStringExtra(Constants.AD_SERVER_NAME);

        String adTypeBanner = getString(R.string.adTypeBanner);
        String adTypeInterstitial = getString(R.string.adTypeInterstitial);
        String adTypeBannerVideo = getString(R.string.adTypeBannerVideo);
        String adTypeInterstitialVideo = getString(R.string.adTypeInterstitialVideo);

        String adServerAdManager = getString(R.string.adServerAdManager);
        String adServerMoPub = getString(R.string.adServerMoPub);

        if (adTypeName.equals(adTypeBanner)) {
            String adSizeName = intent.getStringExtra(Constants.AD_SIZE_NAME);
            int width = 0;
            int height = 0;

            String[] wAndH = adSizeName.split("x");
            width = Integer.valueOf(wAndH[0]);
            height = Integer.valueOf(wAndH[1]);

            enableAdditionalFunctionality(adUnit);

            if (adServerName.equals(adServerAdManager)) {
                setupAndLoadAMBanner(width, height);
            } else if (adServerName.equals(adServerMoPub)) {
                setupAndLoadMPBanner(width, height);
            }

        } else if (adTypeName.equals(adTypeInterstitial)) {
            //Advanced interstitial support
//            adUnit = new InterstitialAdUnit("1001-1", 50, 70);

            enableAdditionalFunctionality(adUnit);

            if (adServerName.equals(adServerAdManager)) {
                setupAndLoadAMInterstitial();
            } else if (adServerName.equals(adServerMoPub)) {
                setupAndLoadMPInterstitial();
            }

        } else if (adTypeName.equals(adTypeBannerVideo)) {

            if (adServerName.equals(adServerAdManager)) {
                setupAndLoadAMBannerVAST();
            } else if (adServerName.equals(adServerMoPub)) {
                Toast.makeText(getApplicationContext(), adServerName + " doest not support " + adTypeName, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (adTypeName.equals(adTypeInterstitialVideo)) {

            if (adServerName.equals(adServerAdManager)) {
                setupAndLoadAMInterstitialVAST();
            } else if (adServerName.equals(adServerMoPub)) {
                setupAndLoadMPInterstitialVAST();
            }
        }
    }

    void setupAndLoadAMBanner(int width, int height) {
        setupPBBanner(width, height);
        setupAMBanner(width, height);
        loadBanner();
    }

    void setupAndLoadAMBannerVAST() {
        setupPBBannerVAST();
        setupAMBannerVAST();
        loadBanner();
    }

    void setupAndLoadMPInterstitial() {
        setupPBInterstitial();
        setupMPInterstitial();
        loadMPInterstitial();

    }

    void setupAndLoadMPInterstitialVAST() {
        setupPBInterstitialVAST();
        setupMPInterstitialVAST();
        loadMPInterstitial();
    }

    void setupAndLoadAMInterstitial() {
        setupPBInterstitial();
        setupAMInterstitial();
        loadInterstitial();
    }

    private void setupAndLoadAMInterstitialVAST() {
        setupPBInterstitialVAST();
        setupAMInterstitialVAST();
        loadInterstitial();
    }

    private void enableAdditionalFunctionality(AdUnit adUnit) {
//        enableCOPPA();
//        addFirstPartyData(adUnit);
//        setStoredResponse();
//        setRequestTimeoutMillis();
    }

    private void enableCOPPA() {
        TargetingParams.setSubjectToCOPPA(true);
    }

    private void addFirstPartyData(AdUnit adUnit) {
        //Access Control List
        TargetingParams.addBidderToAccessControlList(TargetingParams.BIDDER_NAME_APP_NEXUS);

        //global user data
        TargetingParams.addUserData("globalUserDataKey1", "globalUserDataValue1");

        //global context data
        TargetingParams.addContextData("globalContextDataKey1", "globalContextDataValue1");

        //adunit context data
        adUnit.addContextData("adunitContextDataKey1", "adunitContextDataValue1");

        //global context keywords
        TargetingParams.addContextKeyword("globalContextKeywordValue1");
        TargetingParams.addContextKeyword("globalContextKeywordValue2");

        //global user keywords
        TargetingParams.addUserKeyword("globalUserKeywordValue1");
        TargetingParams.addUserKeyword("globalUserKeywordValue2");

        //adunit context keywords
        adUnit.addContextKeyword("adunitContextKeywordValue1");
        adUnit.addContextKeyword("adunitContextKeywordValue2");

    }

    private void setStoredResponse() {
        PrebidMobile.setStoredAuctionResponse("111122223333");
    }

    private void setRequestTimeoutMillis() {
        PrebidMobile.setTimeoutMillis(5_000);
    }

    //Banner
    private void setupPBBanner(int width, int height) {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID);
        PrebidMobile.setStoredAuctionResponse("");

        if (width == 300 && height == 250) {
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250, width, height);
        } else if (width == 320 && height == 50) {
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height);
        } else {
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50, width, height);
        }
    }

    private void setupPBBannerVAST() {
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setStoredAuctionResponse("sample_video_response");

        adUnit = new VideoAdUnit("1001-1", 300, 250, VideoAdUnit.PlacementType.IN_BANNER);
    }

    private void setupAMBanner(int width, int height) {
        setupAMBanner(width, height, Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES);
    }

    private void setupAMBannerVAST() {
        setupAMBanner(300, 250, "/5300653/test_adunit_vast_pavliuchyk");
    }

    private void setupAMBanner(int width, int height, String id) {
        amBanner = new PublisherAdView(this);
        amBanner.setAdUnitId(id);
        amBanner.setAdSizes(new AdSize(width, height));
    }

    private void loadBanner() {
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adFrame.addView(amBanner);

        amBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                AdViewUtils.findPrebidCreativeSize(amBanner, new AdViewUtils.PbFindSizeListener() {
                    @Override
                    public void success(int width, int height) {
                        amBanner.setAdSizes(new AdSize(width, height));

                    }

                    @Override
                    public void failure(@NonNull PbFindSizeError error) {
                        Log.d("MyTag", "error: " + error);
                    }
                });

            }
        });

        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        final PublisherAdRequest request = builder.build();
        //region PrebidMobile Mobile API 1.0 usage
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                amBanner.loadAd(request);
                refreshCount++;
            }
        });
    }

    // Interstitial
    private void setupPBInterstitial() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID);
        PrebidMobile.setStoredAuctionResponse("");

        adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL);
    }

    private void setupPBInterstitialVAST() {
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setStoredAuctionResponse("sample_video_response");

        adUnit = new VideoInterstitialAdUnit("1001-1");
    }

    private void setupAMInterstitial() {
        setupAMInterstitial(Constants.DFP_INTERSTITIAL_ADUNIT_ID);
    }

    private void setupAMInterstitialVAST() {
        setupAMInterstitial("/5300653/test_adunit_vast_pavliuchyk");
    }

    private void setupAMInterstitial(String id) {
        amInterstitial = new PublisherInterstitialAd(this);
        amInterstitial.setAdUnitId(id);
        amInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                amInterstitial.show();
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
    }

    private void loadInterstitial() {
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        final PublisherAdRequest request = builder.build();
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                amInterstitial.loadAd(request);
                refreshCount++;
            }
        });
    }

    void setupAndLoadMPBanner(int width, int height) {
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        final MoPubView adView = new MoPubView(this);
        if (width == 300 && height == 250) {
            adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_300x250);
        } else {
            adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_320x50);
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

    private void setupMPInterstitial() {
        setupMPInterstitial(Constants.MOPUB_INTERSTITIAL_ADUNIT_ID);
    }

    private void setupMPInterstitialVAST() {
        setupMPInterstitial("723dd84529b04075aa003a152ede0c4b");
    }

    private void setupMPInterstitial(String id) {
        mpInterstitial = new MoPubInterstitial(this, id);
        mpInterstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
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
    }

    private void loadMPInterstitial() {
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(mpInterstitial, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                mpInterstitial.load();
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
