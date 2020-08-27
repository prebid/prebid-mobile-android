package org.prebid.mobile.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Signals;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.util.Arrays;

public class RubiconBannerGamDemoActivity extends AppCompatActivity {
    BannerAdUnit adUnit;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_RUBICON);
        PrebidMobile.setStoredAuctionResponse(Constants.PBS_STORED_RESPONSE_300x250_RUBICON);
        String adSizeName = getIntent().getStringExtra(Constants.AD_SIZE_NAME);
        int width = 0;
        int height = 0;

        String[] wAndH = adSizeName.split("x");
        width = Integer.valueOf(wAndH[0]);
        height = Integer.valueOf(wAndH[1]);

        adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_RUBICON, width, height);
        BannerBaseAdUnit.Parameters parameters = new BannerBaseAdUnit.Parameters();
        parameters.setApi(Arrays.asList(Signals.Api.MRAID_2));
        adUnit.setParameters(parameters);
        final PublisherAdView amBanner = new PublisherAdView(this);
        amBanner.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_300x250_RUBICON);
        amBanner.setAdSizes(new AdSize(width, height));
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

        //region PrebidMobile Mobile API 1.0 usage
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(builder, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                PublisherAdRequest request = builder.build();
                amBanner.loadAd(request);
            }
        });

    }
}
