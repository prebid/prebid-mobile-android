package com.applovin.mediation.adapters;

import android.app.Activity;

import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxInterstitialAdapter;
import com.applovin.mediation.adapter.MaxNativeAdAdapter;
import com.applovin.mediation.adapter.MaxRewardedAdapter;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxInterstitialAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxNativeAdAdapterListener;
import com.applovin.mediation.adapter.listeners.MaxRewardedAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.mediation.adapters.prebid.managers.MaxBannerManager;
import com.applovin.mediation.adapters.prebid.managers.MaxInterstitialManager;
import com.applovin.mediation.adapters.prebid.managers.MaxNativeManager;
import com.applovin.sdk.AppLovinSdk;

import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.TargetingParams;
import org.prebid.mobile.api.exceptions.InitError;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;

public class PrebidMaxMediationAdapter extends MediationAdapterBase implements MaxAdViewAdapter, MaxInterstitialAdapter, MaxRewardedAdapter, MaxNativeAdAdapter {

    public static final String TAG = PrebidMaxMediationAdapter.class.getSimpleName();
    public static final String EXTRA_RESPONSE_ID = TAG + "ExtraResponseId";
    public static final String EXTRA_KEYWORDS_ID = TAG + "ExtraKeywordsId";

    private MaxBannerManager maxBannerManager;
    private MaxInterstitialManager maxInterstitialManager;
    private MaxNativeManager maxNativeManager;

    public PrebidMaxMediationAdapter(AppLovinSdk appLovinSdk) {
        super(appLovinSdk);
    }

    @Override
    public void initialize(
            MaxAdapterInitializationParameters parameters,
            Activity activity,
            OnCompletionListener onCompletionListener
    ) {
        setConsents(parameters);
        if (PrebidMobile.isSdkInitialized()) {
            onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
        } else {
            onCompletionListener.onCompletion(InitializationStatus.INITIALIZING, null);
            PrebidMobile.initializeSdk(activity.getApplicationContext(), new SdkInitializationListener() {
                @Override
                public void onSdkInit() {
                    onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
                }

                @Override
                public void onSdkFailedToInit(InitError error) {}
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
        maxBannerManager = new MaxBannerManager();
        maxBannerManager.loadAd(parameters, maxAdFormat, activity, listener);
    }


    @Override
    public void loadInterstitialAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxInterstitialAdapterListener maxListener
    ) {
        maxInterstitialManager = new MaxInterstitialManager();
        maxInterstitialManager.loadAd(parameters, activity, maxListener);
    }

    @Override
    public void loadRewardedAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxRewardedAdapterListener maxListener
    ) {
        maxInterstitialManager = new MaxInterstitialManager();
        maxInterstitialManager.loadAd(parameters, activity, maxListener);
    }

    @Override
    public void showInterstitialAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxInterstitialAdapterListener maxListener
    ) {
        maxInterstitialManager.showAd();
    }


    @Override
    public void showRewardedAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxRewardedAdapterListener maxListener
    ) {
        maxInterstitialManager.showAd();
    }


    @Override
    public void loadNativeAd(
            MaxAdapterResponseParameters parameters,
            Activity activity,
            MaxNativeAdAdapterListener maxListener
    ) {
        maxNativeManager = new MaxNativeManager();
        maxNativeManager.loadAd(parameters, activity, maxListener);
    }


    @Override
    public void onDestroy() {
        if (maxBannerManager != null) {
            maxBannerManager.destroy();
        }

        if (maxInterstitialManager != null) {
            maxInterstitialManager.destroy();
        }

        if (maxNativeManager != null) {
            maxNativeManager.destroy();
        }
    }

    @Override
    public String getAdapterVersion() {
        return "1.15.0-beta1";
    }

    @Override
    public String getSdkVersion() {
        return PrebidMobile.SDK_VERSION;
    }


    private void setConsents(MaxAdapterInitializationParameters parameters) {
        if (parameters != null) {
            Boolean ageRestrictedUser = parameters.isAgeRestrictedUser();
            if (ageRestrictedUser != null) {
                TargetingParams.setSubjectToCOPPA(ageRestrictedUser);
            }
        }
    }

}
