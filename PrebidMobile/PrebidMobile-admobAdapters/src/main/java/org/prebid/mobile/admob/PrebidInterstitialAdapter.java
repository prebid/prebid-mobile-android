package org.prebid.mobile.admob;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

public class PrebidInterstitialAdapter extends PrebidBaseAdapter {

    public static final String EXTRA_RESPONSE_ID = "PrebidInterstitialAdapterExtraId";

    @Nullable
    private InterstitialController interstitialController;
    @Nullable
    private MediationInterstitialAdCallback adMobInterstitialListener;

    @Override
    public void loadInterstitialAd(
            @NonNull MediationInterstitialAdConfiguration configuration,
            @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adMobLoadListener
    ) {
        String responseId = getResponseIdAndCheckParameters(
                configuration,
                EXTRA_RESPONSE_ID,
                adMobLoadListener::onFailure
        );
        if (responseId == null) {
            return;
        }

        try {
            InterstitialControllerListener listener = getListener(adMobLoadListener);
            interstitialController = new InterstitialController(configuration.getContext(), listener);
            interstitialController.loadAd(responseId, false);
        } catch (AdException e) {
            adMobLoadListener.onFailure(AdErrors.interstitialControllerError(e.getMessage()));
        }
    }


    private InterstitialControllerListener getListener(
            MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> loadListener
    ) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                adMobInterstitialListener = loadListener.onSuccess(context -> {
                    if (interstitialController != null) {
                        interstitialController.show();
                    }
                });
            }

            @Override
            public void onInterstitialDisplayed() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.reportAdImpression();
                    adMobInterstitialListener.onAdOpened();
                }
            }

            @Override
            public void onInterstitialClicked() {
                if (adMobInterstitialListener != null) {
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
                loadListener.onFailure(AdErrors.failedToLoadAd(exception.getMessage()));
            }
        };
    }

}
