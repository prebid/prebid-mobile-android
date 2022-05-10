package com.applovin.mediation.adapters.prebid.utils;

import androidx.annotation.Nullable;
import com.applovin.mediation.adapters.PrebidMaxMediationAdapter;
import com.applovin.mediation.ads.MaxRewardedAd;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

import java.util.HashMap;

public class MaxMediationRewardedUtils implements PrebidMediationDelegate {

    private final MaxRewardedAd rewardedAd;

    public MaxMediationRewardedUtils(MaxRewardedAd rewardedAd) {
        this.rewardedAd = rewardedAd;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (rewardedAd != null) {
            String responseId; if (response != null) {
                responseId = response.getId();
            } else {
                responseId = null;
            } rewardedAd.setLocalExtraParameter(PrebidMaxMediationAdapter.EXTRA_RESPONSE_ID, responseId);
        }
    }

    @Override
    public boolean canPerformRefresh() {
        return false;
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {}

}
