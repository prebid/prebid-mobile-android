package org.prebid.mediationadapters.mopub;

import android.content.Context;
import android.util.Log;

import com.mopub.mobileads.CustomEventBanner;

import java.util.Map;

public class FBCustomEventBanner extends CustomEventBanner {
    @Override
    protected void loadBanner(Context context, CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        Log.d("FB-Integration", "request banner");
        if (localExtras != null) {
            String cache_id = (String) localExtras.get("hb_cache_id"); // get the cache
            // todo use FAN SDK to load cache
            // notify custom event banner listener
        }
        // serverExtras ?
    }

    @Override
    protected void onInvalidate() {

    }
}
