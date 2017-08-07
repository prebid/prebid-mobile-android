package org.prebid.mediationadapters.dfp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class FBCustomEventBanner implements CustomEventBanner, AdListener {
    private CustomEventBannerListener customEventBannerListener;
    private AdView adView;

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        // todo use fan sdk to load banner from extra
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        this.customEventBannerListener = customEventBannerListener;
        if (bundle != null) {
            String cacheId = (String) bundle.get("hb_cache_id");
            adView = new AdView(context, "id", new com.facebook.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
            adView.setAdListener(this);
            adView.loadAdFromBid(cacheId); // todo check if there's any other things to be added for loading
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onAdFailedToLoad(0); // todo error code matching
            }
        }
        Log.d("FB-Integration", "finish requesting");


    }

    // Google custom event banner implementation

    @Override
    public void onDestroy() {
        customEventBannerListener = null;
        if (adView != null) {
            adView.destroy();
        }
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {

    }

    // FB ad listener implementation
    @Override
    public void onError(Ad ad, AdError adError) {
        if (customEventBannerListener != null) {
            customEventBannerListener.onAdFailedToLoad(0); // todo error code parsing
        }
    }

    @Override
    public void onAdLoaded(Ad ad) {
        if (customEventBannerListener != null) {
            customEventBannerListener.onAdLoaded(adView);
        }
    }

    @Override
    public void onAdClicked(Ad ad) {
        if (customEventBannerListener != null) {
            customEventBannerListener.onAdClicked();
        }
    }

    @Override
    public void onLoggingImpression(Ad ad) {
    }
}
