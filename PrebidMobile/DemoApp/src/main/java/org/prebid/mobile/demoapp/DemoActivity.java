package org.prebid.mobile.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

import org.prebid.mobile.demoapp.dfpdemofragments.DFPBannerFragment;
import org.prebid.mobile.demoapp.dfpdemofragments.DFPInterstitialFragment;
import org.prebid.mobile.demoapp.mopubdemofragments.MoPubBannerFragment;
import org.prebid.mobile.demoapp.mopubdemofragments.MoPubInterstitialFragment;

public class DemoActivity extends AppCompatActivity {
    private Fragment demoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Intent intent = getIntent();
        if (intent != null) {
            String adServerName = (String) intent.getExtras().get(Constants.ADSERVER);
            if ("dfp".equals(adServerName)) {
                String adFormat = intent.getExtras().getString(Constants.ADFORMAT);
                if ("banner".equals(adFormat)) {
                    demoFragment = new DFPBannerFragment();
                } else if ("interstitial".equals(adFormat)) {
                    demoFragment = new DFPInterstitialFragment();
                }
            } else if ("mopub".equals(adServerName)) {
                String adFormat = intent.getExtras().getString(Constants.ADFORMAT);
                if ("banner".equals(adFormat)) {
                    demoFragment = new MoPubBannerFragment();
                } else if ("interstitial".equals(adFormat)) {
                    demoFragment = new MoPubInterstitialFragment();
                }
            }
        }
        if (demoFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.demoRoot, demoFragment).commit();
        }

    }

    // This is used in the calabash tests to grab the obfuscated web view class name from DFP
    public String getDFPWebViewName() {
        DFPBannerFragment fragment = (DFPBannerFragment) getSupportFragmentManager().findFragmentById(R.id.demoRoot);
        return fragment.getDFPWebViewName();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
