package org.prebid.mobile.admob;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;

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

public class PrebidBannerAdapter extends PrebidBaseAdapter implements CustomEventBanner {

    private static final String TAG = "PrebidBannerAdapter";
    public static final String EXTRA_RESPONSE_ID = "PrebidBannerAdapterExtraId";

    private DisplayView adView;

    @Override
    public void requestBannerAd(
            @NonNull Context context,
            @NonNull CustomEventBannerListener adMobListener,
            @Nullable String serverParameter,
            @NonNull AdSize adSize,
            @NonNull MediationAdRequest mediationAdRequest,
            @Nullable Bundle extras
    ) {
        if (extras == null) {
            String error = "Extras are empty! Check if you add custom event extras bundle to  " + TAG;
            Log.e(TAG, error);
            adMobListener.onAdFailedToLoad(new AdError(1001, error, "prebid"));
            return;
        }

        String responseId = extras.getString(EXTRA_RESPONSE_ID);
        if (responseId == null) {
            String error = "Response id is null";
            adMobListener.onAdFailedToLoad(new AdError(1002, error, "prebid"));
            return;
        }

        HashMap<String, String> prebidParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (!ParametersMatcher.doParametersMatch(serverParameter, prebidParameters)) {
            String error = "Parameters are different";
            adMobListener.onAdFailedToLoad(new AdError(1003, error, "prebid"));
            return;
        }
        LogUtil.verbose(TAG, "Parameters are matched! (" + serverParameter + ")");

        BidResponse response = BidResponseCache.getInstance().popBidResponse(responseId);
        if (response == null) {
            String error = "There's no response for the response id: " + responseId;
            adMobListener.onAdFailedToLoad(new AdError(1004, error, "prebid"));
            return;
        }

        AdUnitConfiguration adConfiguration = new AdUnitConfiguration();
        adConfiguration.setAdFormat(AdFormat.BANNER);
        DisplayViewListener listener = getListener(adMobListener);
        adView = new DisplayView(
                context,
                listener,
                adConfiguration,
                response
        );
    }
    @Override
    public void onResume() {

    }

    @Override
    public void onPause() {

    }

    @Override
    public void onDestroy() {
        if (adView != null) {
            adView.destroy();
        }
    }

    @NonNull
    private DisplayViewListener getListener(CustomEventBannerListener adMobListener) {
        return new DisplayViewListener() {
            @Override
            public void onAdLoaded() {
                adMobListener.onAdLoaded(adView);
            }

            @Override
            public void onAdDisplayed() {

            }

            @Override
            public void onAdFailed(AdException exception) {
                String message = exception.getMessage();
                if (message == null) message = "Failed to load DisplayView ad";
                adMobListener.onAdFailedToLoad(new AdError(1010, message, "prebid"));
            }

            @Override
            public void onAdClicked() {
                adMobListener.onAdClicked();
                adMobListener.onAdOpened();
            }

            @Override
            public void onAdClosed() {
                adMobListener.onAdClosed();
            }
        };
    }

}
