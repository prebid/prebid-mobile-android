package org.prebid.demandsdkadapters.mopub;


import android.content.Context;

import com.mopub.mobileads.CustomEventInterstitial;
import com.mopub.mobileads.MoPubErrorCode;

import org.prebid.demandsdkadapters.common.AdListener;
import org.prebid.demandsdkadapters.common.InterstitialController;
import org.prebid.mobile.core.ErrorCode;
import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.core.PrebidDemandSettings;

import java.util.Map;

import static org.prebid.mobile.core.PrebidDemandSettings.FACEBOOK_BIDDER_NAME;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_BIDDER;
import static org.prebid.mobile.core.PrebidDemandSettings.PREBID_CACHE_ID;
import static org.prebid.mobile.core.PrebidDemandSettings.isDemandEnabled;

public class PrebidCustomEventInterstitial extends CustomEventInterstitial implements AdListener {
    private InterstitialController controller;
    private CustomEventInterstitialListener listener;

    @Override
    protected void loadInterstitial(Context context, CustomEventInterstitialListener customEventInterstitialListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        LogUtil.d("Facebook demand custom event called for MoPub.");
        this.listener = customEventInterstitialListener;
        if (localExtras != null) {
            String cache_id = (String) localExtras.get(PREBID_CACHE_ID);
            String bidder = (String) localExtras.get(PREBID_BIDDER);
            if (FACEBOOK_BIDDER_NAME.equals(bidder) && isDemandEnabled(PrebidDemandSettings.Demand.FACEBOOK)) {
                controller = new InterstitialController(PrebidDemandSettings.Demand.FACEBOOK, cache_id);
                controller.loadAd(context, this);
            } else {
                if (customEventInterstitialListener != null) {
                    customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                }
            }
        } else {
            if (customEventInterstitialListener != null) {
                customEventInterstitialListener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
            }
        }
    }

    @Override
    protected void showInterstitial() {
        if (controller != null) {
            controller.show();
        }

    }

    @Override
    protected void onInvalidate() {
        if (controller != null) {
            controller.destroy();
        }
    }

    @Override
    public void onAdLoaded(Object adObj) {
        if (listener != null) {
            listener.onInterstitialLoaded();
        }
    }

    @Override
    public void onAdFailed(Object adObj, ErrorCode errorCode) {
        if (listener != null) {
            switch (errorCode) {
                case INVALID_REQUEST:
                    listener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                    break;
                case NETWORK_ERROR:
                    listener.onInterstitialFailed(MoPubErrorCode.NO_CONNECTION);
                    break;
                case INTERNAL_ERROR:
                    listener.onInterstitialFailed(MoPubErrorCode.INTERNAL_ERROR);
                    break;
                case NO_BIDS:
                    listener.onInterstitialFailed(MoPubErrorCode.NO_FILL);
                    break;
            }
        }
    }

    @Override
    public void onAdClicked(Object adObj) {
        if (listener != null) {
            listener.onInterstitialClicked();
        }
    }

    @Override
    public void onInterstitialShown(Object adObj) {
        if (listener != null) {
            listener.onInterstitialShown();
        }
    }

    @Override
    public void onInterstitialClosed(Object adObj) {
        if (listener != null) {
            listener.onInterstitialDismissed();
        }
    }
}
