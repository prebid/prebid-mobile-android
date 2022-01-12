package org.prebid.mobile.admob;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.mediation.*;
import com.google.android.gms.ads.mediation.customevent.CustomEventBanner;
import com.google.android.gms.ads.mediation.customevent.CustomEventBannerListener;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;

import java.util.HashMap;
import java.util.List;

@Keep
public class PrebidBannerAdapter extends Adapter implements CustomEventBanner {

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

        HashMap<String, String> adMobParameters = BidResponseCache.getInstance().getKeywords(responseId);
        if (!ParametersMatcher.doParametersMatch(serverParameter, adMobParameters)) {
            String error = "Parameters are different";
            adMobListener.onAdFailedToLoad(new AdError(1003, error, "prebid"));
            return;
        }
        LogUtil.v(TAG, "Parameters are matched! (" + serverParameter + ")");

        BidResponse response = BidResponseCache.getInstance().popBidResponse(responseId);
        if (response == null) {
            String error = "There's no response for the response id: " + responseId;
            adMobListener.onAdFailedToLoad(new AdError(1004, error, "prebid"));
            return;
        }

        AdConfiguration adConfiguration = new AdConfiguration();
        adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
        DisplayViewListener listener = getListener(adMobListener);
        adView = new DisplayView(
                context,
                listener,
                adConfiguration,
                response
        );
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> mediationList) {

    }

    @NonNull
    @Override
    public VersionInfo getVersionInfo() {
        return new VersionInfo(1, 0, 0);
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return new VersionInfo(1, 13, 0);
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
            }

            @Override
            public void onAdClosed() {
                adMobListener.onAdClosed();
            }
        };
    }

}
