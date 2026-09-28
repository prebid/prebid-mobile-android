package com.applovin.mediation.adapters.prebid.managers;

import android.app.Activity;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.applovin.mediation.MaxReward;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.ParametersChecker;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PluginRendererFactory;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.interstitial.rewarded.Reward;

public class MaxRewardedManager {

    private static final String TAG = MaxRewardedManager.class.getSimpleName();

    @Nullable
    private MaxRewardedAdapterListener maxListener;
    @Nullable
    private PrebidMobileInterstitialControllerInterface interstitialController;
    @Nullable
    private AdUnitConfiguration adUnitConfiguration;

    public void loadAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            @NonNull MaxRewardedAdapterListener maxListener
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
            InterstitialControllerListener prebidListener = createRewardedListener();
            adUnitConfiguration = new AdUnitConfiguration();
            adUnitConfiguration.setAdFormat(bidResponse.isVideo() ? AdFormat.VAST : AdFormat.INTERSTITIAL);
            adUnitConfiguration.setRewarded(true);
            adUnitConfiguration.getRewardManager().setRewardListener(prebidListener::onUserEarnedReward);

            interstitialController = PluginRendererFactory.createInterstitialController(
                    activity, prebidListener, adUnitConfiguration, bidResponse
            );
            if (interstitialController == null) {
                String error = "Renderer returned null rewarded controller";
                Log.e(TAG, error);
                onError(1006, error);
                return;
            }
            interstitialController.loadAd(adUnitConfiguration, bidResponse);
        });
    }

    public void showAd() {
        if (interstitialController == null) {
            MaxAdapterError error = new MaxAdapterError(2010, "InterstitialController is null");
            if (maxListener != null) {
                maxListener.onRewardedAdDisplayFailed(error);
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


    private void onError(
            int code,
            String error
    ) {
        if (maxListener != null) {
            maxListener.onRewardedAdLoadFailed(new MaxAdapterError(code, error));
        } else Log.e(TAG, "Max interstitial listener must be not null!");
    }


    private InterstitialControllerListener createRewardedListener() {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                if (maxListener != null) {
                    maxListener.onRewardedAdLoaded();
                }
            }

            @Override
            public void onInterstitialClicked() {
                if (maxListener != null) {
                    maxListener.onRewardedAdClicked();
                }
            }

            @Override
            public void onInterstitialFailedToLoad(org.prebid.mobile.api.exceptions.AdException exception) {
                if (maxListener != null) {
                    maxListener.onRewardedAdLoadFailed(new MaxAdapterError(2002,
                            "Ad failed: " + exception.getMessage()
                    ));
                }
            }

            @Override
            public void onInterstitialDisplayed() {
                if (maxListener != null) {
                    maxListener.onRewardedAdDisplayed();
                }
            }

            @Override
            public void onInterstitialClosed() {
                if (maxListener != null) {
                    maxListener.onRewardedAdHidden();
                }
            }

            @Override
            public void onUserEarnedReward() {
                if (maxListener != null) {
                    Reward reward = adUnitConfiguration != null
                            ? adUnitConfiguration.getRewardManager().getRewardedExt().getReward()
                            : null;
                    maxListener.onUserRewarded(new MaxReward() {
                        @Override
                        public String getLabel() {
                            return reward != null ? reward.getType() : "";
                        }

                        @Override
                        public int getAmount() {
                            return reward != null ? reward.getCount() : 0;
                        }
                    });
                }
            }
        };
    }

}
