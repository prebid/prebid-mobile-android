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

        //region PrebidMobile Mobile API 2.0 usage
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
