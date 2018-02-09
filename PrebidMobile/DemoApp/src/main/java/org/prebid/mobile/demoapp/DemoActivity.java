package org.prebid.mobile.demoapp;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;

public class DemoActivity extends AppCompatActivity {
    private Fragment demoFragment;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);
        Intent intent = getIntent();
        if (intent != null) {
            String adServerName = (String) intent.getExtras().get(Constants.ADSERVER);
            if (Constants.DFP.equals(adServerName)) {
                String demand = intent.getExtras().getString(Constants.DEMAND);
                if (Constants.APN_BANNER.equals(demand)) {
                    demoFragment = new org.prebid.mobile.demoapp.dfpdemofragments.AppNexusBannerDemandFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.WIDTH, intent.getExtras().getInt(Constants.WIDTH));
                    bundle.putInt(Constants.HEIGHT, intent.getExtras().getInt(Constants.HEIGHT));
                    demoFragment.setArguments(bundle);
                } else if (Constants.APN_INTERSTITIAL.equals(demand)) {
                    demoFragment = new org.prebid.mobile.demoapp.dfpdemofragments.AppNexusInterstitialDemandFragment();
                } else if (Constants.FB_BANNER.equals(demand)) {
                    demoFragment = new org.prebid.mobile.demoapp.dfpdemofragments.FacebookBannerDemandFragment();
                } else if (Constants.FB_INTERSTITIAL.equals(demand)) {
                    demoFragment = new org.prebid.mobile.demoapp.dfpdemofragments.FacebookInterstitialDemandFragment();
                }
            } else if (Constants.MOPUB.equals(adServerName)) {
                String adFormat = intent.getExtras().getString(Constants.DEMAND);
                if (Constants.APN_BANNER.equals(adFormat)) {
                    demoFragment = new org.prebid.mobile.demoapp.mopubdemofragments.AppNexusBannerDemandFragment();
                    Bundle bundle = new Bundle();
                    bundle.putInt(Constants.WIDTH, intent.getExtras().getInt(Constants.WIDTH));
                    bundle.putInt(Constants.HEIGHT, intent.getExtras().getInt(Constants.HEIGHT));
                    demoFragment.setArguments(bundle);
                } else if (Constants.APN_INTERSTITIAL.equals(adFormat)) {
                    demoFragment = new org.prebid.mobile.demoapp.mopubdemofragments.AppNexusInterstitialDemandFragment();
                } else if (Constants.FB_BANNER.equals(adFormat)) {
                    demoFragment = new org.prebid.mobile.demoapp.mopubdemofragments.FacebookBannerDemandFragment();
                } else if (Constants.FB_INTERSTITIAL.equals(adFormat)) {
                    demoFragment = new org.prebid.mobile.demoapp.mopubdemofragments.FacebookInterstitialDemandFragment();

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
        org.prebid.mobile.demoapp.dfpdemofragments.AppNexusBannerDemandFragment fragment = (org.prebid.mobile.demoapp.dfpdemofragments.AppNexusBannerDemandFragment) getSupportFragmentManager().findFragmentById(R.id.demoRoot);
        return fragment.getDFPWebViewName();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
