package com.applovin.mediation.adapters.prebid.managers;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.Nullable;
import com.applovin.mediation.MaxReward;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.ParametersChecker;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.interstitial.rewarded.Reward;

public class MaxRewardedManager {

    private static final String TAG = MaxRewardedManager.class.getSimpleName();

    @Nullable
    private MaxRewardedAdapterListener maxListener;
    @Nullable
    private InterstitialController interstitialController;
    @Nullable
    private InterstitialControllerListener prebidListener;

    public void loadAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxRewardedAdapterListener maxListener
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
                prebidListener = createRewardedListener();
                interstitialController = new InterstitialController(activity, prebidListener);
                if (prebidListener != null) {
                    interstitialController.setRewardListener(prebidListener::onUserEarnedReward);
                }
                interstitialController.loadAd(responseId, true);
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


    public InterstitialControllerListener createRewardedListener() {
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
            public void onInterstitialFailedToLoad(AdException exception) {
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
                if (maxListener != null && interstitialController != null) {
                    Reward reward = interstitialController.getReward();
                    maxListener.onUserRewarded(new MaxReward() {
                        @Override
                        public String getLabel() {
                            if (reward != null) {
                                return reward.getType();
                            }
                            return "";
                        }

                        @Override
                        public int getAmount() {
                            if (reward != null) {
                                return reward.getCount();
                            }
                            return 0;
                        }
                    });
                }
            }
        };
    }

}
