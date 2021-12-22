package com.mopub.mediation;

import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MoPubNativeMediationUtils extends MoPubBaseMediationUtils {

    public MoPubNativeMediationUtils(HashMap<String, String> adObject) {
        super(adObject);
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response);
        callMethodOnObjectWithParameter(adObject, "setLocalExtras", Map.class, localExtras);
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {
        removeUsedKeywordsForMoPub(adObject);

        if (keywords != null && !keywords.isEmpty()) {
            if (adObject != null && adObject.getClass() == HashMap.class) {
                ((HashMap) adObject).clear();
                ((HashMap) adObject).putAll(keywords);
            }
        }
    }

}
