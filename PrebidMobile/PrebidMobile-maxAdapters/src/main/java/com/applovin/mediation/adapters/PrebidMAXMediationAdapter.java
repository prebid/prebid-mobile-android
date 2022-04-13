package com.applovin.mediation.adapters;

import android.app.Activity;
import android.util.Log;
import androidx.annotation.Keep;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxRewardedAdapter;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.sdk.AppLovinSdk;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.units.configuration.AdFormat;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

@Keep
public class PrebidMAXMediationAdapter extends MediationAdapterBase implements MaxAdViewAdapter, MaxInterstitialAdapter, MaxRewardedAdapter {

    public static final String TAG = PrebidMAXMediationAdapter.class.getSimpleName();
    public static final String EXTRA_RESPONSE_ID = TAG + "ExtraResponseId";

    private DisplayView adView;
    private InterstitialController interstitialController;

    private MaxAdViewAdapterListener bannerListener;
    private MaxInterstitialAdapterListener interstitialListener;
    private MaxRewardedAdapterListener rewardedListener;

    public PrebidMAXMediationAdapter(AppLovinSdk appLovinSdk) {
        super(appLovinSdk);
    }

    @Override
    public void initialize(
            MaxAdapterInitializationParameters maxAdapterInitializationParameters,
            Activity activity,
            OnCompletionListener onCompletionListener
    ) {
        if (PrebidMobile.isSdkInitialized()) {
            onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
        } else {
            onCompletionListener.onCompletion(InitializationStatus.INITIALIZING, null);
            PrebidMobile.setApplicationContext(activity.getApplicationContext(), () -> {
                onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
            });
        }
    }

    @Override
    public void loadAdViewAd(
            MaxAdapterResponseParameters parameters,
            MaxAdFormat maxAdFormat,
            Activity activity,
            MaxAdViewAdapterListener listener
    ) {
        bannerListener = listener;

        String responseId = ParametersChecker.getResponseId(parameters, this::onBannerError);
        BidResponse bidResponse = ParametersChecker.getBidResponse(responseId, this::onBannerError);
        if (bidResponse == null) {
            return;
        }

        switch (maxAdFormat.getLabel()) {
            case "BANNER":
                showBanner(activity, parameters, bidResponse);
                break;
            default:
                String error = "Unknown type of MAX ad!";
                Log.e(TAG, error);
                bannerListener.onAdViewAdLoadFailed(new MaxAdapterError(1005, error));
        }
    }

    private void showBanner(
            Activity activity,
            MaxAdapterResponseParameters parameters,
            BidResponse response
    ) {
        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        DisplayViewListener listener = ListenersCreator.createBannerListener(bannerListener,
                () -> bannerListener.onAdViewAdLoaded(adView)
        );

        if (activity != null) {
            LogUtil.info(TAG, "Prebid ad won: " + parameters.getThirdPartyAdPlacementId());
            activity.runOnUiThread(() -> {
                adView = new DisplayView(activity, listener, adConfiguration, response);
            });
        } else {
            String error = "Activity is null";
            bannerListener.onAdViewAdLoadFailed(new MaxAdapterError(1005, error));
        }
    }


    @Override
    public void loadInterstitialAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxInterstitialAdapterListener maxListener
    ) {
        interstitialListener = maxListener;

        String responseId = ParametersChecker.getResponseId(parameters, this::onInterstitialError);
        if (responseId == null) {
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                InterstitialControllerListener listener = ListenersCreator.createInterstitialListener(maxListener);
                interstitialController = new InterstitialController(activity, listener);
                interstitialController.loadAd(responseId, false);
            } catch (AdException e) {
                String error = "Exception in Prebid interstitial controller (" + e.getMessage() + ")";
                Log.e(TAG, error);
                onInterstitialError(1006, error);
            }
        });
    }

    @Override
    public void showInterstitialAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxInterstitialAdapterListener maxListener
    ) {
        if (interstitialController == null) {
            maxListener.onInterstitialAdDisplayFailed(new MaxAdapterError(2002, "InterstitialController is null"));
            return;
        }
        interstitialController.show();
    }


    @Override
    public void loadRewardedAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxRewardedAdapterListener maxListener
    ) {
        rewardedListener = maxListener;

        String responseId = ParametersChecker.getResponseId(parameters, this::onRewardedError);
        if (responseId == null) {
            return;
        }

        activity.runOnUiThread(() -> {
            try {
                InterstitialControllerListener listener = ListenersCreator.createRewardedListener(maxListener);
                interstitialController = new InterstitialController(activity, listener);
                interstitialController.loadAd(responseId, true);
            } catch (AdException e) {
                String error = "Exception in Prebid rewarded controller (" + e.getMessage() + ")";
                Log.e(TAG, error);
                onRewardedError(1007, error);
            }
        });
    }

    @Override
    public void showRewardedAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxRewardedAdapterListener maxListener
    ) {
        if (interstitialController == null) {
            maxListener.onRewardedAdDisplayFailed(new MaxAdapterError(2003, "InterstitialController is null"));
            return;
        }
        interstitialController.show();
    }


    @Override
    public String getSdkVersion() {
        return PrebidMobile.SDK_VERSION;
    }

    @Override
    public String getAdapterVersion() {
        return "1.15.0-beta1";
    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }

        if (interstitialController != null) {
            interstitialController.destroy();
        }
    }


    private void onBannerError(
            int code,
            String error
    ) {
        if (bannerListener != null) {
            bannerListener.onAdViewAdLoadFailed(new MaxAdapterError(code, error));
        }
    }

    private void onInterstitialError(
            int code,
            String error
    ) {
        if (interstitialListener != null) {
            interstitialListener.onInterstitialAdLoadFailed(new MaxAdapterError(code, error));
        }
    }

    private void onRewardedError(
            int code,
            String error
    ) {
        if (rewardedListener != null) {
            rewardedListener.onRewardedAdLoadFailed(new MaxAdapterError(code, error));
        }
    }

}
