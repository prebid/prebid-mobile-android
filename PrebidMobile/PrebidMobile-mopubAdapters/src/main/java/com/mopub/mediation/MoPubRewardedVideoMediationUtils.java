package com.mopub.mediation;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.HashMap;

public class MoPubRewardedVideoMediationUtils extends MoPubBaseMediationUtils {

    @Nullable
    public static String cacheId = null;
    private HashMap<String, String> keywords;

    public MoPubRewardedVideoMediationUtils(@NonNull HashMap<String, String> keywords) {
        this.keywords = keywords;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (response != null) {
            if (response.getId() != null) {
                cacheId = response.getId();
                return;
            }
        }
        cacheId = null;
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> prebidKeywords) {
        if (prebidKeywords != null && !prebidKeywords.isEmpty()) {
            if (keywords != null) {
                keywords.clear();
                keywords.putAll(prebidKeywords);
            }
        }
    }

}
