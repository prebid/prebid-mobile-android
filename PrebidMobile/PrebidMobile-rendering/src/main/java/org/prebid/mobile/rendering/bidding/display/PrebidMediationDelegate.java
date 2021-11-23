package org.prebid.mobile.rendering.bidding.display;

import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.HashMap;

public interface PrebidMediationDelegate {

    public boolean isBannerView(@Nullable Object adView);

    public boolean isInterstitialView(@Nullable Object adView);

    public boolean isNativeView(@Nullable Object adView);

    public void handleKeywordsUpdate(@Nullable Object adView, @Nullable HashMap<String, String> keywords);

    public void setResponseToLocalExtras(@Nullable Object adView, @Nullable BidResponse response);

    public void setResponseIdToLocalExtras(@Nullable Object adView, @Nullable BidResponse response);

}
