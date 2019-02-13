

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
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.TargetingParams;

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

    public void post(Runnable r) {
        mHandler.post(r);
    }

    public void setUpRunbiconDemandTest() {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
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
                destroyAdUnit();
                adUnit = new BannerAdUnit("1001-2", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpAppNexusKeyValueTargetingTest() {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("67bac530-9832-4f78-8c94-fbf88ac7bd14", 300, 250);
                adUnit.addUserKeyword("pbm_key", "pbm_value1");
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpAppNexusKeyValueTargetingTest2() {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("67bac530-9832-4f78-8c94-fbf88ac7bd14", 300, 250);
                adUnit.addUserKeyword("pbm_key", "pbm_value2");
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpEmptyPrebidServerAccountID() {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("67bac530-9832-4f78-8c94-fbf88ac7bd14", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpAppNexusInvalidPrebidServerAccountID() {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-ffffffffffff");
        PrebidMobile.setPrebidServerHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("6ace8c7d-88c0-4623-8117-75bc3f0a2e45", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpAppNexusAgeTargeting1(String hostUrl) {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(hostUrl);
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        TargetingParams.setYearOfBirth(1855);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpAppNexusAgeTargeting2(String hostUrl) {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(hostUrl);
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        TargetingParams.setYearOfBirth(-1);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpAppNexusAgeTargeting3(String hostUrl) {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(hostUrl);
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        TargetingParams.setYearOfBirth(2018);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }


    public void setUpAppNexusAgeTargeting4(String hostUrl) {
        PrebidMobile.setApplicationContext(this.getApplicationContext());
        PrebidMobile.setPrebidServerAccountId("bfa84af2-bd16-4d35-96ad-31c6bb888df0");
        Host.CUSTOM.setHostUrl(hostUrl);
        PrebidMobile.setPrebidServerHost(Host.CUSTOM);
        PrebidMobile.setShareGeoLocation(true);
        TargetingParams.setYearOfBirth(1989);
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("47706260-ee91-4cd7-b656-2185aca89f59", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    public void setUpLocationTargeting() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                adObject = new MoPubView(TestActivity.this);
                ((MoPubView) adObject).setAdUnitId(Constants.MOPUB_BANNER_ADUNIT_ID_300x250);
                ((MoPubView) adObject).setMinimumHeight(250);
                ((MoPubView) adObject).setMinimumWidth(300);
                destroyAdUnit();
                adUnit = new BannerAdUnit("8522cead-1eb4-4f09-b6e2-083fa3a6e6ce", 300, 250);
                adUnit.fetchDemand(adObject, TestActivity.this);
            }
        });
    }

    @Override
    public void onComplete(ResultCode resultCode) {
        this.resultCode = resultCode;
        displayAd();
    }

    void displayAd() {
        FrameLayout adFrame = findViewById(R.id.adFrame);
        adFrame.removeAllViews();
        if (adObject instanceof MoPubView) {
            adFrame.addView((View) adObject);
            ((MoPubView) adObject).loadAd();
        }
    }

    void destroyAdUnit() {
        if (adUnit != null) {
            adUnit.stopAutoRefersh();
            adUnit = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        destroyAdUnit();
    }
}
