package org.prebid.demandsdkadapters.dfp;


import android.content.Context;
import android.os.Bundle;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;

import org.prebid.demandsdkadapters.common.AdListener;
import org.prebid.demandsdkadapters.common.InterstitialController;
import org.prebid.demandsdkadapters.common.PrebidCustomEventSettings;
import org.prebid.mobile.core.ErrorCode;

import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_BIDDER_NAME;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_BIDDER;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_CACHE_ID;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.isDemandEnabled;

public class PrebidCustomEventInterstitial implements CustomEventInterstitial, AdListener {
    private InterstitialController controller;
    private CustomEventInterstitialListener listener;

    @Override
    public void requestInterstitialAd(Context context, CustomEventInterstitialListener customEventInterstitialListener, String s, MediationAdRequest mediationAdRequest, Bundle bundle) {
        this.listener = customEventInterstitialListener;
        if (bundle != null) {
            String cacheId = (String) bundle.get(PREBID_CACHE_ID);
            String bidderName = (String) bundle.get(PREBID_BIDDER);
            if (FACEBOOK_BIDDER_NAME.equals(bidderName) && isDemandEnabled(PrebidCustomEventSettings.Demand.FACEBOOK)) {
                controller = new InterstitialController(PrebidCustomEventSettings.Demand.FACEBOOK, cacheId);
                controller.loadAd(context, this);
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
        if (controller != null) {
            controller.show();
        }
    }

    @Override
    public void onDestroy() {
        if (controller != null) {
            controller.destroy();
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onAdLoaded(Object adObj) {
        if (listener != null) {
            listener.onAdLoaded();
        }
    }

    @Override
    public void onAdFailed(Object adObj, ErrorCode errorCode) {
        if (listener != null) {
            switch (errorCode) {
                case INVALID_REQUEST:
                    listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
                    break;
                case NETWORK_ERROR:
                    listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NETWORK_ERROR);
                    break;
                case INTERNAL_ERROR:
                    listener.onAdFailedToLoad(AdRequest.ERROR_CODE_INTERNAL_ERROR);
                    break;
                case NO_BIDS:
                    listener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                    break;
            }
        }
    }

    @Override
    public void onAdClicked(Object adObj) {
        if (listener != null) {
            listener.onAdClicked();
        }
    }

    @Override
    public void onInterstitialShown(Object adObj) {
        if (listener != null) {
            listener.onAdOpened();
        }
    }

    @Override
    public void onInterstitialClosed(Object adObj) {
        if (listener != null) {
            listener.onAdClosed();
        }
    }
}
