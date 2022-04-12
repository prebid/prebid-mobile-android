package com.applovin.mediation.adapters;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import com.applovin.mediation.MaxAdFormat;
import com.applovin.mediation.adapter.MaxAdViewAdapter;
import com.applovin.mediation.adapter.MaxAdapterError;
import com.applovin.mediation.adapter.listeners.MaxAdViewAdapterListener;
import com.applovin.mediation.adapter.parameters.MaxAdapterInitializationParameters;
import com.applovin.mediation.adapter.parameters.MaxAdapterResponseParameters;
import com.applovin.sdk.AppLovinSdk;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.units.configuration.AdFormat;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;

import java.util.HashMap;
import java.util.Map;

public class PrebidMAXMediationAdapter extends MediationAdapterBase implements MaxAdViewAdapter {

    public static final String TAG = PrebidMAXMediationAdapter.class.getSimpleName();
    public static final String EXTRA_RESPONSE_ID = TAG + "ExtraResponseId";

    private DisplayView adView;

    public PrebidMAXMediationAdapter(AppLovinSdk appLovinSdk) {
        super(appLovinSdk);
    }

    @Override
    public void initialize(
            MaxAdapterInitializationParameters maxAdapterInitializationParameters,
            Activity activity,
            OnCompletionListener onCompletionListener
    ) {
        Log.d(TAG, "Initializing");
        onCompletionListener.onCompletion(InitializationStatus.INITIALIZING, null);
        PrebidMobile.setApplicationContext(activity.getApplicationContext(), () -> {
            onCompletionListener.onCompletion(InitializationStatus.INITIALIZED_SUCCESS, null);
        });
    }

    /**
     * Loads Banner ad.
     */
    @Override
    public void loadAdViewAd(
            MaxAdapterResponseParameters parameters,
            MaxAdFormat maxAdFormat,
            Activity activity,
            MaxAdViewAdapterListener maxListener
    ) {
        if (parameters == null || parameters.getCustomParameters() == null || parameters.getLocalExtraParameters() == null) {
            String error = "Parameters are empty!";
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1001, error));
            return;
        }

        Bundle serverParameters = parameters.getCustomParameters();
        Map<String, Object> extras = parameters.getLocalExtraParameters();
        if (!extras.containsKey(EXTRA_RESPONSE_ID) || !(extras.get(EXTRA_RESPONSE_ID) instanceof String)) {
            String error = "Response id is null";
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1002, error));
            return;
        }

        String responseId = (String) extras.get(EXTRA_RESPONSE_ID);
        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (!ParametersMatcher.doParametersMatch(serverParameters, prebidParameters)) {
            String error = "Parameters don't match";
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1003, error));
            return;
        }

        BidResponse response = BidResponseCache.getInstance().popBidResponse(responseId);
        if (response == null) {
            String error = "There's no response for id: " + responseId;
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1004, error));
            return;
        }

        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        DisplayViewListener listener = getListener(maxListener);

        if (activity != null) {
            LogUtil.info(TAG, "Prebid ad won: " + parameters.getThirdPartyAdPlacementId());
            activity.runOnUiThread(() -> {
                adView = new DisplayView(activity, listener, adConfiguration, response);
            });
        } else {
            String error = "Activity is null";
            maxListener.onAdViewAdLoadFailed(new MaxAdapterError(1005, error));
        }
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
    public void onDestroy() {}

    private DisplayViewListener getListener(MaxAdViewAdapterListener maxListener) {
        return new DisplayViewListener() {
            @Override
            public void onAdLoaded() {
                maxListener.onAdViewAdLoaded(adView);
            }

            @Override
            public void onAdDisplayed() {
                maxListener.onAdViewAdDisplayed();
            }

            @Override
            public void onAdFailed(AdException exception) {
                maxListener.onAdViewAdDisplayFailed(new MaxAdapterError(1005, "Ad failed: " + exception.getMessage()));
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

}
