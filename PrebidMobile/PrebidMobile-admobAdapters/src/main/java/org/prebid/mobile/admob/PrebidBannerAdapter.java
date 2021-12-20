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
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.DisplayView;
import org.prebid.mobile.rendering.bidding.listeners.DisplayViewListener;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.AdConfiguration;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Keep
public class PrebidBannerAdapter extends Adapter implements CustomEventBanner {


    private static final String TAG = "PrebidBannerAdapter";

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
        if (extras != null) {
            String responseId = extras.getString(PrebidAdMobRequest.EXTRA_RESPONSE_ID);
            BidResponse response = BidResponseCache.getInstance().popBidResponse(responseId);
            if (response != null) {
                AdConfiguration adConfiguration = new AdConfiguration();
                adConfiguration.setAdUnitIdentifierType(AdConfiguration.AdUnitIdentifierType.BANNER);
                DisplayViewListener listener = getListener(adMobListener);
                adView = new DisplayView(
                        context,
                        listener,
                        adConfiguration,
                        response
                );
            } else {
                String error = "There's no response for the response id: " + responseId;
                Log.e(TAG, error);
                adMobListener.onAdFailedToLoad(new AdError(1001, error, "prebid"));
            }
        } else {
            String error = "Extras are empty! Do you use PrebidAdMobRequest as fetchDemand() parameter?";
            Log.e(TAG, error);
            adMobListener.onAdFailedToLoad(new AdError(1002, error, "prebid"));
        }
    }

    @Override
    public void initialize(@NonNull Context context, @NonNull InitializationCompleteCallback initializationCompleteCallback, @NonNull List<MediationConfiguration> mediationList) {
        String adType = "Ad";
        if (mediationList.size() > 0) {
            adType = mediationList.get(0).getFormat().toString();
        }
        if (!mediationList.isEmpty()) {
            StringBuilder prices = new StringBuilder("Prices configured on server for " + adType + ": ");
            for (MediationConfiguration configuration : mediationList) {
                String serverParameter = configuration.getServerParameters().getString("parameter");
                String price = parseParameter(serverParameter);
                prices.append(price).append(" ");
            }
            Log.d(TAG, prices.toString());
        }
    }

    @NonNull
    @Override
    // TODO: Change
    public VersionInfo getVersionInfo() {
        return new VersionInfo(0, 0, 0);
    }

    @NonNull
    @Override
    public VersionInfo getSDKVersionInfo() {
        return new VersionInfo(0, 0, 0);
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

    private @Nullable
    String parseParameter(String serverParameter) {
        try {
            if (serverParameter.contains("hb_pb")) {
                Pattern pattern = Pattern.compile("\"(hb_pb)\":\"([\\d]+(\\.\\d{1,2})?)\"", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(serverParameter);
                if (matcher.find()) {
                    return matcher.group(2);
                } else {
                    Log.e(TAG, "Can't parse hb_pb value.");
                    return null;
                }
            } else {
                Log.e(TAG, "Server parameter doesn't contain hb_pb key.");
                return null;
            }
        } catch (Exception exception) {
            Log.e(TAG, "Can't parse server parameter. Exception: " + exception);
            return null;
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
