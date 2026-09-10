package org.prebid.mobile.admob;

import androidx.annotation.Keep;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.ads.mediation.MediationAdLoadCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAd;
import com.google.android.gms.ads.mediation.MediationInterstitialAdCallback;
import com.google.android.gms.ads.mediation.MediationInterstitialAdConfiguration;

import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.api.rendering.PrebidMobileInterstitialControllerInterface;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.BidResponseCache;
import org.prebid.mobile.rendering.bidding.display.PluginRendererFactory;
import org.prebid.mobile.rendering.bidding.interfaces.InterstitialControllerListener;

/**
 * Prebid interstitial adapter for AdMob integration.
 */
@Keep
public class PrebidInterstitialAdapter extends PrebidBaseAdapter {

    public static final String EXTRA_RESPONSE_ID = "PrebidInterstitialAdapterExtraId";

    @Nullable
    private PrebidMobileInterstitialControllerInterface interstitialController;
    @Nullable
    private MediationInterstitialAdCallback adMobInterstitialListener;

    @Override
    public void loadInterstitialAd(
            @NonNull MediationInterstitialAdConfiguration configuration,
            @NonNull MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> adMobLoadListener
    ) {
        String responseId = getResponseIdAndCheckParameters(
                configuration,
                EXTRA_RESPONSE_ID,
                adMobLoadListener::onFailure
        );
        if (responseId == null) {
            return;
        }

        BidResponse bidResponse = BidResponseCache.getInstance().popBidResponse(responseId);
        if (bidResponse == null) {
            adMobLoadListener.onFailure(AdErrors.noResponse(responseId));
            return;
        }

        AdUnitConfiguration config = new AdUnitConfiguration();
        config.setAdFormat(bidResponse.isVideo() ? AdFormat.VAST : AdFormat.INTERSTITIAL);
        InterstitialControllerListener listener = getListener(adMobLoadListener);
        interstitialController = PluginRendererFactory.createInterstitialController(
                configuration.getContext(), listener, config, bidResponse
        );
        if (interstitialController == null) {
            adMobLoadListener.onFailure(AdErrors.interstitialControllerError("Renderer returned null controller"));
            return;
        }
        interstitialController.loadAd(config, bidResponse);
    }


    private InterstitialControllerListener getListener(
            MediationAdLoadCallback<MediationInterstitialAd, MediationInterstitialAdCallback> loadListener
    ) {
        return new InterstitialControllerListener() {
            @Override
            public void onInterstitialReadyForDisplay() {
                adMobInterstitialListener = loadListener.onSuccess(context -> {
                    if (interstitialController != null) {
                        interstitialController.show();
                    }
                });
            }

            @Override
            public void onInterstitialDisplayed() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.reportAdImpression();
                    adMobInterstitialListener.onAdOpened();
                }
            }

            @Override
            public void onInterstitialClicked() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.reportAdClicked();
                }
            }

            @Override
            public void onInterstitialClosed() {
                if (adMobInterstitialListener != null) {
                    adMobInterstitialListener.onAdClosed();
                }
            }

            @Override
            public void onInterstitialFailedToLoad(AdException exception) {
                loadListener.onFailure(AdErrors.failedToLoadAd(exception.getMessage()));
            }
        };
    }

}
