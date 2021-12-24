package com.mopub.mediation;

import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import java.lang.reflect.Method;
import java.util.*;

public abstract class MoPubBaseMediationUtils implements PrebidMediationDelegate {

    private static final String TAG = "MoPubBaseMediationUtils";

    protected static final HashSet<String> RESERVED_KEYS = new HashSet<>();
    protected static final int MOPUB_QUERY_STRING_LIMIT = 4000;
    protected static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";

    protected final Object adObject;

    public MoPubBaseMediationUtils(Object adObject) {
        this.adObject = adObject;
    }

    @Override
    public void setResponseToLocalExtras(@Nullable BidResponse response) {
        if (adObject == null) {
            Log.e(TAG, "Ad object is null");
            return;
        }

        if (response != null) {
            Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response.getId());
            callMethodOnObjectWithParameter(adObject, "setLocalExtras", Map.class, localExtras);
        }
    }

    @Override
    public void handleKeywordsUpdate(@Nullable HashMap<String, String> keywords) {
        removeUsedKeywordsForMoPub(adObject);

        if (keywords != null && !keywords.isEmpty()) {
            StringBuilder keywordsBuilder = new StringBuilder();
            for (String key : keywords.keySet()) {
                addReservedKeys(key);
                keywordsBuilder.append(key).append(":").append(keywords.get(key)).append(",");
            }
            String pbmKeywords = keywordsBuilder.toString();
            String adViewKeywords = (String) callMethodOnObject(adObject, "getKeywords");
            if (!TextUtils.isEmpty(adViewKeywords)) {
                adViewKeywords = pbmKeywords + adViewKeywords;
            } else {
                adViewKeywords = pbmKeywords;
            }
            if (adViewKeywords.length() <= MOPUB_QUERY_STRING_LIMIT) {
                callMethodOnObject(adObject, "setKeywords", adViewKeywords);
            }
        }
    }

    @Override
    public boolean canPerformRefresh() {
        return true;
    }

    protected static void removeUsedKeywordsForMoPub(Object adViewObj) {
        String adViewKeywords = (String) callMethodOnObject(adViewObj, "getKeywords");
        if (!TextUtils.isEmpty(adViewKeywords) && RESERVED_KEYS != null && !RESERVED_KEYS.isEmpty()) {
            // Copy used keywords to a temporary list to avoid concurrent modification
            // while iterating through the list
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
            callMethodOnObject(adViewObj, "setKeywords", adViewKeywords);
        }
    }

    public static Object callMethodOnObject(Object object, String methodName, Object... params) {
        try {
            int len = params.length;
            Class<?>[] classes = new Class[len];
            for (int i = 0; i < len; i++) {
                classes[i] = params[i].getClass();
            }
            Method method = object.getClass().getMethod(methodName, classes);
            return method.invoke(object, params);
        } catch (Exception e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        return null;
    }

    protected static Object callMethodOnObjectWithParameter(Object object, String methodName, Class paramType, Object param) {
        try {
            Method method = object.getClass().getMethod(methodName, paramType);
            return method.invoke(object, param);
        } catch (Exception e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        return null;
    }

    protected static void addReservedKeys(String key) {
        synchronized (RESERVED_KEYS) {
            RESERVED_KEYS.add(key);
        }
    }

}
