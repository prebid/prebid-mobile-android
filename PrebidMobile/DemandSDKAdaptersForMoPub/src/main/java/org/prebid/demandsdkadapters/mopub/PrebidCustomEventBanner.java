package org.prebid.demandsdkadapters.mopub;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.mopub.mobileads.CustomEventBanner;
import com.mopub.mobileads.MoPubErrorCode;

import org.prebid.demandsdkadapters.common.AdListener;
import org.prebid.demandsdkadapters.common.BannerController;
import org.prebid.demandsdkadapters.common.PrebidCustomEventSettings;
import org.prebid.mobile.core.ErrorCode;

import java.util.Map;

import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.FACEBOOK_BIDDER_NAME;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.MOPUB_HEIGHT;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.MOPUB_WIDTH;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_BIDDER;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.PREBID_CACHE_ID;
import static org.prebid.demandsdkadapters.common.PrebidCustomEventSettings.isDemandEnabled;


public class PrebidCustomEventBanner extends CustomEventBanner implements AdListener {
    private BannerController controller;
    private CustomEventBannerListener listener;

    @Override
    protected void loadBanner(final Context context, final CustomEventBannerListener customEventBannerListener, Map<String, Object> localExtras, Map<String, String> serverExtras) {
        Log.d("FB-Integration", "Load Prebid content for Facebook");
        this.listener = customEventBannerListener;
        if (localExtras != null) {
            String cache_id = (String) localExtras.get(PREBID_CACHE_ID);
            String bidderName = (String) localExtras.get(PREBID_BIDDER);
            int width = (int) localExtras.get(MOPUB_WIDTH);
            int height = (int) localExtras.get(MOPUB_HEIGHT);
            if (FACEBOOK_BIDDER_NAME.equals(bidderName) && isDemandEnabled(PrebidCustomEventSettings.Demand.FACEBOOK)) {
                controller = new BannerController(cache_id, PrebidCustomEventSettings.Demand.FACEBOOK);
                controller.loadAd(context, width, height, this);
            } else {
                if (customEventBannerListener != null) {
                    customEventBannerListener.onBannerFailed(MoPubErrorCode.NO_FILL);
                }
            }
        } else {
            if (customEventBannerListener != null) {
                customEventBannerListener.onBannerFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            }
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
            listener.onBannerLoaded((View) adObj);
        }
    }

    @Override
    public void onAdFailed(Object adObj, ErrorCode errorCode) {
        if (listener != null) {
            switch (errorCode) {
                case INVALID_REQUEST:
                    listener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                    break;
                case NETWORK_ERROR:
                    listener.onBannerFailed(MoPubErrorCode.NO_CONNECTION);
                    break;
                case INTERNAL_ERROR:
                    listener.onBannerFailed(MoPubErrorCode.INTERNAL_ERROR);
                    break;
                case NO_BIDS:
                    listener.onBannerFailed(MoPubErrorCode.NO_FILL);
                    break;
            }
        }
    }

    @Override
    public void onAdClicked(Object adObj) {
        if (listener != null) {
            listener.onBannerClicked();
        }
    }

    @Override
    public void onInterstitialShown(Object adObj) {

    }

    @Override
    public void onInterstitialClosed(Object adObj) {

    }
}
