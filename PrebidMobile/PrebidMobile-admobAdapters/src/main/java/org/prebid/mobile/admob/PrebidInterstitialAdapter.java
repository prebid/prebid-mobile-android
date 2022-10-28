package org.prebid.mobile.admob;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.*;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

import java.util.HashMap;

public class PrebidInterstitialAdapter extends PrebidBaseAdapter implements MediationInterstitialAd {

    public static final String EXTRA_RESPONSE_ID = "PrebidInterstitialAdapterExtraId";

    @Nullable
    private InterstitialController interstitialController;
    @Nullable
    private MediationInterstitialAdCallback adMobInterstitialListener;

    @Override
    public void loadInterstitialAd(
            @NonNull MediationInterstitialAdConfiguration configuration,
            @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> loadListener
    ) {
        Bundle extras = configuration.getMediationExtras();
        String serverParameter = configuration.getServerParameters().getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
        String responseId = extras.getString(EXTRA_RESPONSE_ID);
        if (responseId == null) {
            String error = "Response id is null";
            loadListener.onFailure(new AdError(1002, error, "prebid"));
            return;
        }

        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (!ParametersMatcher.doParametersMatch(serverParameter, prebidParameters)) {
            String error = "Parameters are different";
            loadListener.onFailure(new AdError(1003, error, "prebid"));
            return;
        }
        LogUtil.verbose(TAG, "Parameters are matched! (" + serverParameter + ")");

        try {
            InterstitialControllerListener listener = getListener(loadListener);
            interstitialController = new InterstitialController(configuration.getContext(), listener);
            interstitialController.loadAd(responseId, false);
        } catch (AdException e) {
            String error = "Exception in Prebid interstitial controller (" + e.getMessage() + ")";
            Log.e(TAG, error);
            loadListener.onFailure(new AdError(1004, error, "prebid"));
        }
    }


    private InterstitialControllerListener getListener(
            MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> loadListener
    ) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                adMobInterstitialListener = loadListener.onSuccess(PrebidInterstitialAdapter.this);
            }

            @Override
            public void onInterstitialDisplayed() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.onAdOpened();
                }
            }

            @Override
            public void onInterstitialClicked() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.onAdOpened();
                    adMobInterstitialListener.reportAdClicked();
                }
            }

            @Override
            public void onInterstitialClosed() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.onAdClosed();
                }
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                String error = "Failed to load ad: " + exception.getMessage();
                Log.e(TAG, error);
                loadListener.onFailure(new AdError(1005, error, "prebid"));
            }
        };
    }

    @Override
    public void showAd(@NonNull Context context) {
        if (interstitialController != null) {
            interstitialController.show();
        }
    }

}
