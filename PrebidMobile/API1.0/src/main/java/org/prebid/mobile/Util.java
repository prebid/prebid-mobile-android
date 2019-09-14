/*
 *    Copyright 2018-2019 Prebid.org, Inc.
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

package org.prebid.mobile;

import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.addendum.AdViewUtils;
import org.prebid.mobile.addendum.PbFindSizeError;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Random;
import java.util.Set;

public class Util {

    static final String MOPUB_BANNER_VIEW_CLASS = "com.mopub.mobileads.MoPubView";
    static final String MOPUB_INTERSTITIAL_CLASS = "com.mopub.mobileads.MoPubInterstitial";
    static final String DFP_AD_REQUEST_CLASS = "com.google.android.gms.ads.doubleclick.PublisherAdRequest";
    private static final Random RANDOM = new Random();
    private static final HashSet<String> reservedKeys;
    private static final int MoPubQueryStringLimit = 4000;

    static {
        reservedKeys = new HashSet<>();
    }

    private Util() {

    }

    /**
     *@deprecated Please migrate to - AdViewUtils.findPrebidCreativeSize(View, AdViewUtils.PbFindSizeListener)
     *@see AdViewUtils#findPrebidCreativeSize(View, AdViewUtils.PbFindSizeListener)
     */
    @Deprecated
    public static void findPrebidCreativeSize(@Nullable View adView, final CreativeSizeCompletionHandler completionHandler) {
        AdViewUtils.findPrebidCreativeSize(adView, new AdViewUtils.PbFindSizeListener() {
            @Override
            public void success(int width, int height) {
                completionHandler.onSize(new CreativeSize(width, height));

            }

            @Override
            public void failure(@NonNull PbFindSizeError error) {
                LogUtil.w("Missing failure handler, please migrate to - Util.findPrebidCreativeSize(View, CreativeSizeResultHandler)");
                completionHandler.onSize(null); // backwards compatibility
            }
        });

    }

    @Nullable
    static JSONObject getObjectWithoutEmptyValues(@NonNull JSONObject jsonObject) {

        JSONObject result = null;
        try {
            JSONObject clone = new JSONObject(jsonObject.toString());
            removeEntryWithoutValue(clone);

            if (clone.length() > 0) {
                result = clone;
            }

        } catch (JSONException e) {
            LogUtil.e("message:" + e.getMessage());
        }

        return result;
    }

    private static void removeEntryWithoutValue(@NonNull JSONObject map) throws JSONException {
        Iterator<String> iterator = map.keys();

        while (iterator.hasNext()) {
            String key = iterator.next();

            Object value = map.opt(key);
            if (value != null) {

                if (value instanceof JSONObject) {

                    JSONObject mapValue = (JSONObject)value;
                    removeEntryWithoutValue(mapValue);

                    if (mapValue.length() == 0) {
                        iterator.remove();
                    }
                } else if (value instanceof JSONArray) {

                    JSONArray arrayValue = (JSONArray)value;
                    arrayValue = removeEntryWithoutValue(arrayValue);

                    map.put(key, arrayValue);

                    if (arrayValue.length() == 0) {
                        iterator.remove();
                    }
                } else if (value instanceof String) {
                    String stringValue = (String) value;

                    if (stringValue.length() == 0) {
                        iterator.remove();
                    }
                }
            }
        }
    }

    @CheckResult
    private static JSONArray removeEntryWithoutValue(@NonNull JSONArray array) throws JSONException {

        for (int i = 0; i < array.length(); i++) {

            Object value = array.opt(i);
            if (value != null) {

                if (value instanceof JSONObject) {

                    JSONObject mapValue = (JSONObject)value;
                    removeEntryWithoutValue(mapValue);

                    if (mapValue.length() == 0) {
                        array = getJsonArrayWithoutEntryByIndex(array, i);
                    }
                } else if (value instanceof JSONArray) {
                    JSONArray arrayValue = (JSONArray)value;
                    arrayValue = removeEntryWithoutValue(arrayValue);

                    array.put(i, arrayValue);

                    if (arrayValue.length() == 0) {
                        array = getJsonArrayWithoutEntryByIndex(array, i);
                    }
                } else if (value instanceof String) {
                    String stringValue = (String) value;

                    if (stringValue.length() == 0) {
                        array = getJsonArrayWithoutEntryByIndex(array, i);
                    }
                }
            }

        }

        return array;
    }

    @CheckResult
    private static JSONArray getJsonArrayWithoutEntryByIndex(JSONArray jsonArray, int pos) throws JSONException {
        JSONArray result = new JSONArray();

        for (int i = 0; i < jsonArray.length(); i++) {
            if (i != pos) {
                result.put(jsonArray.get(i));
            }
        }

        return result;
    }

    static Class getClassFromString(String className) {
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
        }
        return null;
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
        } catch (NullPointerException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Creates a random lowercase string whose length is the number
     * of characters specified.
     * <p>
     * Characters will be chosen from the set of Latin alphabetic
     * characters (a-z).
     *
     * @param count the length of random string to create
     * @return the random string
     */
    static String randomLowercaseAlphabetic(int count) {
        return randomLowercaseAlphabetic(count, RANDOM);
    }

    // Code inspiration from apache commons RandomStringUtils
    static String randomLowercaseAlphabetic(int count, Random random) {
        if (count == 0) {
            return "";
        } else if (count < 0) {
            throw new IllegalArgumentException("Invalid count value: " + count + " is less than 0.");
        }

        int start = 'a';
        int end = 'z' + 1;

        StringBuilder sb = new StringBuilder(count);
        int gap = end - start;

        while (count-- != 0) {
            int codePoint = random.nextInt(gap) + start;
            sb.appendCodePoint(codePoint);
        }
        return sb.toString();
    }

    /**
     * Escapes the string using EcmaScript String rules, dealing correctly with
     * quotes and control-chars (tab, backslash, cr, ff, etc.).
     * <p>
     * <p>Example:</p>
     * <pre>
     * input string: He didn't say, "Stop!"
     * output string: He didn\'t say, \"Stop!\"
     * </pre>
     * <p>
     * NOTE: Code inspiration from apache commons StringEscapeUtils and android
     * JSONStringer.
     *
     * @param str String to escape values in, may be null
     * @return String with escaped values, {@code null} if null string input
     */
    static String escapeEcmaScript(String str) {
        if (str == null) return null;

        StringBuilder sb = new StringBuilder(str.length() + 50); // optimistic initial size

        int pos = 0;
        int len = str.length();
        while (pos < len) {
            char c = str.charAt(pos);

            switch (c) {
                case '\'':
                case '"':
                case '\\':
                case '/':
                    sb.append('\\').append(c);
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                case '\b':
                    sb.append("\\b");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                case '\f':
                    sb.append("\\f");
                    break;

                default:
                    int cp = Character.codePointAt(str, pos);
                    if (cp < 32 || cp > 0x7f) {
                        if (cp > 0xffff) {
                            char[] surrogatePair = Character.toChars(cp);
                            sb.append("\\u");
                            sb.append(Integer.toHexString(surrogatePair[0]));
                            sb.append("\\u");
                            sb.append(Integer.toHexString(surrogatePair[1]));
                        } else {
                            sb.append(String.format("\\u%04x", cp));
                        }
                        pos += Character.charCount(cp) - 1;
                    } else {
                        sb.append(c);
                    }
                    break;
            }

            pos++;
        }

        return sb.toString();
    }

    static boolean supportedAdObject(Object adObj) {
        if (adObj == null) return false;
        if (adObj.getClass() == getClassFromString(MOPUB_BANNER_VIEW_CLASS)
                || adObj.getClass() == getClassFromString(MOPUB_INTERSTITIAL_CLASS)
                || adObj.getClass() == getClassFromString(DFP_AD_REQUEST_CLASS))
            return true;
        return false;
    }

    static void apply(HashMap<String, String> bids, Object adObj) {
        if (adObj == null) return;
        if (adObj.getClass() == getClassFromString(MOPUB_BANNER_VIEW_CLASS)
                || adObj.getClass() == getClassFromString(MOPUB_INTERSTITIAL_CLASS)) {
            handleMoPubKeywordsUpdate(bids, adObj);
        } else if (adObj.getClass() == getClassFromString(DFP_AD_REQUEST_CLASS)) {
            handleDFPCustomTargetingUpdate(bids, adObj);
        }
    }

    private static void handleMoPubKeywordsUpdate(HashMap<String, String> bids, Object adObj) {
        removeUsedKeywordsForMoPub(adObj);
        if (bids != null && !bids.isEmpty()) {
            StringBuilder keywordsBuilder = new StringBuilder();
            for (String key : bids.keySet()) {
                addReservedKeys(key);
                keywordsBuilder.append(key).append(":").append(bids.get(key)).append(",");
            }
            String pbmKeywords = keywordsBuilder.toString();
            String adViewKeywords = (String) Util.callMethodOnObject(adObj, "getKeywords");
            if (!TextUtils.isEmpty(adViewKeywords)) {
                adViewKeywords = pbmKeywords + adViewKeywords;
            } else {
                adViewKeywords = pbmKeywords;
            }
            // only set keywords if less than mopub query string limit
            if (adViewKeywords.length() <= MoPubQueryStringLimit) {
                Util.callMethodOnObject(adObj, "setKeywords", adViewKeywords);
            }
        }
    }

    private static void handleDFPCustomTargetingUpdate(HashMap<String, String> bids, Object adObj) {
        removeUsedCustomTargetingForDFP(adObj);
        if (bids != null && !bids.isEmpty()) {
            Bundle bundle = (Bundle) Util.callMethodOnObject(adObj, "getCustomTargeting");
            if (bundle != null) {
                // retrieve keywords from mopub adview
                for (String key : bids.keySet()) {
                    bundle.putString(key, bids.get(key));
                    addReservedKeys(key);
                }
            }
        }
    }

    private static void addReservedKeys(String key) {
        synchronized (reservedKeys) {
            reservedKeys.add(key);
        }
    }

    private static void removeUsedKeywordsForMoPub(Object adViewObj) {
        String adViewKeywords = (String) Util.callMethodOnObject(adViewObj, "getKeywords");
        if (!TextUtils.isEmpty(adViewKeywords) && reservedKeys != null && !reservedKeys.isEmpty()) {
            // Copy used keywords to a temporary list to avoid concurrent modification
            // while iterating through the list
            String[] adViewKeywordsArray = adViewKeywords.split(",");
            ArrayList<String> adViewKeywordsArrayList = new ArrayList<>(Arrays.asList(adViewKeywordsArray));
            LinkedList<String> toRemove = new LinkedList<>();
            for (String keyword : adViewKeywordsArray) {
                if (!TextUtils.isEmpty(keyword) && keyword.contains(":")) {
                    String[] keywordArray = keyword.split(":");
                    if (keywordArray.length > 0) {
                        if (reservedKeys.contains(keywordArray[0])) {
                            toRemove.add(keyword);
                        }
                    }
                }
            }
            adViewKeywordsArrayList.removeAll(toRemove);
            adViewKeywords = TextUtils.join(",", adViewKeywordsArrayList);
            Util.callMethodOnObject(adViewObj, "setKeywords", adViewKeywords);
        }
    }

    private static void removeUsedCustomTargetingForDFP(Object adRequestObj) {
        Bundle bundle = (Bundle) Util.callMethodOnObject(adRequestObj, "getCustomTargeting");
        if (bundle != null && reservedKeys != null) {
            for (String key : reservedKeys) {
                bundle.remove(key);
            }
        }
    }

    static <E, U> void addValue(Map<E, Set<U>> map, E key, U value) {
        Set<U> valueSet = map.get(key);

        if (valueSet == null) {
            valueSet = new HashSet<>();
            map.put(key, valueSet);
        }

        valueSet.add(value);
    }

    @NonNull
    static <E, U> JSONObject toJson(@Nullable Map<E, ? extends Collection<U>> map) throws JSONException {

        JSONObject jsonObject = new JSONObject();
        if (map == null) {
            return jsonObject;
        }

        for (Map.Entry<E, ? extends Collection<U>> entry : map.entrySet()) {

            jsonObject.put(entry.getKey().toString(), new JSONArray(entry.getValue()));
        }

        return jsonObject;
    }

    public interface CreativeSizeCompletionHandler {
        void onSize(@Nullable CreativeSize size);
    }

    /**
     * Utility Size class
     */
    public static class CreativeSize {
        private int width;
        private int height;

        /**
         * Creates an ad size object with width and height as specified
         *
         * @param width  width of the ad container
         * @param height height of the ad container
         */
        public CreativeSize(int width, int height) {
            this.width = width;
            this.height = height;
        }

        /**
         * Returns the width of the ad container
         *
         * @return width
         */
        public int getWidth() {
            return width;
        }

        /**
         * Returns the height of the ad container
         *
         * @return height
         */
        public int getHeight() {
            return height;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            CreativeSize adSize = (CreativeSize) o;

            if (width != adSize.width) return false;
            return height == adSize.height;
        }

        @Override
        public int hashCode() {
            String size = width + "x" + height;
            return size.hashCode();
        }
    }
}
