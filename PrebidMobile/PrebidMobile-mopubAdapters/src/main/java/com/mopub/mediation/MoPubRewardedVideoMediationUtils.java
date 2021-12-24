package com.mopub.mediation;

import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.HashMap;

public class MoPubRewardedVideoMediationUtils extends MoPubBaseMediationUtils {

    private HashMap<String, String> keywords;

    public MoPubRewardedVideoMediationUtils(HashMap<String, String> keywords) {
        super(keywords);
        this.keywords = keywords;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {

    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> prebidKeywords) {
        removeUsedKeywordsForMoPub(keywords);

        if (prebidKeywords != null && !prebidKeywords.isEmpty()) {
            if (keywords != null) {
                keywords.clear();
                keywords.putAll(prebidKeywords);
            }
        }
    }

}
