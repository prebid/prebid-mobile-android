package com.mopub.mediation;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;

import java.util.*;

public abstract class MoPubBaseMediationUtils implements PrebidMediationDelegate {

    private static final String TAG = "MoPubBaseMediationUtils";

    protected static final HashSet<String> RESERVED_KEYS = new HashSet<>();
    protected static final int MOPUB_QUERY_STRING_LIMIT = 4000;
    protected static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";

    final protected void setResponseToLocalExtras(@Nullable BidResponse response, @NonNull LocalExtrasManager setter) {
        if (response == null) {
            Log.e(TAG, "Response is null! Can't set response to local extras.");
            return;
        }

        Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response.getId());
        setter.setLocalExtras(localExtras);
    }

    final protected void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords, KeywordsManager keywordsManager) {
        removeUsedKeywordsForMoPub(keywordsManager);

        if (keywords != null && !keywords.isEmpty()) {
            StringBuilder keywordsBuilder = new StringBuilder();
            for (String key : keywords.keySet()) {
                addReservedKeys(key);
                keywordsBuilder.append(key).append(":").append(keywords.get(key)).append(",");
            }
            String pbmKeywords = keywordsBuilder.toString();
            String adViewKeywords = keywordsManager.getKeywords();
            if (!TextUtils.isEmpty(adViewKeywords)) {
                adViewKeywords = pbmKeywords + adViewKeywords;
            } else {
                adViewKeywords = pbmKeywords;
            }
            if (adViewKeywords.length() <= MOPUB_QUERY_STRING_LIMIT) {
                keywordsManager.setKeywords(adViewKeywords);
            }
        }
    }

    @Override
    public boolean canPerformRefresh() {
        return false;
    }

    /**
     * Method removes used keywords. Copies used keywords to a temporary list
     * to avoid concurrent modification while iterating through the list.
     */
    protected static void removeUsedKeywordsForMoPub(KeywordsManager keywordsManager) {
        String adViewKeywords = keywordsManager.getKeywords();

        if (!TextUtils.isEmpty(adViewKeywords) && !RESERVED_KEYS.isEmpty()) {
            String[] adViewKeywordsArray = adViewKeywords.split(",");
            ArrayList<String> adViewKeywordsArrayList = new ArrayList<>(Arrays.asList(adViewKeywordsArray));
            LinkedList<String> toRemove = new LinkedList<>();
            for (String keyword : adViewKeywordsArray) {
                if (!TextUtils.isEmpty(keyword) && keyword.contains(":")) {
                    String[] keywordArray = keyword.split(":");
                    if (keywordArray.length > 0) {
                        if (RESERVED_KEYS.contains(keywordArray[0])) {
                            toRemove.add(keyword);
                        }
                    }
                }
            }
            adViewKeywordsArrayList.removeAll(toRemove);
            adViewKeywords = TextUtils.join(",", adViewKeywordsArrayList);
            keywordsManager.setKeywords(adViewKeywords);
        }
    }

    protected static void addReservedKeys(String key) {
        synchronized (RESERVED_KEYS) {
            RESERVED_KEYS.add(key);
        }
    }

    protected boolean isAdViewNull(Object adView) {
        if (adView == null) {
            LogUtil.e(TAG, "AdView is null, it can be destroyed as WeakReference");
            return true;
        }
        return false;
    }

    interface KeywordsManager {

        void setKeywords(String adViewKeywords);

        String getKeywords();

    }

    interface LocalExtrasManager {

        void setLocalExtras(Map<String, Object> localExtras);

    }

}
