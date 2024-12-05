package com.applovin.mediation.adapters.prebid.managers;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.Nullable;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.ListenersCreator;
import com.applovin.mediation.adapters.prebid.ParametersChecker;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

public class MaxInterstitialManager {

    private static final String TAG = MaxInterstitialManager.class.getSimpleName();

    @Nullable
    private MaxInterstitialAdapterListener maxListener;
    @Nullable
    private InterstitialController interstitialController;

    public void loadAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxInterstitialAdapterListener maxListener
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

        activity.runOnUiThread(() -> {
            try {
                InterstitialControllerListener listener = createPrebidListener();
                interstitialController = new InterstitialController(activity, listener);
                interstitialController.loadAd(responseId, false);
            } catch (AdException e) {
                String error = "Exception in Prebid interstitial controller (" + e.getMessage() + ")";
                Log.e(TAG, error);
                onError(1006, error);
            }
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
        if (maxListener != null) {
            return ListenersCreator.createInterstitialListener(maxListener);
        } else {
            Log.e(TAG, "Max interstitial listener must be not null!");
        }
        return null;
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
