/*
 *    Copyright 2018-2021 Prebid.org, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.mopub.mediation;

import android.text.TextUtils;
import androidx.annotation.Nullable;
import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.bidding.display.PrebidMediationDelegate;
import org.prebid.mobile.rendering.utils.logger.LogUtil;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

public class MoPubMediationUtils implements PrebidMediationDelegate {
    private static final String TAG = MoPubMediationUtils.class.getSimpleName();

    private static final HashSet<String> RESERVED_KEYS;
    private static final int MOPUB_QUERY_STRING_LIMIT = 4000;
    private static final String MOPUB_BANNER_VIEW_CLASS = "com.mopub.mobileads.MoPubView";
    private static final String MOPUB_INTERSTITIAL_VIEW_CLASS = "com.mopub.mobileads.MoPubInterstitial";
    private static final String MOPUB_NATIVE_CLASS = "com.mopub.nativeads.MoPubNative";

    static final String KEY_BID_RESPONSE = "PREBID_BID_RESPONSE_ID";

    static {
        RESERVED_KEYS = new HashSet<>();
    }

    static Class getClassForString(String className) {
        try {
            return Class.forName(className);
        }
        catch (ClassNotFoundException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        return null;
    }


    @Override
    public boolean isBannerView(@Nullable Object adView) {
        return adView != null && adView.getClass() == getClassForString(MOPUB_BANNER_VIEW_CLASS);
    }

    @Override
    public boolean isInterstitialView(@Nullable Object adView) {
        return adView != null && adView.getClass() == getClassForString(MOPUB_INTERSTITIAL_VIEW_CLASS);
    }

    @Override
    public boolean isNativeView(@Nullable Object adView) {
        return adView != null && adView.getClass() == getClassForString(MOPUB_NATIVE_CLASS);
    }

    @Override
    public void handleKeywordsUpdate(@Nullable Object adView, HashMap<String, String> keywords) {
        removeUsedKeywordsForMoPub(adView);

        if (keywords != null && !keywords.isEmpty()) {
            if (adView != null && adView.getClass() == HashMap.class) {
                ((HashMap) adView).clear();
                ((HashMap) adView).putAll(keywords);
            }
            else {
                StringBuilder keywordsBuilder = new StringBuilder();
                for (String key : keywords.keySet()) {
                    addReservedKeys(key);
                    keywordsBuilder.append(key).append(":").append(keywords.get(key)).append(",");
                }
                String pbmKeywords = keywordsBuilder.toString();
                String adViewKeywords = (String) callMethodOnObject(adView, "getKeywords");
                if (!TextUtils.isEmpty(adViewKeywords)) {
                    adViewKeywords = pbmKeywords + adViewKeywords;
                }
                else {
                    adViewKeywords = pbmKeywords;
                }
                // only set keywords if less than mopub query string limit
                if (adViewKeywords.length() <= MOPUB_QUERY_STRING_LIMIT) {
                    callMethodOnObject(adView, "setKeywords", adViewKeywords);
                }
            }
        }
    }

    @Override
    public void setResponseToLocalExtras(@Nullable Object adView, @Nullable BidResponse response) {
        if (isNativeView(adView)) {
            Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response);
            callMethodOnObjectWithParameter(adView, "setLocalExtras", Map.class, localExtras);
        }
    }

    @Override
    public void setResponseIdToLocalExtras(@Nullable Object adView, @Nullable BidResponse response) {
        if (isBannerView(adView) || isInterstitialView(adView)) {
            if (response != null) {
                Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response.getId());
                callMethodOnObjectWithParameter(adView, "setLocalExtras", Map.class, localExtras);
            }
        }
    }

    private static void removeUsedKeywordsForMoPub(Object adViewObj) {
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
        }
        catch (NullPointerException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        catch (NoSuchMethodException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        catch (InvocationTargetException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        catch (IllegalAccessException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        return null;
    }

    private static Object callMethodOnObjectWithParameter(Object object, String methodName, Class paramType, Object param) {
        try {
            Method method = object.getClass().getMethod(methodName, paramType);
            return method.invoke(object, param);
        }
        catch (NullPointerException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        catch (NoSuchMethodException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        catch (InvocationTargetException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        catch (IllegalAccessException e) {
            LogUtil.debug(TAG, e.getMessage());
        }
        return null;
    }

    private static void addReservedKeys(String key) {
        synchronized (RESERVED_KEYS) {
            RESERVED_KEYS.add(key);
        }
    }
}
