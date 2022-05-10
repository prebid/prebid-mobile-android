package org.prebid.mobile.admob;

import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

import java.util.HashMap;

@Keep
public class PrebidRewardedAdapter extends PrebidBaseAdapter {

    private static final String TAG = "PrebidRewardedAdapter";
    private static final String CLASS_NAME = "org.prebid.mobile.admob.PrebidRewardedAdapter";
    public static final String EXTRA_RESPONSE_ID = "PrebidRewardedAdapterExtraId";

    private InterstitialController interstitialController;
    private MediationRewardedAdCallback rewardedAdCallback;

    @Override
    public void loadRewardedAd(
            @NonNull MediationRewardedAdConfiguration adConfiguration,
            @NonNull MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> callback
    ) {
        Bundle serverParameters = adConfiguration.getServerParameters();
        String adMobParameters = serverParameters.getString("parameter");
        String adMobClassName = serverParameters.getString("class_name");

        if (!adMobClassName.equals(CLASS_NAME)) {
            String error = "Class name is different";
            callback.onFailure(new AdError(1001, error, "prebid"));
            return;
        }

        String responseId = adConfiguration.getMediationExtras().getString(EXTRA_RESPONSE_ID);
        if (responseId == null) {
            String error = "Empty response id";
            callback.onFailure(new AdError(1002, error, "prebid"));
            return;
        }

        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (prebidParameters == null) {
            String error = "Prebid keywords are empty";
            callback.onFailure(new AdError(1003, error, "prebid"));
            return;
        }

        if (!ParametersMatcher.doParametersMatch(adMobParameters, prebidParameters)) {
            String error = "Parameters are different";
            callback.onFailure(new AdError(1004, error, "prebid"));
            return;
        }

        try {
            InterstitialControllerListener listener = getListener(callback);
            interstitialController = new InterstitialController(adConfiguration.getContext(), listener);
            interstitialController.loadAd(responseId, true);
        } catch (AdException e) {
            String error = "Exception in Prebid interstitial controller (" + e.getMessage() + ")";
            Log.e(TAG, error);
            callback.onFailure(new AdError(1005, error, "prebid"));
        }
    }

    private InterstitialControllerListener getListener(MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> adMobCallback) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                rewardedAdCallback = adMobCallback.onSuccess(context -> {
                    interstitialController.show();
                });
            }

            @Override
            public void onInterstitialDisplayed() {
                rewardedAdCallback.onAdOpened();
                rewardedAdCallback.onVideoStart();
                rewardedAdCallback.reportAdImpression();
            }

            @Override
            public void onInterstitialClicked() {
                rewardedAdCallback.reportAdClicked();
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                String errorMessage = exception.getMessage() != null ? exception.getMessage() : "Failed to load ad";
                AdError adError = new AdError(1006, errorMessage, "prebid");
                adMobCallback.onFailure(adError);
                if (rewardedAdCallback != null) {
                    rewardedAdCallback.onAdFailedToShow(adError);
                }
            }

            @Override
            public void onInterstitialClosed() {
                rewardedAdCallback.onVideoComplete();
                rewardedAdCallback.onAdClosed();
            }
        };
    }

}
