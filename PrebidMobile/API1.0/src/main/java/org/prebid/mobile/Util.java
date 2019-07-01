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

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.Size;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public static void findPrebidCreativeSize(View adView, CreativeSizeCompletionHandler completionHandler) {

        List<WebView> webViewList = new ArrayList<>(2);
        recursivelyFindWebView(adView, webViewList);
        if (webViewList.size() == 0) {
            LogUtil.w("adView doesn't include WebView");
            return;
        }

        findSizeInWebViewListAsync(webViewList, completionHandler);
    }

    @Nullable
    static void recursivelyFindWebView(View view, List<WebView> webViewList) {
        if (view instanceof ViewGroup) {
            //ViewGroup
            ViewGroup viewGroup = (ViewGroup) view;

            if (!(viewGroup instanceof WebView)) {
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    recursivelyFindWebView(viewGroup.getChildAt(i), webViewList);
                }
            } else {
                //WebView
                final WebView webView = (WebView) viewGroup;
                webViewList.add(webView);
            }

        }
    }

    static void findSizeInWebViewListAsync(@Size(min = 1) final List<WebView> webViewList, final CreativeSizeCompletionHandler completionHandler) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LogUtil.d("webViewList size:" + webViewList.size());

            iterateWebViewListAsync(webViewList, webViewList.size() - 1, new WebViewPrebidCallback() {
                @Override
                public void success(final WebView webView, @NonNull CreativeSize adSize) {

                    completionHandler.onSize(adSize);
                    webView.post(new Runnable() {
                        @Override
                        public void run() {
                            webView.getSettings().setLoadWithOverviewMode(true);
                        }
                    });

                }

                @Override
                public void failure() {
                    completionHandler.onSize(null);
                }
            });


        } else {
            LogUtil.w("AndroidSDK < KITKAT");
            completionHandler.onSize(null);
        }

    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static void iterateWebViewListAsync(@Size(min = 1) final List<WebView> webViewList, final int index, final WebViewPrebidCallback webViewPrebidCallback) {

        final WebView webView = webViewList.get(index);

        webView.evaluateJavascript("document.body.innerHTML", new ValueCallback<String>() {

            private void repeatOrFail() {
                int nextIndex = index - 1;

                if (nextIndex >= 0) {
                    iterateWebViewListAsync(webViewList, nextIndex, webViewPrebidCallback);
                } else {
                    webViewPrebidCallback.failure();
                }
            }

            @Override
            public void onReceiveValue(@Nullable String html) {

                if (html == null) {
                    LogUtil.w("webView jsCode is null");

                    repeatOrFail();
                } else {

                    @Nullable
                    CreativeSize adSize = findSizeInJavaScript(html);

                    if (adSize == null) {
                        LogUtil.w("adSize is null");
                        repeatOrFail();
                    } else {
                        webViewPrebidCallback.success(webView, adSize);
                    }

                }

            }
        });
    }

    @Nullable
    static CreativeSize findSizeInJavaScript(@Nullable String jsCode) {

        if (TextUtils.isEmpty(jsCode)) {
            LogUtil.w("jsCode is empty");
            return null;
        }

        String hbSizeKeyValue = findHbSizeKeyValue(jsCode);
        if (hbSizeKeyValue == null) {
            LogUtil.w("HbSizeKeyValue is null");
            return null;
        }

        String hbSizeValue = findHbSizeValue(hbSizeKeyValue);
        if (hbSizeValue == null) {
            LogUtil.w("HbSizeValue is null");
            return null;
        }

        return stringToSize(hbSizeValue);
    }

    @Nullable
    static String findHbSizeKeyValue(String text) {
        return matchAndCheck("hb_size\\W+[0-9]+x[0-9]+", text);
    }

    @Nullable
    static String findHbSizeValue(String text) {
        return matchAndCheck("[0-9]+x[0-9]+", text);
    }

    @NonNull
    static String[] matches(String regex, String text) {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> allMatches = new ArrayList<>();
        while (matcher.find()) {
            allMatches.add(matcher.group());
        }

        return allMatches.toArray(new String[0]);
    }

    @Nullable
    static String matchAndCheck(String regex, String text) {

        String[] matched = matches(regex, text);

        if (matched.length == 0) {
            return null;
        }

        String firstResult = matched[0];
        return firstResult;
    }

    @Nullable
    static CreativeSize stringToSize(String size) {
        String[] sizeArr = size.split("x");

        if (sizeArr.length != 2) {
            LogUtil.w(size + "has a wrong format");
            return null;
        }

        String widthString = sizeArr[0];
        String heightString = sizeArr[1];

        int width;
        int height;
        try {
            width = Integer.parseInt(widthString);
        } catch (NumberFormatException e) {
            LogUtil.w(size + "can not be converted to Size");
            return null;
        }

        try {
            height = Integer.parseInt(heightString);
        } catch (NumberFormatException e) {
            LogUtil.w(size + "can not be converted to Size");
            return null;
        }

        return new CreativeSize(width, height);
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

    public interface CreativeSizeCompletionHandler {
        void onSize(@Nullable CreativeSize size);
    }

    private interface WebViewPrebidCallback {
        void success(WebView webView, CreativeSize adSize);

        void failure();
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
