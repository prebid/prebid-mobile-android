package org.prebid.mobile;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubView;

import static org.prebid.mobile.Constants.MOPUB_BANNER_ADUNIT_ID_300x250;

public class DemoActivity extends AppCompatActivity {
    int count = 0;

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
//        if (width == 300 && height == 250) {
//            dfpAdView.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_300x250);
//        } else {
//            dfpAdView.setAdUnitId("asizelessadunitidhaha");
//        }
        dfpAdView.setAdUnitId("/19968336/WeiTestMbpbJS");
        dfpAdView.setAdSizes(new AdSize(width, height), new AdSize(320,50));
        dfpAdView.setAdListener(new AdListener() {

            @Override
            public void onAdFailedToLoad(int i) {
                super.onAdFailedToLoad(i);
                count++;
                LogUtil.d("ad failed " + count);
            }

            @Override
            public void onAdLoaded() {
                super.onAdLoaded();
                count++;
                LogUtil.d("ad loaded " + count);
            }
        });
        adFrame.addView(dfpAdView);
        final PublisherAdRequest.Builder builder = new PublisherAdRequest.Builder();
        builder.addCustomTargeting("hb_cache_id","f0c6ffe4-e49e-4d5b-bf83-9d415f66f451");
        builder.addCustomTargeting("hb_size", "320x50");
        builder.addCustomTargeting("hb_pb","0.50");
        builder.addCustomTargeting("hb_env","mobile-app");
        final PublisherAdRequest request = builder.build();
        dfpAdView.loadAd(request);
        //region PrebidMobile Mobile API 2.0 usage
//        final BannerAdUnit adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND);
//        adUnit.addSize(width, height);
//        adUnit.setAutoRefreshPeriodMillis(30000);
//        adUnit.fetchDemand(request, this, new OnCompleteListener() {
//            @Override
//            public void onComplete(ResultCode resultCode) {
//                LogUtil.d("Load ad " + resultCode.name());
//                dfpAdView.loadAd(request);
//            }
//        });
        //endregion
    }

    void createDFPInterstitial() {

    }

    void createMoPubBanner(String size) {
        FrameLayout adFrame = (FrameLayout) findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        String[] wAndH = size.split("x");
        int width = Integer.valueOf(wAndH[0]);
        int height = Integer.valueOf(wAndH[1]);
        final MoPubView adView = new MoPubView(this);
//        adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_300x250);
        adView.setAdUnitId("59c45acb1cc34114925a8fe3eba8436a");
        adView.setMinimumWidth(width);
        adView.setMinimumHeight(height);
        adFrame.addView(adView);
        adView.setKeywords("hb_cache_id:76248d7f-1a06-462d-9b7e-2f1d19af071b,hb_size:300x250,hb_pb:0.50,hb_env:mobile-app");
        adView.loadAd();
//        final BannerAdUnit adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND);
//        adUnit.addSize(width, height);
//        adUnit.setAutoRefreshPeriodMillis(3000);
//        adUnit.fetchDemand(adView, this, new OnCompleteListener() {
//            @Override
//            public void onComplete(ResultCode resultCode) {
//                adView.loadAd();
//            }
//        });
    }

    void createMoPubInterstitial() {
        MoPubInterstitial interstitial = new MoPubInterstitial(this, "3acf42c3c06a4d93a512b51881f8ac60");
        interstitial.setKeywords("hb_cache_id:46c39349-a591-46f2-8306-1cc9f736a176,hb_size:300x250,hb_pb:0.50,hb_env:mobile-app");
        interstitial.setInterstitialAdListener(new MoPubInterstitial.InterstitialAdListener() {
            @Override
            public void onInterstitialLoaded(MoPubInterstitial interstitial) {
                interstitial.show();
            }

            @Override
            public void onInterstitialFailed(MoPubInterstitial interstitial, MoPubErrorCode errorCode) {
                LogUtil.d(errorCode.toString());

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
        interstitial.load();

    }
}
