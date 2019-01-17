package org.prebid.mobile;


import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.google.android.gms.ads.doubleclick.PublisherInterstitialAd;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import static org.prebid.mobile.Constants.MOPUB_BANNER_ADUNIT_ID_300x250;

public class DemoActivity extends AppCompatActivity {
    AdUnit adUnit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Intent intent = getIntent();
        if (intent.getStringExtra(Constants.AD_SERVER_NAME).equals("DFP") && intent.getStringExtra(Constants.AD_TYPE_NAME).equals("Banner")) {
            createDFPBanner(intent.getStringExtra(Constants.AD_SIZE_NAME));
        } else if (intent.getStringExtra(Constants.AD_SERVER_NAME).equals("DFP") && intent.getStringExtra(Constants.AD_TYPE_NAME).equals("Interstitial")) {
            createDFPInterstitial();
        } else if (intent.getStringExtra(Constants.AD_SERVER_NAME).equals("MoPub") && intent.getStringExtra(Constants.AD_TYPE_NAME).equals("Banner")) {
            createMoPubBanner(intent.getStringExtra(Constants.AD_SIZE_NAME));
        } else if (intent.getStringExtra(Constants.AD_SERVER_NAME).equals("MoPub") && intent.getStringExtra(Constants.AD_TYPE_NAME).equals("Interstitial")) {
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
            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_300x250);
        } else {
            dfpAdView.setAdUnitId("asizelessadunitidhaha");
        }
        dfpAdView.setAdSizes(new AdSize(width, height));
        adFrame.addView(dfpAdView);
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();

        final PublisherAdRequest request = builder.build();

        //region PrebidMobile Mobile API 1.0 usage
        adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND, width, height);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                dfpAdView.loadAd(request);
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
        adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS_DEMAND);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        final PublisherAdRequest request = builder.build();
        adUnit.fetchDemand(request, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                LogUtil.i(resultCode.toString());
                interstitialAd.loadAd(request);
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
        adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_300x250);
        adView.setMinimumWidth(width);
        adView.setMinimumHeight(height);
        adFrame.addView(adView);
        adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND, 300, 250);
        adUnit.setAutoRefreshPeriodMillis(getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0));
        adUnit.fetchDemand(adView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                adView.loadAd();
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
                builder.setTitle("Failed to load DFP interstitial ad")
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
        adUnit = new InterstitialAdUnit(Constants.PBS_CONFIG_ID_INTERSTITIAL_APPNEXUS_DEMAND);
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(interstitial, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                LogUtil.i(resultCode.toString());
                interstitial.load();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefersh();
            adUnit = null;
        }
    }
}
