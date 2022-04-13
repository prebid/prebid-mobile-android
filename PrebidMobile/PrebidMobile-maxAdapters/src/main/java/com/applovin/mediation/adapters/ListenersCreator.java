package com.applovin.mediation.adapters;

import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;

public class ListenersCreator {

    public static DisplayViewListener createBannerListener(
            MaxAdViewAdapterListener maxListener,
            OnBannerAdViewLoaded listener
    ) {
        return new DisplayViewListener() {
            @Override
            public void onAdLoaded() {
                listener.run();
            }

            @Override
            public void onAdDisplayed() {
                maxListener.onAdViewAdDisplayed();
            }

            @Override
            public void onAdFailed(AdException exception) {
                maxListener.onAdViewAdDisplayFailed(new MaxAdapterError(2001, "Ad failed: " + exception.getMessage()));
            }

            @Override
            public void onAdClicked() {
                maxListener.onAdViewAdClicked();
                maxListener.onAdViewAdExpanded();
            }

            @Override
            public void onAdClosed() {
                maxListener.onAdViewAdCollapsed();
                maxListener.onAdViewAdHidden();
            }
        };
    }

    /**
     * This callback created for calling MAX onAdViewAdLoaded listener from outside
     * because it needs ad view parameter when ad will be loaded.
     */
    interface OnBannerAdViewLoaded {

        void run();

    }


    public static InterstitialControllerListener createInterstitialListener(MaxInterstitialAdapterListener maxListener) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                maxListener.onInterstitialAdLoaded();
            }

            @Override
            public void onInterstitialClicked() {
                maxListener.onInterstitialAdClicked();
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                maxListener.onInterstitialAdLoadFailed(new MaxAdapterError(2002,
                        "Ad failed: " + exception.getMessage()
                ));
            }

            @Override
            public void onInterstitialDisplayed() {
                maxListener.onInterstitialAdDisplayed();
            }

            @Override
            public void onInterstitialClosed() {
                maxListener.onInterstitialAdHidden();
            }
        };
    }

    public static InterstitialControllerListener createRewardedListener(MaxRewardedAdapterListener maxListener) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                maxListener.onRewardedAdLoaded();
            }

            @Override
            public void onInterstitialClicked() {
                maxListener.onRewardedAdClicked();
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                maxListener.onRewardedAdLoadFailed(new MaxAdapterError(2002, "Ad failed: " + exception.getMessage()));
            }

            @Override
            public void onInterstitialDisplayed() {
                maxListener.onRewardedAdDisplayed();
            }

            @Override
            public void onInterstitialClosed() {
                maxListener.onRewardedAdHidden();
            }
        };
    }

}
