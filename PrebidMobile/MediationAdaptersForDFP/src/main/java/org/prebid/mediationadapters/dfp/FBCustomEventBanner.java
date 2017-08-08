package org.prebid.mediationadapters.dfp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.ads.Ad;
import com.facebook.ads.AdError;
import com.facebook.ads.AdListener;
import com.facebook.ads.AdView;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

public class FBCustomEventBanner implements CustomEventBanner, AdListener {
    private CustomEventBannerListener customEventBannerListener;
    private AdView adView;

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        this.customEventBannerListener = customEventBannerListener;
        if (bundle != null) {
            String cacheId = (String) bundle.get("hb_cache_id");
            // todo how to get the facebook adview id
            // todo check if there's any other things to be added for loading
            adView = new AdView(context, "id", new com.facebook.ads.AdSize(adSize.getWidth(), adSize.getHeight()));
            adView.setAdListener(this);
            adView.loadAdFromBid(cacheId);
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            }
        }
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
            if (adError != null) {
                switch (adError.getErrorCode()) {
                    case 1000:
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                        break;
                    case 1001:
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                        break;
                    case 1002:
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                        break;
                    case 2000:
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                        break;
                    case 2001:
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                        break;
                    case 3001:
                        customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                        break;
                }
            } else {
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
            }

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
