package org.prebid.mobile.admob;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.mediation.MediationAdRequest;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitial;
import com.google.android.gms.ads.mediation.customevent.CustomEventInterstitialListener;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.ParametersMatcher;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.InterstitialController;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

import java.util.HashMap;

public class PrebidInterstitialAdapter extends PrebidBaseAdapter implements CustomEventInterstitial {

    private static final String TAG = "PrebidInterstitial";
    public static final String EXTRA_RESPONSE_ID = "PrebidInterstitialAdapterExtraId";

    private InterstitialController interstitialController;

    @Override
    public void requestInterstitialAd(
            @NonNull Context context,
            @NonNull CustomEventInterstitialListener adMobListener,
            @Nullable String serverParameter,
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

        try {
            InterstitialControllerListener listener = getListener(adMobListener);
            interstitialController = new InterstitialController(context, listener);
            interstitialController.loadAd(responseId, false);
        } catch (AdException e) {
            String error = "Exception in Prebid interstitial controller (" + e.getMessage() + ")";
            Log.e(TAG, error);
            adMobListener.onAdFailedToLoad(new AdError(1004, error, "prebid"));
        }

    }

    @Override
    public void showInterstitial() {
        interstitialController.show();
    }

    @Override
    public void onDestroy() {
        interstitialController.destroy();
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    private InterstitialControllerListener getListener(CustomEventInterstitialListener adMobListener) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                adMobListener.onAdLoaded();
            }

            @Override
            public void onInterstitialDisplayed() {
                adMobListener.onAdOpened();
            }

            @Override
            public void onInterstitialClicked() {
                adMobListener.onAdClicked();
            }

            @Override
            public void onInterstitialClosed() {
                adMobListener.onAdClosed();
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                String error = "Failed to load ad: " + exception.getMessage();
                Log.e(TAG, error);
                adMobListener.onAdFailedToLoad(new AdError(1005, error, "prebid"));
            }
        };
    }

}
