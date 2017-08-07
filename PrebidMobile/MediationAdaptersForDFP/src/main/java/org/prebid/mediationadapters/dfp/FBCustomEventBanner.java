package org.prebid.mediationadapters.dfp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class FBCustomEventBanner implements CustomEventBanner {
    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        // todo use fan sdk to load banner from extra
        Log.d("FB-Integration", "request banner");
        Log.d("FB-Integration", "String parameter: " + s);
        if (bundle != null) {
            Log.d("FB-Integration", "Extra: " + bundle.get("hb_cache_id"));
        }
        if (customEventBannerListener != null) {
//            TextView t = new TextView(context);
//            t.setText("Hello World!");
//            customEventBannerListener.onAdLoaded(t);
        }
        Log.d("FB-Integration", "finish requesting");
    }

    @Override
    public void onDestroy() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }
}
