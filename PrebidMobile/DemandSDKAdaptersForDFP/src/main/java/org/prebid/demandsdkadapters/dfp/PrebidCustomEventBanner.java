package org.prebid.demandsdkadapters.dfp;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

import org.prebid.demandsdkadapters.common.AdListener;
import org.prebid.demandsdkadapters.common.BannerController;
import org.prebid.demandsdkadapters.common.PrebidCustomEventSettings;
import org.prebid.mobile.core.ErrorCode;

import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_BIDDER_NAME;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_BIDDER;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_CACHE_ID;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.isDemandEnabled;

public class PrebidCustomEventBanner implements CustomEventBanner, AdListener {
    private BannerController controller;
    private CustomEventBannerListener listener;

    @Override
    public void requestBannerAd(Context context, CustomEventBannerListener customEventBannerListener, String s, AdSize adSize, MediationAdRequest mediationAdRequest, Bundle bundle) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        this.listener = customEventBannerListener;
        if (bundle != null) {
            String cacheId = (String) bundle.get(PREBID_CACHE_ID);
            String bidderName = (String) bundle.get(PREBID_BIDDER);
//            if ("appnexus".equals(bidderName) && isDemandEnabled(PrebidCustomEventSettings.Demand.FACEBOOK)) {
            if (FACEBOOK_BIDDER_NAME.equals(bidderName) && isDemandEnabled(PrebidCustomEventSettings.Demand.FACEBOOK)) {
                controller = new BannerController(cacheId, PrebidCustomEventSettings.Demand.FACEBOOK);
                controller.loadAd(context, adSize.getWidth(), adSize.getHeight(), this);
            } else {
                if (customEventBannerListener != null) {
                    customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_NO_FILL);
                }
            }
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onAdFailedToLoad(AdRequest.ERROR_CODE_INVALID_REQUEST);
            }
        }
    }

    // Google custom event banner implementation
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
            listener.onAdLoaded((View) adObj);
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

    }

    @Override
    public void onInterstitialClosed(Object adObj) {

    }
}
