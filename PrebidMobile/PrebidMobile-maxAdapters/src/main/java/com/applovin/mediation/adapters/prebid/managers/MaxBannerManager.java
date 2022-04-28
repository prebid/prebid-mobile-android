package com.applovin.mediation.adapters.prebid.managers;

import android.app.Activity;
import android.util.Log;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.ListenersCreator;
import com.applovin.mediation.adapters.prebid.ParametersChecker;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.DisplayView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

public class MaxBannerManager {

    private static final String TAG = MaxBannerManager.class.getSimpleName();

    private DisplayView adView;
    private MaxAdViewAdapterListener maxListener;

    public void loadAd(
            MaxAdapterResponseParameters parameters,
            MaxAdFormat maxAdFormat,
            Activity activity,
            MaxAdViewAdapterListener listener
    ) {
        maxListener = listener;

        String responseId = ParametersChecker.getResponseIdAndCheckKeywords(parameters, this::onError);
        BidResponse bidResponse = ParametersChecker.getBidResponse(responseId, this::onError);
        if (bidResponse == null) {
            return;
        }

        switch (maxAdFormat.getLabel()) {
            case "BANNER":
            case "MREC":
                showBanner(activity, parameters, bidResponse);
                break;
            default:
                String error = "Unknown type of MAX ad!";
                Log.e(TAG, error);
                maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1005, error));
        }
    }

    public void destroy() {
        adView.destroy();
        adView = null;
    }


    private void showBanner(
            Activity activity,
            MaxAdapterResponseParameters parameters,
            BidResponse response
    ) {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        DisplayViewListener listener = ListenersCreator.createBannerListener(maxListener,
                () -> maxListener.onAdViewAdLoaded(adView)
        );

        if (activity != null) {
            LogUtil.info(TAG, "Prebid ad won: " + parameters.getThirdPartyAdPlacementId());
            activity.runOnUiThread(() -> {
                adView = new DisplayView(activity, listener, adConfiguration, response);
            });
        } else {
            String error = "Activity is null";
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1005, error));
        }
    }

    private void onError(
            int code,
            String error
    ) {
        if (maxListener != null) {
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(code, error));
        } else Log.e(TAG, "Max banner listener must be not null!");
    }

}
