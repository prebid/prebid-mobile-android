package org.prebid.mobile.app;

import android.os.Bundle;
import android.widget.FrameLayout;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.BannerBaseAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Signals;

import java.util.Arrays;

public class XandrBannerMoPubDemoActivity extends AppCompatActivity {
    AdUnit adUnit;

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
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setPrebidServerAccountId(Constants.PBS_ACCOUNT_ID_APPNEXUS);
        BannerAdUnit adUnit = new BannerAdUnit(Constants.PBS_CONFIG_ID_300x250_APPNEXUS, 300, 250);

        BannerBaseAdUnit.Parameters parameters = new BannerBaseAdUnit.Parameters();
        parameters.setApi(Arrays.asList(Signals.Api.MRAID_2));
//        parameters.setApi(Arrays.asList(new Signals.Api(5)));

        adUnit.setParameters(parameters);

        this.adUnit = adUnit;
//        Util.enableAdditionalFunctionality(this.adUnit);
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        final MoPubView adView = new MoPubView(this);
        adView.setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250_APPNEXUS);

        adView.setMinimumWidth(300);
        adView.setMinimumHeight(250);
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
