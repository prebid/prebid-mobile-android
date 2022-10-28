package org.prebid.mobile.admob;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.*;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.DisplayView;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;

import java.util.HashMap;

public class PrebidBannerAdapter extends PrebidBaseAdapter {

    public static final String EXTRA_RESPONSE_ID = "PrebidBannerAdapterExtraId";

    private DisplayView adView;
    @Nullable
    private MediationBannerAdCallback adMobBannerListener;

    @Override
    public void loadBannerAd(
            @NonNull MediationBannerAdConfiguration configuration,
            @NonNull MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> adMobLoadListener
    ) {
        Bundle extras = configuration.getMediationExtras();
        String serverParameter = configuration.getServerParameters().getString(MediationConfiguration.CUSTOM_EVENT_SERVER_PARAMETER_FIELD);
        String responseId = extras.getString(EXTRA_RESPONSE_ID);
        if (responseId == null) {
            String error = "Response id is null";
            adMobLoadListener.onFailure(new AdError(1002, error, "prebid"));
            return;
        }

        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (!ParametersMatcher.doParametersMatch(serverParameter, prebidParameters)) {
            String error = "Parameters are different";
            adMobLoadListener.onFailure(new AdError(1003, error, "prebid"));
            return;
        }
        LogUtil.verbose(TAG, "Parameters are matched! (" + serverParameter + ")");

        BidResponse response = BidResponseCache.getInstance().popBidResponse(responseId);
        if (response == null) {
            String error = "There's no response for the response id: " + responseId;
            adMobLoadListener.onFailure(new AdError(1004, error, "prebid"));
            return;
        }

        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        DisplayViewListener listener = getPrebidListener(adMobLoadListener);
        adView = new DisplayView(
                configuration.getContext(),
                listener,
                adConfiguration,
                response
        );
    }

    @NonNull
    private DisplayViewListener getPrebidListener(
            MediationAdLoadCallback<MediationBannerAd, MediationBannerAdCallback> adMobLoadListener
    ) {
        return new DisplayViewListener() {

            @Override
            public void onAdLoaded() {
                adMobBannerListener = adMobLoadListener.onSuccess(() -> adView);
            }

            @Override
            public void onAdDisplayed() {
                if (adMobBannerListener != null) {
                    adMobBannerListener.reportAdImpression();
                }
            }

            @Override
            public void onAdClicked() {
                if (adMobBannerListener != null) {
                    adMobBannerListener.onAdOpened();
                    adMobBannerListener.reportAdClicked();
                }
            }

            @Override
            public void onAdClosed() {
                if (adMobBannerListener != null) {
                    adMobBannerListener.onAdClosed();
                }
            }

            @Override
            public void onAdFailed(AdException exception) {
                String message = exception.getMessage();
                if (message == null) message = "Failed to load DisplayView ad";
                adMobLoadListener.onFailure(new AdError(1010, message, "prebid"));
            }

        };
    }

}
