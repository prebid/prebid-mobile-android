

package org.prebid.mobile.app;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.FrameLayout;

import com.mopub.mobileads.MoPubView;

import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerAdUnit;
import org.prebid.mobile.Host;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;

public class TestActivity extends AppCompatActivity implements OnCompleteListener {
    ResultCode resultCode = null;
    Object adObject = null;
    AdUnit adUnit = null;
    private Handler mHandler = null;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        mHandler = new Handler(Looper.getMainLooper());
    }

    public void setUpRunbiconDemandTest() {
        PrebidMobile.setApplicationContext(this);
        PrebidMobile.setPrebidServerAccountId("1001");
        PrebidMobile.setPrebidServerHost(Host.RUBICON);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                adUnit = new BannerAdUnit("1001-2", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    @Override
    public void onComplete(ResultCode resultCode) {
        this.resultCode = resultCode;
        displayAd();
    }

    public void displayAd() {
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        if (adObject instanceof MoPubView) {
            adFrame.addView((View) adObject);
            LogUtil.d(("TestActivity: " + ((MoPubView) adObject).getKeywords()));
            ((MoPubView) adObject).loadAd();
        }
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
