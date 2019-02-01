package org.prebid.mobile.app;

import android.app.Application;

import com.mopub.common.MoPub;
import com.mopub.common.SdkConfiguration;

import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;

import java.util.ArrayList;
import java.util.List;

public class CustomApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        //init MoPub SDK
        List<String> networksToInit = new ArrayList<String>();
        networksToInit.add("com.mopub.mobileads.VungleRewardedVideo");
        SdkConfiguration sdkConfiguration = new SdkConfiguration.Builder("a935eac11acd416f92640411234fbba6")
                .withNetworksToInit(networksToInit)
                .build();
        MoPub.initializeSdk(this, sdkConfiguration, null);
        //set Prebid Mobile global Settings
        //region PrebidMobile API
        PrebidMobile.setAccountId(Constants.PBS_ACCOUNT_ID);
        PrebidMobile.setHost(Host.APPNEXUS);
        PrebidMobile.setShareGeoLocation(true);
        PrebidMobile.setApplicationContext(getApplicationContext());
        //endregion
    }
}
