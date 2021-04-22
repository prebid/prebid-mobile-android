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

package org.prebid.mobile.rendering.bidding.display;

import android.text.TextUtils;

import org.prebid.mobile.rendering.bidding.data.bid.BidResponse;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;

import androidx.annotation.Nullable;

public class ReflectionUtils {
    private static final String TAG = ReflectionUtils.class.getSimpleName();

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
            OXLog.debug(TAG, e.getMessage());
        }
        return null;
    }

    static void handleMoPubKeywordsUpdate(Object adObj, HashMap<String, String> keywords) {
        removeUsedKeywordsForMoPub(adObj);

        if (keywords != null && !keywords.isEmpty()) {
            if (adObj.getClass() == HashMap.class) {
                ((HashMap) adObj).clear();
                ((HashMap) adObj).putAll(keywords);
            }
            else {
                StringBuilder keywordsBuilder = new StringBuilder();
                for (String key : keywords.keySet()) {
                    addReservedKeys(key);
                    keywordsBuilder.append(key).append(":").append(keywords.get(key)).append(",");
                }
                String pbmKeywords = keywordsBuilder.toString();
                String adViewKeywords = (String) callMethodOnObject(adObj, "getKeywords");
                if (!TextUtils.isEmpty(adViewKeywords)) {
                    adViewKeywords = pbmKeywords + adViewKeywords;
                }
                else {
                    adViewKeywords = pbmKeywords;
                }
                // only set keywords if less than mopub query string limit
                if (adViewKeywords.length() <= MOPUB_QUERY_STRING_LIMIT) {
                    callMethodOnObject(adObj, "setKeywords", adViewKeywords);
                }
            }
        }
    }

    static void setResponseIdToMoPubLocalExtras(Object adViewObj, BidResponse response) {
        if (isMoPubBannerView(adViewObj) || isMoPubInterstitialView(adViewObj)) {
            Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response.getId());
            callMethodOnObjectWithParameter(adViewObj, "setLocalExtras", Map.class, localExtras);
        }
    }

    static void setResponseToMoPubLocalExtras(Object adViewObj, BidResponse response) {
        if (isMoPubNative(adViewObj)) {
            Map<String, Object> localExtras = Collections.singletonMap(KEY_BID_RESPONSE, response);
            callMethodOnObjectWithParameter(adViewObj, "setLocalExtras", Map.class, localExtras);
        }
    }

    static boolean isMoPubBannerView(
        @Nullable
            Object adViewObj) {
        return adViewObj != null
               && adViewObj.getClass() == getClassForString(MOPUB_BANNER_VIEW_CLASS);
    }

    static boolean isMoPubInterstitialView(
        @Nullable
            Object adViewObj) {
        return adViewObj != null
               && adViewObj.getClass() == getClassForString(MOPUB_INTERSTITIAL_VIEW_CLASS);
    }

    static boolean isMoPubNative(
        @Nullable
            Object adViewObj) {
        return adViewObj != null
               && adViewObj.getClass() == getClassForString(MOPUB_NATIVE_CLASS);
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

    static Object callMethodOnObject(Object object, String methodName, Object... params) {
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
            OXLog.debug(TAG, e.getMessage());
        }
        catch (NoSuchMethodException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        catch (InvocationTargetException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        catch (IllegalAccessException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        return null;
    }

    private static Object callMethodOnObjectWithParameter(Object object, String methodName, Class paramType, Object param) {
        try {
            Method method = object.getClass().getMethod(methodName, paramType);
            return method.invoke(object, param);
        }
        catch (NullPointerException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        catch (NoSuchMethodException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        catch (InvocationTargetException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        catch (IllegalAccessException e) {
            OXLog.debug(TAG, e.getMessage());
        }
        return null;
    }

    private static void addReservedKeys(String key) {
        synchronized (RESERVED_KEYS) {
            RESERVED_KEYS.add(key);
        }
    }
}
