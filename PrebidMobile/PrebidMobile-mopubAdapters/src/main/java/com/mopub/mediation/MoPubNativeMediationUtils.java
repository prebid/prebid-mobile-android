package com.mopub.mediation;

import android.util.Log;
import androidx.annotation.Nullable;
import com.mopub.nativeads.MoPubNative;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;

import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MoPubNativeMediationUtils extends MoPubBaseMediationUtils {

    private static final String TAG = "MoPubNativeMediation";

    private final WeakReference<MoPubNative> nativeView;
    private final HashMap<String, String> keywords;

    public MoPubNativeMediationUtils(HashMap<String, String> keywords, MoPubNative nativeView) {
        super(nativeView);
        this.keywords = keywords;
        this.nativeView = new WeakReference<>(nativeView);
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (adObject == null) {
            Log.e(TAG, "Ad object is null");
            return;
        }

        Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response);
        callMethodOnObjectWithParameter(adObject, "setLocalExtras", Map.class, localExtras);
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
