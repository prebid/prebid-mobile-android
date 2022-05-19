package org.prebid.mobile.drprebid;

import androidx.multidex.MultiDexApplication;
import org.prebid.mobile.ServerRequestSettings;
import org.prebid.mobile.drprebid.managers.LineItemKeywordManager;

public class DrPrebidApplication extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();
        try {
            ServerRequestSettings.update(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        LineItemKeywordManager.getInstance().refreshCacheIds(this);
    }
}
