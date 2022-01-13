package org.prebid.mobile.rendering.bidding.config;

import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

import java.util.HashMap;

public class MockMediationUtils implements PrebidMediationDelegate {

    @Override
    public boolean isBannerView(@Nullable @org.jetbrains.annotations.Nullable Object adView) {
        return false;
    }

    @Override
    public boolean isInterstitialView(@Nullable @org.jetbrains.annotations.Nullable Object adView) {
        return false;
    }

    @Override
    public boolean isNativeView(@Nullable @org.jetbrains.annotations.Nullable Object adView) {
        return false;
    }

    @Override
    public void handleKeywordsUpdate(@Nullable @org.jetbrains.annotations.Nullable Object adView, @Nullable @org.jetbrains.annotations.Nullable HashMap<String, String> keywords) {

    }

    @Override
    public void setResponseToLocalExtras(@Nullable @org.jetbrains.annotations.Nullable Object adView, @Nullable @org.jetbrains.annotations.Nullable BidResponse response) {

    }

    @Override
    public void setResponseIdToLocalExtras(@Nullable @org.jetbrains.annotations.Nullable Object adView, @Nullable @org.jetbrains.annotations.Nullable BidResponse response) {

    }
}