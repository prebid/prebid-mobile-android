package org.prebid.mobile.admob;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;

import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

@Keep
public class PrebidRewardedAdapter extends PrebidBaseAdapter {

    public static final String EXTRA_RESPONSE_ID = "PrebidRewardedAdapterExtraId";

    @Nullable
    private InterstitialController interstitialController;
    @Nullable
    private MediationRewardedAdCallback rewardedAdCallback;

    @Override
    public void loadRewardedAd(
            @NonNull MediationRewardedAdConfiguration configuration,
            @NonNull MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> adMobLoadListener
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
            interstitialController.loadAd(responseId, true);
        } catch (AdException e) {
            adMobLoadListener.onFailure(AdErrors.interstitialControllerError(e.getMessage()));
        }
    }

    private InterstitialControllerListener getListener(
            MediationAdLoadCallback<MediationRewardedAd, MediationRewardedAdCallback> adMobCallback
    ) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                rewardedAdCallback = adMobCallback.onSuccess(context -> {
                    if (interstitialController != null) {
                        interstitialController.show();
                    }
                });
            }

            @Override
            public void onInterstitialDisplayed() {
                if (rewardedAdCallback != null) {
                    rewardedAdCallback.reportAdImpression();
                    rewardedAdCallback.onAdOpened();
                    rewardedAdCallback.onVideoStart();
                }
            }

            @Override
            public void onInterstitialClicked() {
                if (rewardedAdCallback != null) {
                    rewardedAdCallback.reportAdClicked();
                }
            }

            @Override
            public void onInterstitialClosed() {
                if (rewardedAdCallback != null) {
                    rewardedAdCallback.onVideoComplete();
                    rewardedAdCallback.onAdClosed();
                }
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                adMobCallback.onFailure(AdErrors.failedToLoadAd(
                        exception.getMessage() != null ? exception.getMessage() : "Failed to load ad"
                ));
            }
        };
    }

}
