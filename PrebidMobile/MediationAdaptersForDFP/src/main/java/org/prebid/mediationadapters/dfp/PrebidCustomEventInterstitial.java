package org.prebid.mediationadapters.dfp;


import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

public class PrebidCustomEventInterstitial implements CustomEventInterstitial {
    private String bidderName;
    private Object adObject;

    @Override
    public void requestInterstitialAd(Context context, CustomEventInterstitialListener customEventInterstitialListener, String s, MediationAdRequest mediationAdRequest, Bundle bundle) {
        if (bundle != null) {
            String cacheId = (String) bundle.get("hb_cache_id");
            bidderName = (String) bundle.get("hb_bidder");
            if ("audienceNetwork".equals(bidderName)) {
                loadFacebookInterstitial();
            } else {
                if (customEventInterstitialListener != null) {
                    customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                }
            }
        } else {
            if (customEventInterstitialListener != null) {
                customEventInterstitialListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            }
        }
    }

    @Override
    public void showInterstitial() {

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

    private void loadFacebookInterstitial() {

    }
}
