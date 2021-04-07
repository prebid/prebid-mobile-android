package com.mopub.mobileads;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.mopub.common.LifecycleListener;
import com.mopub.common.logging.MoPubLog;
import com.openx.apollo.bidding.display.InterstitialController;
import com.openx.apollo.bidding.interfaces.InterstitialControllerListener;
import com.openx.apollo.errors.AdException;

public class OpenXApolloInterstitialAdapter extends BaseAd {
    private static final String TAG = OpenXApolloInterstitialAdapter.class.getSimpleName();
    private static final String KEY_BID_RESPONSE = "OPENX_BID_RESPONSE_ID";

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
        if (!adData.getExtras().containsKey(KEY_BID_RESPONSE)) {
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
            return;
        }

        initInterstitialController(context, adData.getExtras().get(KEY_BID_RESPONSE));
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
            mInterstitialController.loadAd(responseId, false);
        }
        catch (AdException e) {
            MoPubLog.log(MoPubLog.AdapterLogEvent.LOAD_FAILED, TAG);
            mLoadListener.onAdLoadFailed(MoPubErrorCode.ADAPTER_CONFIGURATION_ERROR);
        }
    }
}
