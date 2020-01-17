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
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;
import android.widget.FrameLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

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
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.NativeAdUnit;
import org.prebid.mobile.NativeDataAsset;
import org.prebid.mobile.NativeEventTracker;
import org.prebid.mobile.NativeImageAsset;
import org.prebid.mobile.NativeTitleAsset;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.VideoAdUnit;
import org.prebid.mobile.VideoInterstitialAdUnit;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.util.ArrayList;


public class DemoActivity extends AppCompatActivity {
    int refreshCount;
    ResultCode resultCode;

    private AdUnit adUnit;

    PublisherAdRequest gamRequest;
    private PublisherAdView gamBanner;
    private PublisherInterstitialAd gamInterstitial;

    private MoPubInterstitial mpInterstitial;
    MoPubView mpView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        refreshCount = 0;
        setContentView(R.layout.activity_demo);
        setupPrebid();
        setupAdServerAndLoadWithPrebid();
    }


    // region Prebid Setup
    private void setupPrebid() {
        String adServer = getIntent().getStringExtra(Constants.AD_SERVER_NAME);
        String pbsHostName = getIntent().getStringExtra(Constants.PBS_HOST_NAME);
        String pbsHostNameAppnexus = getString(R.string.appnexusHost);
        Host pbsHost = pbsHostName.equals(pbsHostNameAppnexus) ? Host.APPNEXUS : Host.RUBICON;
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(getApplicationContext());
        if (pbsHost.equals(Host.APPNEXUS)) {
            setupAppNexusDemand();
        } else {
            setupRubiconDemand();
        }
    }

    private void setupAppNexusDemand() {
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);
        PrebidMobile.setStoredAuctionResponse("");
        String adTypeName = getIntent().getStringExtra(Constants.AD_TYPE_NAME);
        if (adTypeName.equals(getString(R.string.adTypeBanner))) {
            Pair<Integer, Integer> size = getBannerWidthAndHeight();
            if (size.first == 300 && size.second == 250) {
                adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS, size.first, size.second);
            } else if (size.first == 320 && size.second == 50) {
                adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50_APPNEXUS, size.first, size.second);
            } else {
                adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_320x50_APPNEXUS, size.first, size.second);
            }
        } else if (adTypeName.equals(getString(R.string.adTypeInterstitial))) {
            adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS);
        } else if (adTypeName.equals(getString(R.string.adTypeBannerVideo))) {
            // todo APPNEXUS to add this demo
            adUnit = new VideoAdUnit("", 300, 250, VideoAdUnit.PlacementType.IN_BANNER);
        } else if (adTypeName.equals(getString(R.string.adTypeInterstitialVideo))) {
            // todo APPNEXUS to add this demo
            adUnit = new VideoInterstitialAdUnit("");
        } else {
            adUnit = new NativeAdUnit(Constants.PBS_CONFIG_ID_NATIVE_APPNEXUS);
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
        }
        adUnit.setAutoRefreshPeriodMillis(getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0));
    }

    private void setupRubiconDemand() {
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);
        // setup ad unit
        String adTypeName = getIntent().getStringExtra(Constants.AD_TYPE_NAME);
        if (adTypeName.equals(getString(R.string.adTypeBanner))) {
            PrebidMobile.setStoredAuctionResponse("");
            adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_RUBICON, 300, 250);
        } else if (adTypeName.equals(getString(R.string.adTypeInterstitial))) {
            PrebidMobile.setStoredAuctionResponse("");
            adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL_RUBICON);
        } else if (adTypeName.equals(getString(R.string.adTypeBannerVideo))) {
            PrebidMobile.setStoredAuctionResponse("sample_video_response");
            adUnit = new VideoAdUnit(Constants.PBS_CONFIG_ID_300x250_RUBICON, 300, 250, VideoAdUnit.PlacementType.IN_BANNER);

        } else if (adTypeName.equals(getString(R.string.adTypeInterstitialVideo))) {
            PrebidMobile.setStoredAuctionResponse("sample_video_response");
            adUnit = new VideoInterstitialAdUnit(Constants.PBS_CONFIG_ID_300x250_RUBICON);
        } else {
            PrebidMobile.setStoredAuctionResponse("");
            // todo RUBICON to add this demo
            adUnit = new NativeAdUnit("");
        }
        adUnit.setAutoRefreshPeriodMillis(getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0));
