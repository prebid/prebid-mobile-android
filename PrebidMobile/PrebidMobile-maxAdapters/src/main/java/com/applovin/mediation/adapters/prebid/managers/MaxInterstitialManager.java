package com.applovin.mediation.adapters.prebid.managers;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.ListenersCreator;
import com.applovin.mediation.adapters.prebid.ParametersChecker;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PluginRendererFactory;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

public class MaxInterstitialManager {

    private static final String TAG = MaxInterstitialManager.class.getSimpleName();

    @Nullable
    private MaxInterstitialAdapterListener maxListener;
    @Nullable
    private PrebidMobileInterstitialControllerInterface interstitialController;

    public void loadAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            @NonNull MaxInterstitialAdapterListener maxListener
    ) {
        this.maxListener = maxListener;
        loadAd(parameters, activity);
    }

    private void loadAd(
            MaxAdapterResponseParameters parameters,
            Activity activity
    ) {
        String responseId = ParametersChecker.getResponseIdAndCheckKeywords(parameters, this::onError);
        if (responseId == null) {
            return;
        }

        BidResponse bidResponse = ParametersChecker.getBidResponse(responseId, this::onError);
        if (bidResponse == null) {
            return;
        }

        activity.runOnUiThread(() -> {
            AdUnitConfiguration config = new AdUnitConfiguration();
            config.setAdFormat(bidResponse.isVideo() ? AdFormat.VAST : AdFormat.INTERSTITIAL);
            InterstitialControllerListener listener = createPrebidListener();
            interstitialController = PluginRendererFactory.createInterstitialController(
                    activity, listener, config, bidResponse
            );
            if (interstitialController == null) {
                String error = "Renderer returned null interstitial controller";
                Log.e(TAG, error);
                onError(1006, error);
                return;
            }
            interstitialController.loadAd(config, bidResponse);
        });
    }

    public void showAd() {
        if (interstitialController == null) {
            MaxAdapterError error = new MaxAdapterError(2010, "InterstitialController is null");
            if (maxListener != null) {
                maxListener.onInterstitialAdDisplayFailed(error);
            }
            return;
        }
        interstitialController.show();
    }

    public void destroy() {
        if (interstitialController != null) {
            interstitialController.destroy();
        }
    }


    private InterstitialControllerListener createPrebidListener() {
        return ListenersCreator.createInterstitialListener(maxListener);
    }

    private void onError(
            int code,
            String error
    ) {
        if (maxListener != null) {
            maxListener.onInterstitialAdLoadFailed(new MaxAdapterError(code, error));
        } else Log.e(TAG, "Max interstitial listener must be not null!");
    }

}
