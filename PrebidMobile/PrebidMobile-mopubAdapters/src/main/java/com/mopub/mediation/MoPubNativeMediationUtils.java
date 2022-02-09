package com.mopub.mediation;

import androidx.annotation.Nullable;
import com.mopub.nativeads.MoPubNative;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MoPubNativeMediationUtils {

    private final MoPubNative adView;
    private final HashMap<String, String> keywords;

    public MoPubNativeMediationUtils(HashMap<String, String> keywords, MoPubNative nativeView) {
        this.keywords = keywords;
        this.adView = nativeView;
    }

    public void saveCacheId(@Nullable String cacheId) {
        Map<String, Object> localExtras = Collections.singletonMap(
                MoPubBaseMediationUtils.KEY_BID_RESPONSE, cacheId
        );
        adView.setLocalExtras(localExtras);
    }

    public void handleKeywordsUpdate(@Nullable HashMap<String, String> prebidKeywords) {
        if (prebidKeywords != null && !prebidKeywords.isEmpty()) {
            if (keywords != null) {
                keywords.clear();
                keywords.putAll(prebidKeywords);
            }
        }
    }

}
