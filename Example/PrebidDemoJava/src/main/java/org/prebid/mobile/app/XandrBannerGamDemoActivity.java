package org.prebid.mobile.app;

import android.os.Bundle;
import android.util.Log;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.admanager.AdManagerAdRequest;
import com.google.android.gms.ads.admanager.AdManagerAdView;

import org.prebid.mobile.AdUnit;
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

public class XandrBannerGamDemoActivity extends AppCompatActivity {
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
        final AdManagerAdView amBanner = new AdManagerAdView(this);
        amBanner.setAdUnitId(Constants.DFP_BANNER_ADUNIT_ID_ALL_SIZES_APPNEXUS);
        amBanner.setAdSizes(new AdSize(300, 250));
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

        final AdManagerAdRequest.Builder builder = new AdManagerAdRequest.Builder();

        //region PrebidMobile Mobile API 1.0 usage
        int millis = getIntent().getIntExtra(Constants.AUTO_REFRESH_NAME, 0);
        adUnit.setAutoRefreshPeriodMillis(millis);
        adUnit.fetchDemand(builder, new OnCompleteListener() {
            @Override
            public void onComplete(ResultCode resultCode) {
                AdManagerAdRequest request = builder.build();
                amBanner.loadAd(request);
            }
        });
    }
}
