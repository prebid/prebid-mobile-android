package com.mopub.mediation;

import androidx.annotation.Nullable;
import com.mopub.nativeads.MoPubNative;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MoPubNativeMediationUtils extends MoPubBaseMediationUtils {

    private static final String TAG = "MoPubNativeMediation";

    private final MoPubNative adView;
    private final HashMap<String, String> keywords;

    public MoPubNativeMediationUtils(HashMap<String, String> keywords, MoPubNative nativeView) {
        this.keywords = keywords;
        this.adView = nativeView;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response);
        adView.setLocalExtras(localExtras);
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
