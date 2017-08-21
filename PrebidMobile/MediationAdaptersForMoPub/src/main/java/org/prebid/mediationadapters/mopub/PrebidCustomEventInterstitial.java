package org.prebid.mediationadapters.mopub;


import android.content.Context;

import com.mopub.mobileads.CustomEventInterstitial;

import java.util.Map;

public class PrebidCustomEventInterstitial extends CustomEventInterstitial {
    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        if (localExtras != null) {
            String cache_id = (String) localExtras.get("hb_cache_id");
            String bidder = (String) localExtras.get("hb_bidder");
        }
    }

    @Override
    protected void showInterstitial() {

    }

    @Override
    protected void onInvalidate() {

    }
}
