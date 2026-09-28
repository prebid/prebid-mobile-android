package org.prebid.mobile.admob;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAd;
import com.google.android.gms.ads.mediation.MediationRewardedAdCallback;
import com.google.android.gms.ads.mediation.MediationRewardedAdConfiguration;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.PluginRendererFactory;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

/**
 * Prebid rewarded adapter for AdMob integration.
 */
@Keep
public class PrebidRewardedAdapter extends PrebidBaseAdapter {

    public static final String EXTRA_RESPONSE_ID = "PrebidRewardedAdapterExtraId";

    @Nullable
    private PrebidMobileInterstitialControllerInterface interstitialController;
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

        BidResponse bidResponse = BidResponseCache.getInstance().popBidResponse(responseId);
        if (bidResponse == null) {
            adMobLoadListener.onFailure(AdErrors.noResponse(responseId));
            return;
        }

        InterstitialControllerListener prebidListener = getListener(adMobLoadListener);
        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(bidResponse.isVideo() ? AdFormat.VAST : AdFormat.INTERSTITIAL);
        config.setRewarded(true);
        config.getRewardManager().setRewardListener(prebidListener::onUserEarnedReward);

        interstitialController = PluginRendererFactory.createInterstitialController(
                configuration.getContext(), prebidListener, config, bidResponse
        );
        if (interstitialController == null) {
            adMobLoadListener.onFailure(AdErrors.interstitialControllerError("Renderer returned null controller"));
            return;
        }
        interstitialController.loadAd(config, bidResponse);
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

            @Override
            public void onUserEarnedReward() {
                if (rewardedAdCallback != null) {
                    rewardedAdCallback.onUserEarnedReward();
                }
            }
        };
    }

}
