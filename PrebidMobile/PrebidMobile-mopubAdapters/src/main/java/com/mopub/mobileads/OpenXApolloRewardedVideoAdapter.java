package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.LifecycleListener;
import com.mopub.common.MoPubReward;
import com.mopub.common.logging.MoPubLog;
import com.openx.apollo.bidding.display.InterstitialController;
import com.openx.apollo.bidding.interfaces.InterstitialControllerListener;
import com.openx.apollo.errors.AdException;

public class OpenXApolloRewardedVideoAdapter extends BaseAd {
    private static final String TAG = OpenXApolloRewardedVideoAdapter.class.getSimpleName();

    private InterstitialController mInterstitialController;
    private InterstitialControllerListener mInterstitialListener = new InterstitialControllerListener() {
        @Override
        public void onInterstitialReadyForDisplay() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_SUCCESS, TAG);
            mLoadListener.onAdLoaded();
        }

        @Override
        public void onInterstitialClicked() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.CLICKED, TAG);
            mInteractionListener.onAdClicked();
        }

        @Override
        public void onInterstitialFailedToLoad(AdException exception) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.NO_FILL);
        }

        @Override
        public void onInterstitialDisplayed() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.SHOW_SUCCESS, TAG);
            mInteractionListener.onAdShown();
        }

        @Override
        public void onInterstitialClosed() {
            MoPubLog.log(MoPubLog.AdapterLogEvent.DID_DISAPPEAR, TAG);
            MoPubLog.log(MoPubLog.AdapterLogEvent.CUSTOM, TAG, "Display - adDidComplete");
            mInteractionListener.onAdComplete(MoPubReward.success(MoPubReward.NO_REWARD_LABEL, MoPubReward.DEFAULT_REWARD_AMOUNT));
            mInteractionListener.onAdDismissed();
        }
    };

    @Override
    protected void onInvalidate() {
        if (mInterstitialController != null) {
            mInterstitialController.destroy();
        }
    }

    @Nullable
    @Override
    protected LifecycleListener getLifecycleListener() {
        return null;
    }

    @NonNull
    @Override
    protected String getAdNetworkId() {
        return "";
    }

    @Override
    protected boolean checkAndInitializeSdk(
        @NonNull
            Activity launcherActivity,
        @NonNull
            AdData adData) {
        return false;
    }

    @Override
    protected void load(
        @NonNull
            Context context,
        @NonNull
            AdData adData) {
        if (TextUtils.isEmpty(adData.getAdUnit())) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        initInterstitialController(context, adData.getAdUnit());
    }

    @Override
    protected void show() {
        if (mInterstitialController != null) {
            mInterstitialController.show();
        }
    }

    private void initInterstitialController(Context context, String responseId) {
        try {
            mInterstitialController = new InterstitialController(context, mInterstitialListener);
            mInterstitialController.loadAd(responseId, true);
        }
        catch (AdException e) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }
}
