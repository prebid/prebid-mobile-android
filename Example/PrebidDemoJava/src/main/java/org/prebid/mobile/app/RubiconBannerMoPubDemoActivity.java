package org.prebid.mobile.app;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Signals;

import java.util.Arrays;

public class RubiconBannerMoPubDemoActivity extends AppCompatActivity {
    BannerAdUnit adUnit;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (adUnit != null) {
            adUnit.stopAutoRefresh();
            adUnit = null;
        }
    }

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
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        final MoPubView adView = new MoPubView(this);
        adView.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_RUBICON);
        adView.setMinimumWidth(width);
        adView.setMinimumHeight(height);
        adFrame.addView(adView);
        adUnit.setAutoRefreshPeriodMillis(getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0));
        adUnit.fetchDemand(adView, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                adView.loadAd();

            }
        });
    }
}
