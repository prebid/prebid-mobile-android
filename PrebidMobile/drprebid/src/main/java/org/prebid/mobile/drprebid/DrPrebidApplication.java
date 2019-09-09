package org.prebid.mobile.drprebid;

import androidx.multidex.MultiDexApplication;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ServerRequestSettings;
import org.prebid.mobile.drprebid.managers.LineItemKeywordManager;

public class DrPrebidApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        PrebidMobile.setApplicationContext(this);
        ServerRequestSettings.update(this);

        LineItemKeywordManager.getInstance().refreshCacheIds(this);
    }
}
