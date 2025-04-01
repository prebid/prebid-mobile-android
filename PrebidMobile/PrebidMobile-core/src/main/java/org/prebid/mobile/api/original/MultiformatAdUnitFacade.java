package org.prebid.mobile.api.original;

import android.annotation.SuppressLint;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.AdSize;
import org.prebid.mobile.AdUnit;
import org.prebid.mobile.BannerParameters;
import org.prebid.mobile.NativeParameters;
import org.prebid.mobile.OnCompleteListener;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.Util;
import org.prebid.mobile.VideoParameters;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.configuration.NativeAdUnitConfiguration;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.listeners.BidRequesterListener;
import org.prebid.mobile.rendering.models.AdPosition;
import org.prebid.mobile.rendering.models.PlacementType;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Internal AdUnit implementation that is used for PrebidAdUnit
 * with multi-format configuration. It separates logic for multi-format ad unit.
 */
class MultiformatAdUnitFacade extends AdUnit {

    @Nullable
    private BidResponse bidResponse;

    public MultiformatAdUnitFacade(@NotNull String configId, @NonNull PrebidRequest request) {
        super(configId);
        allowNullableAdObject = true;
        setConfigurationBasedOnRequest(request);
    }

    /**
     * Applies the interstitial native visibility tracker for tracking `burl` url.
     */
    public void activateInterstitialPrebidImpressionTracker() {
        this.activateInterstitialPrebidImpressionTracker = true;
    }

    @Override
    protected BidRequesterListener createBidListener(OnCompleteListener originalListener) {
        return new BidRequesterListener() {
            @Override
            public void onFetchCompleted(BidResponse response) {
                bidResponse = response;

                HashMap<String, String> keywords = response.getTargeting();
                Util.apply(keywords, adObject);

                originalListener.onComplete(ResultCode.SUCCESS);
            }

            @Override
            public void onError(AdException exception) {
                bidResponse = null;

                Util.apply(null, adObject);

                originalListener.onComplete(convertToResultCode(exception));
            }
        };
    }

    private void setConfigurationBasedOnRequest(
            @NonNull PrebidRequest request
    ) {
        if (request.isInterstitial()) {
            configuration.setAdPosition(AdPosition.FULLSCREEN);
        }

        BannerParameters bannerParameters = request.getBannerParameters();
        if (bannerParameters != null) {
            if (request.isInterstitial()) {
                configuration.addAdFormat(AdFormat.INTERSTITIAL);

                Integer minWidth = bannerParameters.getInterstitialMinWidthPercentage();
                Integer minHeight = bannerParameters.getInterstitialMinHeightPercentage();
                if (minWidth != null && minHeight != null) {
                    configuration.setMinSizePercentage(new AdSize(minWidth, minHeight));
                }
            } else {
                configuration.addAdFormat(AdFormat.BANNER);
            }

            configuration.setBannerParameters(bannerParameters);
            configuration.addSizes(bannerParameters.getAdSizes());
        }

        VideoParameters videoParameters = request.getVideoParameters();
        if (videoParameters != null) {
            configuration.addAdFormat(AdFormat.VAST);

            if (request.isInterstitial()) {
                configuration.setPlacementType(PlacementType.INTERSTITIAL);
            }
            if (request.isRewarded()) {
                configuration.setRewarded(true);
            }

            configuration.setVideoParameters(videoParameters);
            configuration.addSize(videoParameters.getAdSize());
        }

        NativeParameters nativeParameters = request.getNativeParameters();
        if (nativeParameters != null) {
            configuration.addAdFormat(AdFormat.NATIVE);

            NativeAdUnitConfiguration nativeConfig = nativeParameters.getNativeConfiguration();
            configuration.setNativeConfiguration(nativeConfig);
        }

        String gpid = request.getGpid();
        configuration.setGpid(gpid);
    }

    @Nullable
    public BidResponse getBidResponse() {
        return bidResponse;
    }

    @SuppressLint("VisibleForTests")
    @Override
    public AdUnitConfiguration getConfiguration() {
        return super.getConfiguration();
    }

}