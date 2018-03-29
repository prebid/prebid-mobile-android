package org.prebid.mobile.demoapp;

import android.app.Application;
import android.os.Build;
import android.webkit.WebView;

import org.prebid.mobile.core.AdUnit;
import org.prebid.mobile.core.BannerAdUnit;
import org.prebid.mobile.core.InterstitialAdUnit;
import org.prebid.mobile.core.Prebid;
import org.prebid.mobile.core.PrebidException;
import org.prebid.mobile.core.TargetingParams;

import java.util.ArrayList;

import static org.prebid.mobile.demoapp.Constants.BANNER_300x250;
import static org.prebid.mobile.demoapp.Constants.BANNER_320x50;
import static org.prebid.mobile.demoapp.Constants.INTERSTITIAL_FULLSCREEN;
import static org.prebid.mobile.demoapp.Constants.PBS_ACCOUNT_ID;
import static org.prebid.mobile.demoapp.Constants.PBS_CONFIG_300x250_APPNEXUS_DEMAND;
import static org.prebid.mobile.demoapp.Constants.PBS_CONFIG_APPNEXUS_DEMAND;

public class PrebidApplication extends Application {
    /**
     * Called when the application is starting, before any activity, service,
     * or receiver objects (excluding content providers) have been created.
     * Implementations should be as quick as possible (for example using
     * lazy initialization of state) since the time spent in this function
     * directly impacts the performance of starting the first activity,
     * service, or receiver in a process.
     * If you override this method, be sure to call super.onCreate().
     */
    @Override
    public void onCreate() {
        super.onCreate();


        // Clear WebView Cache when needed
        WebView obj = new WebView(this);
        obj.clearCache(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && BuildConfig.DEBUG) {
            WebView.setWebContentsDebuggingEnabled(true);
        }

        /**
         * Initialise prebid for DFP ad unit
         */
        initialisePrebidForDFP();
    }

    private void initialisePrebidForDFP() {

        ArrayList<AdUnit> adUnits = new ArrayList<AdUnit>();

        //Configure Ad-Slot1
        BannerAdUnit adUnit1 = new BannerAdUnit(BANNER_320x50, PBS_CONFIG_APPNEXUS_DEMAND);
        adUnit1.addSize(320, 50);

        //Configure Ad-Slot2 with the same demand source
        BannerAdUnit adUnit2 = new BannerAdUnit(BANNER_300x250, PBS_CONFIG_300x250_APPNEXUS_DEMAND);
        adUnit2.addSize(300, 250);
        adUnit2.addSize(300, 600);

        //Configure Interstitial Ad Unit
        InterstitialAdUnit adUnit3 = new InterstitialAdUnit(INTERSTITIAL_FULLSCREEN, PBS_CONFIG_APPNEXUS_DEMAND);

        // Add Configuration
        adUnits.add(adUnit1);
        adUnits.add(adUnit2);
        adUnits.add(adUnit3);

        // Set targeting
        TargetingParams.setGender(TargetingParams.GENDER.FEMALE);
        TargetingParams.setYearOfBirth(1992);
        TargetingParams.setLocationDecimalDigits(2);
        TargetingParams.setLocationEnabled(true);
        TargetingParams.setUserTargeting("PrebidKey", "PrebidValue"); // this should add "Prebidkey=PrebidValue" in user.keywords in ortb request
        TargetingParams.setUserTargeting("PrebidKey2", null); // this should add "PrebidKey2" in user.keywords in ortb request
        TargetingParams.setUserTargeting(null, "PrebidValue2"); // this should add nothing
        TargetingParams.setUserTargeting(null, null); // this should add nothing

        // Register ad units for prebid.
        try {
            // Start the initialization with DFP ad server
            Prebid.init(getApplicationContext(), adUnits, PBS_ACCOUNT_ID, Prebid.AdServer.DFP, Prebid.Host.APPNEXUS);
        } catch (PrebidException e) {
            e.printStackTrace();
        }
    }


}
