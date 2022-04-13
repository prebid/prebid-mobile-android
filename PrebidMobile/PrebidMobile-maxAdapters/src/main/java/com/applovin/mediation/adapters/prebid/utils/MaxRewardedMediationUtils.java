package com.applovin.mediation.adapters.prebid.utils;

import androidx.annotation.Nullable;
import com.applovin.mediation.adapters.PrebidMaxMediationAdapter;
import com.applovin.mediation.ads.MaxRewardedAd;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

import java.util.HashMap;

public class MaxRewardedMediationUtils implements PrebidMediationDelegate {

    private final MaxRewardedAd rewardedAd;

    public MaxRewardedMediationUtils(MaxRewardedAd rewardedAd) {
        this.rewardedAd = rewardedAd;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (response != null && rewardedAd != null) {
            rewardedAd.setLocalExtraParameter(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, response.getId());
        }
    }

    @Override
    public boolean canPerformRefresh() {
        return false;
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {}

}