//        enableAdditionalFunctionality();
    }
    // endregion

    // region AdServer Setup
    private void setupAdServerAndLoadWithPrebid() {
        Intent intent = getIntent();
        String adTypeName = intent.getStringExtra(Constants.AD_TYPE_NAME);
        String adServerName = intent.getStringExtra(Constants.AD_SERVER_NAME);


        String adTypeBanner = getString(R.string.adTypeBanner);
        String adTypeInterstitial = getString(R.string.adTypeInterstitial);
        String adTypeBannerVideo = getString(R.string.adTypeBannerVideo);
        String adTypeInterstitialVideo = getString(R.string.adTypeInterstitialVideo);
        String adTypeInBannerNative = getString(R.string.adTypeInBannerNative);

        String adServerAdManager = getString(R.string.adServerAdManager);
        String adServerMoPub = getString(R.string.adServerMoPub);
        if (adTypeName.equals(adTypeBanner)) {
            if (adServerName.equals(adServerAdManager)) {
                setupGAMBannerAndLoadWithPrebid();
            } else if (adServerName.equals(adServerMoPub)) {
                setupMPBannerAndLoadWithPrebid();
            }

        } else if (adTypeName.equals(adTypeInterstitial)) {
            if (adServerName.equals(adServerAdManager)) {
                setupGAMInterstitialAndLoadWithPrebid();
            } else if (adServerName.equals(adServerMoPub)) {
                setupMPInterstitialAndLoadwithPrebid();
            }

        } else if (adTypeName.equals(adTypeBannerVideo)) {
            if (adServerName.equals(adServerAdManager)) {
                setupGAMBannerVideoAndLoadWithPrebid();
            } else if (adServerName.equals(adServerMoPub)) {
                Toast.makeText(getApplicationContext(), adServerName + " doest not support " + adTypeName, Toast.LENGTH_SHORT).show();
                finish();
            }
        } else if (adTypeName.equals(adTypeInterstitialVideo)) {
            if (adServerName.equals(adServerAdManager)) {
                setupGAMIntestitialVideoAndLoadWithPrebid();
            } else if (adServerName.equals(adServerMoPub)) {
                setupMPInterstitialVideoAndLoadWithPrebid();
            }
        } else if (adTypeInBannerNative.equals(adTypeName)) {
            if (adServerAdManager.equals(adServerName)) {
                setupGAMInBannerNativeAndLoadWithPrebid();
            } else if (adServerMoPub.equals(adServerName)) {
                setupMPInBannerNativeAndLoadWithPrebid();
            }
        }
    }

    private void setupGAMBannerAndLoadWithPrebid() {
        Pair<Integer, Integer> size = getBannerWidthAndHeight();
        gamBanner = new PublisherAdView(this);
        gamBanner.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES);
        gamBanner.setAdSizes(new AdSize(size.first, size.second));
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        adFrame.addView(gamBanner);

        gamBanner.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();

                AdViewUtils.findPrebidCreativeSize(gamBanner, new AdViewUtils.PbFindSizeListener() {
                    @Override
                    public void success(int width, int height) {
                        gamBanner.setAdSizes(new AdSize(width, height));

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
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                gamBanner.loadAd(request);
                refreshCount++;
            }
        });
    }

    private void setupMPBannerAndLoadWithPrebid() {
        Pair<Integer, Integer> size = getBannerWidthAndHeight();
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        mpView = new MoPubView(this);
        if (size.first == 300 && size.second == 250) {
            mpView.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
        } else {
            mpView.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_320x50_APPNEXUS);
        }
        mpView.setMinimumWidth(size.first);
        mpView.setMinimumHeight(size.second);
        adFrame.addView(mpView);
        adUnit.fetchDemand(mpView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                mpView.loadAd();
                refreshCount++;
            }
        });
    }

    private void setupMPInterstitialAndLoadwithPrebid() {
        mpInterstitial = new MoPubInterstitial(this, Constants.MOPUB_INTERSTITIAL_ADUNIT_ID);
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
        adUnit.fetchDemand(mpInterstitial, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                mpInterstitial.load();
                refreshCount++;
            }
        });
    }

    private void setupGAMInterstitialAndLoadWithPrebid() {
        gamInterstitial = new PublisherInterstitialAd(this);
        gamInterstitial.setAdUnitId(Constants.DFP_INTERSTITIAL_ADUNIT_ID_APPNEXUS_2);
        gamInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                gamInterstitial.show();
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
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        gamRequest = builder.build();
        adUnit.fetchDemand(gamRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                gamInterstitial.loadAd(gamRequest);
                refreshCount++;
            }
        });
    }

    private void setupGAMBannerVideoAndLoadWithPrebid() {
        gamBanner = new PublisherAdView(this);
        gamBanner.setAdUnitId(Constants.DFP_BANNER_VIDEO_ADUNIT_ID_300x250_RUBICON);
        gamBanner.setAdSizes(new AdSize(300, 250));
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        final PublisherAdRequest request = builder.build();
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                gamBanner.loadAd(request);
                refreshCount++;
            }
        });
    }

    private void setupGAMIntestitialVideoAndLoadWithPrebid() {
        gamInterstitial = new PublisherInterstitialAd(this);
        gamInterstitial.setAdUnitId(Constants.DFP_INTERSTITIAL_VIDEO_ADUNIT_ID_RUBICON);
        gamInterstitial.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                gamInterstitial.show();
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
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        gamRequest = builder.build();
        adUnit.fetchDemand(gamRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                gamInterstitial.loadAd(gamRequest);
                refreshCount++;
            }
        });
    }

    private void setupMPInterstitialVideoAndLoadWithPrebid() {
        mpInterstitial = new MoPubInterstitial(this, Constants.MOPUB_INTERSTITIAL_VIDEO_ADUNIT_ID_RUBICON);
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
        adUnit.fetchDemand(mpInterstitial, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                mpInterstitial.load();
                refreshCount++;
            }
        });
    }

    private void setupMPInBannerNativeAndLoadWithPrebid() {
        final FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        mpView = new MoPubView(this);
        mpView.setAdUnitId(Constants.MOPUB_IN_BANNER_NATIVE_ADUNIT_ID_APPNEXUS);
        mpView.setBannerAdListener(new MoPubView.BannerAdListener() {
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
        mpView.setAutorefreshEnabled(false);
        adUnit.fetchDemand(mpView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                mpView.loadAd(MoPubView.MoPubAdSize.MATCH_VIEW);
                DemoActivity.this.mpView = mpView;
                refreshCount++;
            }
        });
    }

    private void setupGAMInBannerNativeAndLoadWithPrebid() {
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
        gamRequest = builder.build();
        adUnit.fetchDemand(gamRequest, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                DemoActivity.this.resultCode = resultCode;
                nativeAdView.loadAd(gamRequest);
                DemoActivity.this.gamRequest = gamRequest;
                refreshCount++;
            }
        });
    }
    // endregion


    // region helper methods
    private void enableAdditionalFunctionality() {
        enableCOPPA();
        addFirstPartyData();
        setStoredResponse();
        setRequestTimeoutMillis();
    }

    private void enableCOPPA() {
        TargetingParams.setSubjectToCOPPA(true);
    }

    private void addFirstPartyData() {
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

    private Pair<Integer, Integer> getBannerWidthAndHeight() {
        String adSize = getIntent().getStringExtra(Constants.AD_SIZE_NAME);
        if (TextUtils.isEmpty(adSize)) {
            return new Pair<>(-1, -1);
        }
        String[] wAndH = adSize.split("x");
        int width = Integer.valueOf(wAndH[0]);
        int height = Integer.valueOf(wAndH[1]);
        return new Pair<>(width, height);

    }

    // endregion

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
        if (mpInterstitial != null) {
            mpInterstitial.destroy();
            mpInterstitial = null;
        }
        if (mpView != null) {
            mpView.destroy();
            mpView = null;
        }
        if (gamBanner != null) {
            gamBanner.destroy();
            gamBanner = null;
        }
        gamInterstitial = null;
    }

    @VisibleForTesting
    void stopAutoRefresh() {
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
        }
    }
}
