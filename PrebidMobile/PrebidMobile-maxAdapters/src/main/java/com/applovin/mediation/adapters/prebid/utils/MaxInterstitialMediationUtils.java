package com.applovin.mediation.adapters.prebid.utils;

import androidx.annotation.Nullable;
import com.applovin.mediation.adapters.PrebidMAXMediationAdapter;
import com.applovin.mediation.ads.MaxInterstitialAd;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

import java.util.HashMap;

public class MaxInterstitialMediationUtils implements PrebidMediationDelegate {

    private final MaxInterstitialAd interstitialAd;

    public MaxInterstitialMediationUtils(MaxInterstitialAd interstitialAd) {
        this.interstitialAd = interstitialAd;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (response != null && interstitialAd != null) {
            interstitialAd.setLocalExtraParameter(PrebidMAXMediationAdapter.EXTRA_RESPONSE_ID, response.getId());
        }
    }

    @Override
    public boolean canPerformRefresh() {
        return false;
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {}

}
