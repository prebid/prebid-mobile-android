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
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdCallback;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;
import com.mopub.common.MediationSettings;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.common.SdkConfiguration;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;
import com.mopub.mobileads.MoPubRewardedVideoManager;
import com.mopub.mobileads.MoPubRewardedVideos;
import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.InterstitialAdUnit;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.RewardedVideoAdUnit;
import org.prebid.mobile.Signals;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.Util;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.VideoBaseAdUnit;
import org.prebid.mobile.VideoInterstitialAdUnit;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DemoActivity extends AppCompatActivity implements MoPubRewardedVideoListener {
    int refreshCount;
    AdUnit adUnit;
    ResultCode resultCode;

    PublisherAdRequest request;
    MoPubView adView;

    private static final String MP_ADUNITID_REWARDED = "066483fc44bf4793b4e275522ef7c428";
    private PublisherAdView amBanner;
    private PublisherInterstitialAd amInterstitial;

    private MoPubInterstitial mpInterstitial;
    private RewardedAd amRewardedAd;

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
        String adTypeInBannerNative = getString(R.string.adTypeInBannerNative);
        String adTypeRewardedVideo = getString(R.string.adTypeRewardedVideo);

        String adServerAdManager = getString(R.string.adServerAdManager);
        String adServerMoPub = getString(R.string.adServerMoPub);

        if (adTypeName.equals(adTypeBanner)) {
            String adSizeName = intent.getStringExtra(Constants.AD_SIZE_NAME);
            int width = 0;
            int height = 0;

            String[] wAndH = adSizeName.split("x");
            width = Integer.valueOf(wAndH[0]);
            height = Integer.valueOf(wAndH[1]);

//            enableAdditionalFunctionality(adUnit);

            if (adServerName.equals(adServerAdManager)) {
                setupAndLoadAMBanner(width, height);
            } else if (adServerName.equals(adServerMoPub)) {
                setupAndLoadMPBanner(width, height);
            }

        } else if (adTypeName.equals(adTypeInterstitial)) {
            //Advanced interstitial support
//            adUnit = new InterstitialAdUnit("1001-1", 50, 70);

//            enableAdditionalFunctionality(adUnit);

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
        } else if (adTypeInBannerNative.equals(adTypeName)) {
            PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
            PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);

            adUnit = new NativeAdUnit(Constants.PBS_CONFIG_ID_NATIVE_APPNEXUS);
            if (adServerAdManager.equals(adServerName)) {
                createDFPNative();
            } else if (adServerMoPub.equals(adServerName)) {
                createMoPubNative();
            }
        } else if (adTypeRewardedVideo.equals(adTypeName)) {
            if (adServerAdManager.equals(adServerName)) {
                setupAndLoadAMRewardedVideo();
            } else if (adServerMoPub.equals(adServerName)) {
                setupAndLoadMPRewardedVideo();
            }
        }

    }

    void createMoPubNative() {
        final FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView = new MoPubView(this);
        adView.setAdUnitId(Constants.MOPUB_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS);
        adView.setBannerAdListener(new MoPubView.BannerAdListener() {
            @Override
            public void onBannerLoaded(final MoPubView banner) {
                adFrame.addView(banner);
            }

            @Override
            public void onBannerFailed(MoPubView banner, MoPubErrorCode errorCode) {
                LogUtil.d("Banner failed " + errorCode);
            }

            @Override
            public void onBannerClicked(MoPubView banner) {

            }

            @Override
            public void onBannerExpanded(MoPubView banner) {

            }

            @Override
            public void onBannerCollapsed(MoPubView banner) {

            }
        });
        adView.setAutorefreshEnabled(false);
        NativeAdUnit nativeAdUnit = (NativeAdUnit) adUnit;
        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.JS);
        try {
            NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            nativeAdUnit.addEventTracker(tracker);
        } catch (Exception e) {
            e.printStackTrace();
        }
        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(90);
        title.setRequired(true);
        nativeAdUnit.addAsset(title);
        NativeImageAsset icon = new NativeImageAsset();
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setWMin(20);
        icon.setHMin(20);
        icon.setRequired(true);
        nativeAdUnit.addAsset(icon);
        NativeImageAsset image = new NativeImageAsset();
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setHMin(200);
        image.setWMin(200);
        image.setRequired(true);
        nativeAdUnit.addAsset(image);
        NativeDataAsset data = new NativeDataAsset();
        data.setLen(90);
        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        data.setRequired(true);
        nativeAdUnit.addAsset(data);
        NativeDataAsset body = new NativeDataAsset();
        body.setRequired(true);
        body.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        nativeAdUnit.addAsset(body);
        NativeDataAsset cta = new NativeDataAsset();
        cta.setRequired(true);
        cta.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        nativeAdUnit.addAsset(cta);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        nativeAdUnit.setAutoRefreshPeriodMillis(millis);
        nativeAdUnit.fetchDemand(adView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                adView.loadAd(MoPubView.MoPubAdSize.MATCH_VIEW);
                DemoActivity.this.adView = adView;
                refreshCount++;
            }
        });
    }

    void createDFPNative() {
        FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        final PublisherAdView nativeAdView = new PublisherAdView(this);
        nativeAdView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                LogUtil.d("ad loaded");
            }
        });
        nativeAdView.setAdUnitId(Constants.DFP_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS);
        nativeAdView.setAdSizes(AdSize.FLUID);
        adFrame.addView(nativeAdView);
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        request = builder.build();
        NativeAdUnit nativeAdUnit = (NativeAdUnit) adUnit;
        nativeAdUnit.setContextType(NativeAdUnit.CONTEXT_TYPE.SOCIAL_CENTRIC);
        nativeAdUnit.setPlacementType(NativeAdUnit.PLACEMENTTYPE.CONTENT_FEED);
        nativeAdUnit.setContextSubType(NativeAdUnit.CONTEXTSUBTYPE.GENERAL_SOCIAL);
        ArrayList<NativeEventTracker.EVENT_TRACKING_METHOD> methods = new ArrayList<>();
        methods.add(NativeEventTracker.EVENT_TRACKING_METHOD.IMAGE);

        try {
            NativeEventTracker tracker = new NativeEventTracker(NativeEventTracker.EVENT_TYPE.IMPRESSION, methods);
            nativeAdUnit.addEventTracker(tracker);
        } catch (Exception e) {
            e.printStackTrace();

        }
        NativeTitleAsset title = new NativeTitleAsset();
        title.setLength(90);
        title.setRequired(true);
        nativeAdUnit.addAsset(title);
        NativeImageAsset icon = new NativeImageAsset();
        icon.setImageType(NativeImageAsset.IMAGE_TYPE.ICON);
        icon.setWMin(20);
        icon.setHMin(20);
        icon.setRequired(true);
        nativeAdUnit.addAsset(icon);
        NativeImageAsset image = new NativeImageAsset();
        image.setImageType(NativeImageAsset.IMAGE_TYPE.MAIN);
        image.setHMin(200);
        image.setWMin(200);
        image.setRequired(true);
        nativeAdUnit.addAsset(image);
        NativeDataAsset data = new NativeDataAsset();
        data.setLen(90);
        data.setDataType(NativeDataAsset.DATA_TYPE.SPONSORED);
        data.setRequired(true);
        nativeAdUnit.addAsset(data);
        NativeDataAsset body = new NativeDataAsset();
        body.setRequired(true);
        body.setDataType(NativeDataAsset.DATA_TYPE.DESC);
        nativeAdUnit.addAsset(body);
        NativeDataAsset cta = new NativeDataAsset();
        cta.setRequired(true);
        cta.setDataType(NativeDataAsset.DATA_TYPE.CTATEXT);
        nativeAdUnit.addAsset(cta);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        nativeAdUnit.setAutoRefreshPeriodMillis(millis);
        nativeAdUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                nativeAdView.loadAd(request);
                DemoActivity.this.request = request;
                refreshCount++;
            }
        });
    }

    //Banner
    void setupAndLoadAMBanner(int width, int height) {
        setupPBRubiconBanner(width, height);
        setupAMRubiconBanner(width, height);
        loadAMBanner();
    }

    void setupAndLoadMPBanner(int width, int height) {
        setupPBRubiconBanner(width, height);
        setupMPRubiconBanner(width, height);
        loadMPBanner();
    }

    //setup PB
    private void setupPBAppNexusBanner(int width, int height) {

        String accountId = Constants.PBS_CONFIG_ID_320x50_APPNEXUS;
        if (width == 300 && height == 250) {
            accountId = Constants.PBS_CONFIG_ID_300x250_APPNEXUS;
        }

        setupPBBanner(Host.APPNEXUS, Constants.PBS_ACCOUNT_ID_APPNEXUS, accountId, "", width, height);
    }

    private void setupPBRubiconBanner(int width, int height) {

        setupPBBanner(Host.RUBICON, Constants.PBS_ACCOUNT_ID_RUBICON, Constants.PBS_CONFIG_ID_300x250_RUBICON, Constants.PBS_STORED_RESPONSE_300x250_RUBICON, width, height);
    }

    private void setupPBBanner(Host host, String accountId, String configId, @NonNull String storedResponse, int width, int height) {

        setupPB(host, accountId, storedResponse);

        adUnit = new BannerAdUnit(configId, width, height);

    }

    private void setupPB(Host host, String accountId, @NonNull String storedResponse) {
        PrebidMobile.setPrebidServerHost(host);
        PrebidMobile.setPrebidServerAccountId(accountId);
        PrebidMobile.setStoredAuctionResponse(storedResponse);
    }

    //Setup AdServer
    private void setupAMAppNexusBanner(int width, int height) {
        setupAMBanner(width, height, Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS);
    }

    private void setupAMRubiconBanner(int width, int height) {
        setupAMBanner(width, height, Constants.DFP_BANNER_ADUNIT_ID_300x250_RUBICON);
    }

    private void setupAMBanner(int width, int height, String adUnitId) {
        amBanner = new PublisherAdView(this);
        amBanner.setAdUnitId(adUnitId);
        amBanner.setAdSizes(new AdSize(width, height));
    }

    void setupMPAppNexusBanner(int width, int height) {
        String adUnitId = Constants.MOPUB_BANNER_ADUNIT_ID_320x50_APPNEXUS;
        if (width == 300 && height == 250) {
            adUnitId = Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS;
        }

        setupMPBanner(adUnitId, width, height);
    }

    void setupMPRubiconBanner(int width, int height) {
        setupMPBanner(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON, width, height);
    }

    void setupMPBanner(String adUnitId, int width, int height) {
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adView = new MoPubView(this);
        adView.setAdUnitId(adUnitId);

        adView.setMinimumWidth(width);
        adView.setMinimumHeight(height);
        adFrame.addView(adView);
    }

    //Load
    private void loadAMBanner() {
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

    void loadMPBanner() {
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

    //Banner VAST
    void setupAndLoadAMBannerVAST() {
        int width = 300;
        int height = 250;

        setupPBRubiconBannerVAST(width, height);
        setupAMRubiconBannerVAST(width, height);
        loadAMBanner();
    }

    private void setupPBRubiconBannerVAST(int width, int height) {

        setupPB(Host.RUBICON, Constants.PBS_ACCOUNT_ID_RUBICON, Constants.PBS_STORED_RESPONSE_VAST_RUBICON);

        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Arrays.asList("video/mp4"));

        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0));
        // parameters.setProtocols(Arrays.asList(new Signals.Protocols(2)));

        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOff));
        // parameters.setPlaybackMethod(Arrays.asList(new Signals.PlaybackMethod(2)));

        parameters.setPlacement(Signals.Placement.InBanner);
        // parameters.setPlacement(new Signals.Placement(2));

        VideoAdUnit adUnit = new VideoAdUnit("1001-1", width, height);
        adUnit.setParameters(parameters);

        this.adUnit = adUnit;
    }

    private void setupAMRubiconBannerVAST(int width, int height) {
        setupAMBanner(width, height, Constants.DFP_VAST_ADUNIT_ID_RUBICON);
    }

    //Interstitial
    void setupAndLoadAMInterstitial() {
        setupPBRubiconInterstitial();
        setupAMRubiconInterstitial();
        loadAMInterstitial();
    }

    void setupAndLoadMPInterstitial() {
        setupPBRubiconInterstitial();
        setupMPRubiconInterstitial();
        loadMPInterstitial();
    }

    //Setup PB
    private void setupPBAppNexusInterstitial() {
        setupPBInterstitial(Host.APPNEXUS, Constants.PBS_ACCOUNT_ID_APPNEXUS, Constants.PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS, "");
    }

    private void setupPBRubiconInterstitial() {
        setupPBInterstitial(Host.RUBICON, Constants.PBS_ACCOUNT_ID_RUBICON, Constants.PBS_CONFIG_ID_INTERSTITIAL_RUBICON, Constants.PBS_STORED_RESPONSE_300x250_RUBICON);
    }

    private void setupPBInterstitial(Host host, String accountId, String configId, @NonNull String storedResponse) {
        setupPB(host, accountId, storedResponse);

        adUnit = new InterstitialAdUnit(configId);
    }

    //Setup AdServer
    private void setupAMAppNexusInterstitial() {
        setupAMInterstitial(Constants.DFP_INTERSTITIAL_ADUNIT_ID_APPNEXUS);
    }

    private void setupAMRubiconInterstitial() {
        setupAMInterstitial(Constants.DFP_INTERSTITIAL_ADUNIT_ID_RUBICON);
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
                builder.setTitle("Failed to load AdManager interstitial ad")
                        .setMessage("Error code: " + i)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    private void setupMPAppNexusInterstitial() {
        setupMPInterstitial(Constants.MOPUB_INTERSTITIAL_ADUNIT_ID_APPNEXUS);
    }

    private void setupMPRubiconInterstitial() {
        setupMPInterstitial(Constants.MOPUB_INTERSTITIAL_ADUNIT_ID_RUBICON);
    }

    private void setupMPInterstitial(String adUnitId) {
        mpInterstitial = new MoPubInterstitial(this, adUnitId);
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

    //Load
    private void loadAMInterstitial() {
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        request = builder.build();
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                amInterstitial.loadAd(request);
                refreshCount++;
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

    //Interstitial VAST
    void setupAndLoadMPInterstitialVAST() {
        setupPBRubiconInterstitialVAST();
        setupMPRubiconInterstitialVAST();
        loadMPInterstitial();
    }

    private void setupAndLoadAMInterstitialVAST() {
        setupPBRubiconInterstitialVAST();
        setupAMRubiconInterstitialVAST();
        loadAMInterstitial();
    }

    //Setup PB
    private void setupPBRubiconInterstitialVAST() {
        setupPB(Host.RUBICON, Constants.PBS_ACCOUNT_ID_RUBICON, Constants.PBS_STORED_RESPONSE_VAST_RUBICON);

        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Arrays.asList("video/mp4"));

        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0));
        // parameters.setProtocols(Arrays.asList(new Signals.Protocols(2)));

        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOff));
        // parameters.setPlaybackMethod(Arrays.asList(new Signals.PlaybackMethod(2)));

        VideoInterstitialAdUnit adUnit = new VideoInterstitialAdUnit("1001-1");
        adUnit.setParameters(parameters);

        this.adUnit = adUnit;
    }

    //Setup AdServer
    private void setupAMRubiconInterstitialVAST() {
        setupAMInterstitial(Constants.DFP_VAST_ADUNIT_ID_RUBICON);
    }

    private void setupMPRubiconInterstitialVAST() {
        setupMPInterstitial("723dd84529b04075aa003a152ede0c4b");
    }

    //Rewarded Video
    private void setupAndLoadAMRewardedVideo() {
        setupPBRubiconRewardedVideo();
        setupAMRubiconRewardedVideo();
        loadAMRewardedVideo();
    }

    private void setupAndLoadMPRewardedVideo() {
        setupPBRubiconRewardedVideo();
        setupMPRubiconRewardedVideo();
        loadMPRewardedVideo();

    }

    //Setup PB
    private void setupPBRubiconRewardedVideo() {
        setupPB(Host.RUBICON, Constants.PBS_ACCOUNT_ID_RUBICON, Constants.PBS_STORED_RESPONSE_VAST_RUBICON);

        VideoBaseAdUnit.Parameters parameters = new VideoBaseAdUnit.Parameters();
        parameters.setMimes(Arrays.asList("video/mp4"));

        parameters.setProtocols(Arrays.asList(Signals.Protocols.VAST_2_0));
        // parameters.setProtocols(Arrays.asList(new Signals.Protocols(2)));

        parameters.setPlaybackMethod(Arrays.asList(Signals.PlaybackMethod.AutoPlaySoundOff));
        // parameters.setPlaybackMethod(Arrays.asList(new Signals.PlaybackMethod(2)));

        RewardedVideoAdUnit adUnit = new RewardedVideoAdUnit("1001-1");
        adUnit.setParameters(parameters);

        this.adUnit = adUnit;

    }

    //Setup AdServer
    private void setupAMRubiconRewardedVideo() {
        amRewardedAd = new RewardedAd(this, "/5300653/test_adunit_vast_rewarded-video_pavliuchyk");
    }

    private void setupMPRubiconRewardedVideo() {
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder(MP_ADUNITID_REWARDED)
                .build();
        MoPub.initializeSdk(this, sdkConfiguration, null);

        MoPubRewardedVideos.setRewardedVideoListener(this);
    }

    //Load
    private void loadAMRewardedVideo() {

        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        request = builder.build();
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                amRewardedAd.loadAd(request, new RewardedAdLoadCallback() {
                    @Override
                    public void onRewardedAdLoaded() {
                        // Ad successfully loaded.

                        if (amRewardedAd.isLoaded()) {
                            amRewardedAd.show(DemoActivity.this, new RewardedAdCallback() {
                                @Override
                                public void onRewardedAdOpened() {
                                    // Ad opened.
                                }

                                @Override
                                public void onRewardedAdClosed() {
                                    // Ad closed.
                                }

                                @Override
                                public void onUserEarnedReward(@NonNull RewardItem reward) {
                                    // User earned reward.
                                }

                                @Override
                                public void onRewardedAdFailedToShow(int errorCode) {
                                    // Ad failed to display.
                                }
                            });
                        }
                    }

                    @Override
                    public void onRewardedAdFailedToLoad(int errorCode) {
                        // Ad failed to load.
                    }
                });
            }
        });
    }

    private void loadMPRewardedVideo() {

        final Map<String, String> keywordsMap = new HashMap<>();
        adUnit.fetchDemand(keywordsMap, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {

                MoPubRewardedVideoManager.RequestParameters parameters = new MoPubRewardedVideoManager.RequestParameters(Util.convertMapToMoPubKeywords(keywordsMap));
                MoPubRewardedVideos.loadRewardedVideo(MP_ADUNITID_REWARDED, parameters, (MediationSettings) null);
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

    //MoPub Rewarded Video
    @Override
    public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
        if (MoPubRewardedVideos.hasRewardedVideo(MP_ADUNITID_REWARDED)) {
            MoPubRewardedVideos.showRewardedVideo(MP_ADUNITID_REWARDED);
        }
    }

    @Override
    public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {
        LogUtil.d("onRewardedVideoLoadFailure:" + errorCode);
    }

    @Override
    public void onRewardedVideoStarted(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {

    }

    @Override
    public void onRewardedVideoClicked(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoClosed(@NonNull String adUnitId) {

    }

    @Override
    public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {

    }

    void stopAutoRefresh() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
    }

    private void enableAdditionalFunctionality(AdUnit adUnit) {
        enableCOPPA();
        addFirstPartyData(adUnit);
        setStoredResponse();
        setRequestTimeoutMillis();
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
}
