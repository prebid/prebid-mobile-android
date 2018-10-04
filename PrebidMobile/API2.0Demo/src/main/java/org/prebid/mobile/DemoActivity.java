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
import com.mopub.mobileads.MoPubView;

import static org.prebid.mobile.Constants.MOPUB_BANNER_ADUNIT_ID_300x250;

public class DemoActivity extends AppCompatActivity {
    int count = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Prebid.setTimeOUt(10000);
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
        final PublisherAdRequest request = builder.build();
        //region Prebid Mobile API 2.0 usage
        BannerAdUnit adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND);
        adUnit.addSize(width, height);
        adUnit.fetchDemand(request, this, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                dfpAdView.loadAd(request);
            }
        });
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
        adView.setAdUnitId(MOPUB_BANNER_ADUNIT_ID_300x250);
        adView.setMinimumWidth(width);
        adView.setMinimumHeight(height);
        adFrame.addView(adView);
        final BannerAdUnit adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS_DEMAND);
        adUnit.addSize(width, height);
        adUnit.setAutoRefreshPeriod(30);
        adUnit.fetchDemand(adView, this, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                adView.loadAd();
            }
        });

    }

    void createMoPubInterstitial() {

    }
}
