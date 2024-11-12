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

package org.prebid.mobile.addendum;

import android.annotation.TargetApi;
import android.os.Build;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.Size;

import org.prebid.mobile.CacheManager;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidNativeAd;
import org.prebid.mobile.PrebidNativeAdListener;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class AdViewUtils {

    /**
     * Escaped double quotes.
     */
    private static final String EDQ = "\\\\\"";

    private static final String INNER_HTML_SCRIPT = "document.body.innerHTML";
    private static final String SIZE_VALUE_REGEX_EXPRESSION = "[0-9]+x[0-9]+";
    private static final String SIZE_OBJECT_REGEX_EXPRESSION = "hb_size\\W+" + SIZE_VALUE_REGEX_EXPRESSION; //"hb_size\\W+[0-9]+x[0-9]+"

    private static final String CACHE_ID_REGEX = EDQ + "hb_cache_id" + EDQ + ":\\[" + EDQ + "(.*?)" + EDQ + "\\]";


    private static final String GAM_VIEW_CLASS = "com.google.android.gms.ads.doubleclick.PublisherAdView";
    private static final String GAM_VIEW_CLASS_2 = "com.google.android.gms.ads.admanager.AdManagerAdView";
    private static final String GAM_CUSTOM_TEMPLATE_AD_CLASS = "com.google.android.gms.ads.formats.NativeCustomTemplateAd";
    private static final String GAM_CUSTOM_TEMPLATE_AD_CLASS_2 = "com.google.android.gms.ads.nativead.NativeCustomFormatAd";

    private AdViewUtils() {
    }

    public static void findPrebidCreativeSize(@Nullable View adView, final PbFindSizeListener handler) {
        if (adView == null) {
            warnAndTriggerFailure(PbFindSizeErrorFactory.NO_WEB_VIEW, handler);
            return;
        }

        List<WebView> webViewList = new ArrayList<>(2);
        recursivelyFindWebViewList(adView, webViewList);
        if (webViewList.size() == 0) {
            warnAndTriggerFailure(PbFindSizeErrorFactory.NO_WEB_VIEW, handler);
            return;
        }

        findSizeInWebViewListAsync(webViewList, handler);
    }

    static void triggerSuccess(WebView webView, Pair<Integer, Integer> adSize, PbFindSizeListener handler) {
        final int width = adSize.first;
        final int height = adSize.second;

        handler.success(width, height);

        fixZoomIn(webView, width, height);

    }

    //a fix of strange bug on Android with image scaling up
    //case: should be called after PublisherAdView.setAdSizes()
    static void fixZoomIn(final WebView webView, final int expectedWidth, final int expectedHeight) {

        final int minViewHeight = 10;

        //500 millis to find a webViewContentHeight
        //usually it takes 200 millis
        final int contentHeightDelayMillis = 100;
        int queueLimit = 5;

        final LimitedQueueContainer<Integer> contentHeightQueue = new LimitedQueueContainer<>(queueLimit);
        final Set<Integer> contentHeightSet = new HashSet<>(queueLimit);

        webView.post(new Runnable() {
            @Override
            public void run() {

                int webViewHeight = webView.getHeight();

                //case: check if a publisher have called PublisherAdView.setAdSizes()
                //if publisher does not call PublisherAdView.setAdSizes() it is less then 10(e.g 3 instead of 750)
                if (webViewHeight > minViewHeight) {
                    int webViewContentHeight = webView.getContentHeight();

                    //case: wait when webView.getContentHeight() >= expected height from HTML
                    //webView does not contain getContentWidth()
                    if (webViewContentHeight < expectedHeight) {
                        LogUtil.debug("fixZoomIn" + " webViewContentHeight:" + webViewContentHeight);
                        contentHeightQueue.add(webViewContentHeight);
                        if (contentHeightQueue.isFull()) {

                            contentHeightSet.clear();
                            contentHeightSet.addAll(contentHeightQueue.getList());

                            if (contentHeightSet.size() == 1) {

                                //case: if it is not possible to get an expected height se scale as is
                                setWebViewScale(webView, webViewHeight, webViewContentHeight);
                                return;
                            }
                        }
                        webView.postDelayed(this, contentHeightDelayMillis);
                    } else {
                        setWebViewScale(webView, webViewHeight, webViewContentHeight);
                    }
                }

            }
        });
    }

    static void setWebViewScale(WebView webView, float webViewHeight, int webViewContentHeight) {
        //case: regulate scale because WebView.getSettings().setLoadWithOverviewMode() does not work
        int scale = (int) (webViewHeight / webViewContentHeight * 100 + 1);

        LogUtil.debug("fixZoomIn WB Height:" + webViewHeight + " getContentHeight:" + webViewContentHeight + " scale:" + scale);
        webView.setInitialScale(scale);
    }

    static void warnAndTriggerFailure(Set<Pair<WebView, PbFindSizeError>> webViewErrorSet, PbFindSizeListener handler) {

        warnAndTriggerFailure(PbFindSizeErrorFactory.getCompositeFailureError(webViewErrorSet), handler);
    }

    static void warnAndTriggerFailure(PbFindSizeError error, PbFindSizeListener handler) {

        String description = error.getDescription();
        LogUtil.warning(description);

        handler.failure(error);
    }

    @Nullable
    static void recursivelyFindWebViewList(@Nullable View view, List<WebView> webViewList) {
        if (view instanceof ViewGroup) {
            //ViewGroup
            ViewGroup viewGroup = (ViewGroup) view;

            if (viewGroup instanceof WebView) {
                //WebView
                final WebView webView = (WebView) viewGroup;
                webViewList.add(webView);
            } else {
                for (int i = 0; i < viewGroup.getChildCount(); i++) {
                    recursivelyFindWebViewList(viewGroup.getChildAt(i), webViewList);
                }
            }
        }
    }

    static void findSizeInWebViewListAsync(@Size(min = 1) final List<WebView> webViewList, final PbFindSizeListener handler) {

        int currentAndroidApi = Build.VERSION.SDK_INT;
        int necessaryAndroidApi = Build.VERSION_CODES.KITKAT;

        if (currentAndroidApi >= necessaryAndroidApi) {
            LogUtil.debug("webViewList size:" + webViewList.size());

            int lastIndex = webViewList.size() - 1;
            iterateWebViewListAsync(webViewList, lastIndex, handler);
        } else {
            warnAndTriggerFailure(PbFindSizeErrorFactory.getUnsupportedAndroidIpiError(currentAndroidApi, necessaryAndroidApi), handler);
        }
    }

    /**
     * {@link PbFindSizeListener} will be called only once.
     * {@link PbFindSizeListener#success(int, int)} when size is found
     * and {@link PbFindSizeListener#failure(PbFindSizeError)} when size is not found inside passed WebView list
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    static void iterateWebViewListAsync(@Size(min = 1) final List<WebView> webViewList, final int index, final PbFindSizeListener handler) {

        final WebView webView = webViewList.get(index);

        webView.evaluateJavascript(INNER_HTML_SCRIPT, new ValueCallback<String>() {

            private Set<Pair<WebView, PbFindSizeError>> errorSet = new LinkedHashSet<>();

            private void processNextWebViewOrFail(PbFindSizeError error) {

                errorSet.add(new Pair<>(webView, error));

                int nextIndex = index - 1;

                if (nextIndex >= 0) {
                    iterateWebViewListAsync(webViewList, nextIndex, handler);
                } else {
                    warnAndTriggerFailure(errorSet, handler);
                }
            }

            @Override
            public void onReceiveValue(@Nullable String html) {

                Pair<Pair<Integer, Integer>, PbFindSizeError> pair = findSizeInHtml(html);

                @Nullable
                Pair<Integer, Integer> size = pair.first;
                @Nullable
                PbFindSizeError error = pair.second;

                if (size != null) {
                    triggerSuccess(webView, size, handler);
                } else {
                    processNextWebViewOrFail(error);
                }
            }
        });
    }

    /**
     * {@link PrebidNativeAdListener} will be called only once.
     * {@link PrebidNativeAdListener#onPrebidNativeLoaded(PrebidNativeAd)} when Cached Ad is found and is valid
     * {@link PrebidNativeAdListener#onPrebidNativeNotValid()} (PrebidNativeAd)} when Cached Ad is found and but is invalid
     * and {@link PrebidNativeAdListener#onPrebidNativeNotFound()} ()} (PrebidNativeAd)} when Cached Ad is not found
     */
    static void iterateWebViewListAsync(final List<WebView> webViewList, final int index, final PrebidNativeAdListener listener) {

        final WebView webView = webViewList.get(index);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            webView.evaluateJavascript(INNER_HTML_SCRIPT, new ValueCallback<String>() {

                private void processNextWebView() {

                    int nextIndex = index - 1;

                    if (nextIndex >= 0) {
                        iterateWebViewListAsync(webViewList, nextIndex, listener);
                    } else {
                        listener.onPrebidNativeNotFound();
                    }
                }

                @Override
                public void onReceiveValue(@Nullable String html) {
                    Pattern prebidPattern = Pattern.compile("\\%\\%Prebid\\%\\%.*\\%\\%Prebid\\%\\%");
                    Matcher m = prebidPattern.matcher(html);
                    if (m.find()) {
                        String s = m.group();
                        String[] results = s.split("%%");
                        String cacheId = results[2];
                        if (CacheManager.isValid(cacheId)) {
                            PrebidNativeAd ad = createPrebidNativeAd(cacheId, listener);
                            if (ad != null) {
                                listener.onPrebidNativeLoaded(ad);
                                return;
                            }
                        }
                        listener.onPrebidNativeNotValid();
                    } else {
                        processNextWebView();
                    }
                }
            });
        } else {
            if (listener != null) {
                listener.onPrebidNativeNotFound();
            }
        }
    }

    private static PrebidNativeAd createPrebidNativeAd(String cacheId, PrebidNativeAdListener listener) {
        Class clazz = null;
        try {
            clazz = Class.forName("org.prebid.mobile.PrebidNativeAd");
            Method method = clazz.getDeclaredMethod("create", String.class);
            method.setAccessible(true);
            return (PrebidNativeAd) method.invoke(null, cacheId);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    @NonNull
    static Pair<Pair<Integer, Integer>, PbFindSizeError> findSizeInHtml(@Nullable String html) {

        if (TextUtils.isEmpty(html)) {
            return new Pair<>(null, PbFindSizeErrorFactory.NO_HTML);
        }

        String hbSizeObject = findHbSizeObject(html);
        if (hbSizeObject == null) {
            return new Pair<>(null, PbFindSizeErrorFactory.NO_SIZE_OBJECT);
        }

        String hbSizeValue = findHbSizeValue(hbSizeObject);
        if (hbSizeValue == null) {
            return new Pair<>(null, PbFindSizeErrorFactory.NO_SIZE_VALUE);
        }

        Pair<Integer, Integer> size = stringToSize(hbSizeValue);
        if (size == null) {
            return new Pair<>(null, PbFindSizeErrorFactory.SIZE_UNPARSED);
        } else {
            return new Pair<>(size, null);
        }

    }

    @Nullable
    static String findHbSizeObject(String text) {
        return matchAndCheck(SIZE_OBJECT_REGEX_EXPRESSION, text);
    }

    @Nullable
    static String findHbSizeValue(String text) {
        return matchAndCheck(SIZE_VALUE_REGEX_EXPRESSION, text);
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

    public static void findCacheId(WebView webView, CacheIdResult onResult) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
            onResult.run(null);
            return;
        }

        webView.evaluateJavascript(INNER_HTML_SCRIPT, value -> {
            String result = findCacheId(value);
            onResult.run(result);
        });
    }

    @Nullable
    static String findCacheId(String html) {
        Pattern pattern = Pattern.compile(CACHE_ID_REGEX);
        Matcher matcher = pattern.matcher(html);

        if (!matcher.find() || matcher.groupCount() == 0) {
            return null;
        }
        return matcher.group(1);
    }

    @Nullable
    static Pair<Integer, Integer> stringToSize(String size) {
        String[] sizeArr = size.split("x");

        if (sizeArr.length != 2) {
            LogUtil.warning(size + " has a wrong format");
            return null;
        }

        String widthString = sizeArr[0];
        String heightString = sizeArr[1];

        int width;
        int height;
        try {
            width = Integer.parseInt(widthString);
        } catch (NumberFormatException e) {
            LogUtil.warning(size + "can not be converted to Size");
            return null;
        }

        try {
            height = Integer.parseInt(heightString);
        } catch (NumberFormatException e) {
            LogUtil.warning(size + "can not be converted to Size");
            return null;
        }

        return new Pair<>(width, height);
    }

    public interface PbFindSizeListener {
        void success(int width, int height);

        void failure(@NonNull PbFindSizeError error);
    }

    /**
     * This API can be used to find if the passed object contains info to retreive valid cached Native response or not,
     * and notifies using the {@link PrebidNativeAdListener}
     *
     * @param object   instances of Google Native Ads
     * @param listener to notify the validity of passed object via @onPrebidNativeLoaded, #onPrebidNativeNotFound, #onPrebidNativeNotValid
     */
    public static void findNative(@NonNull Object object, @NonNull PrebidNativeAdListener listener) {
        String objectClassName = object.getClass().getCanonicalName();
        if (GAM_VIEW_CLASS.equals(objectClassName) || GAM_VIEW_CLASS_2.equals(objectClassName)) {
            View adView = (View) object;
            findNativeInGAMPublisherAdView(adView, listener);
        } else if (implementsInterface(object, GAM_CUSTOM_TEMPLATE_AD_CLASS) || implementsInterface(object, GAM_CUSTOM_TEMPLATE_AD_CLASS_2)) {
            findNativeInGAMCustomTemplateAd(object, listener);
        } else {
            listener.onPrebidNativeNotFound();
        }
    }

    private static boolean implementsInterface(@NonNull Object object, @NonNull String interfaceName) {
        for (Class c : object.getClass().getInterfaces()) {
            LogUtil.debug("Prebid", c.getCanonicalName());
            if (c.getCanonicalName().equals(interfaceName)) {
                return true;
            }
        }
        return false;
    }

    private static void findNativeInGAMCustomTemplateAd(@NonNull Object object, @NonNull PrebidNativeAdListener listener) {
        String isPrebid = (String) callMethodOnObject(object, "getText", "isPrebid");
        if ("1".equals(isPrebid)) {
            String cacheId = (String) callMethodOnObject(object, "getText", "hb_cache_id_local");
            PrebidNativeAd ad = createPrebidNativeAd(cacheId, listener);
            if (ad != null) {
                listener.onPrebidNativeLoaded(ad);
            } else {
                listener.onPrebidNativeNotValid();
            }
        } else {
            listener.onPrebidNativeNotFound();
        }
    }

    private static void findNativeInGAMPublisherAdView(@NonNull View adView, @NonNull PrebidNativeAdListener listener) {
        List<WebView> webViewList = new ArrayList<>();
        AdViewUtils.recursivelyFindWebViewList(adView, webViewList);
        if (webViewList.size() == 0) {
            listener.onPrebidNativeNotFound();
        } else {
            iterateWebViewListAsync(webViewList, webViewList.size() - 1, listener);
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

    public interface CacheIdResult {
        void run(String cacheId);
    }

}

//It is not possible to use Enum because we should have a possibility to pass additional information
final class PbFindSizeErrorFactory {

    private PbFindSizeErrorFactory() {
    }

    //common errors
    static final int UNSPECIFIED_CODE = 201;
    static final int UNSUPPORTED_ANDROID_IPI_CODE = 202;
    static final int COMPOSITE_FAILURE_CODE = 203;

    //Platform's errors
    static final int NO_WEBVIEW_CODE = 210;
    static final int WEBVIEW_FAILED_CODE = 220;
    static final int NO_HTML_CODE = 230;
    static final int NO_SIZE_OBJECT_CODE = 240;
    static final int NO_SIZE_VALUE_CODE = 250;
    static final int SIZE_UNPARSED_CODE = 260;

    //factory's objects
    static final PbFindSizeError UNSPECIFIED = getUnspecifiedError();

    static final PbFindSizeError NO_WEB_VIEW = getNoWebViewError();
    static final PbFindSizeError NO_HTML = getNoHtmlError();
    static final PbFindSizeError NO_SIZE_OBJECT = getNoSizeObjectError();
    static final PbFindSizeError NO_SIZE_VALUE = getNoSizeValueError();
    static final PbFindSizeError SIZE_UNPARSED = getSizeUnparsedError();

    private static PbFindSizeError getUnspecifiedError() {
        return getError(UNSPECIFIED_CODE, "Unspecified error");
    }

    static PbFindSizeError getUnsupportedAndroidIpiError(int currentAndroidApi, int necessaryAndroidApi) {
        return getError(UNSUPPORTED_ANDROID_IPI_CODE, "AndroidAPI:" + currentAndroidApi + " doesn't support the functionality. Minimum AndroidAPI is:" + necessaryAndroidApi);
    }

    static PbFindSizeError getCompositeFailureError(Set<Pair<WebView, PbFindSizeError>> errorSet) {
        StringBuilder result = new StringBuilder();
        result.append("There is a set of errors:\n");

        for (Pair<WebView, PbFindSizeError> webViewFindSizeError : errorSet) {

            result.append("    WebView:").append(webViewFindSizeError.first)
                    .append(" errorCode:").append(webViewFindSizeError.second.getCode())
                    .append(" errorDescription:").append(webViewFindSizeError.second.getDescription())
                    .append("\n");
        }

        return getError(COMPOSITE_FAILURE_CODE, result.toString());
    }

    //private zone
    private static PbFindSizeError getNoWebViewError() {
        return getError(NO_WEBVIEW_CODE, "The view doesn't include WebView");
    }

    private static PbFindSizeError getWebViewFailedError() {
        return getError(WEBVIEW_FAILED_CODE, "The view doesn't include WebView");
    }

    private static PbFindSizeError getNoHtmlError() {
        return getError(NO_HTML_CODE, "The WebView doesn't have HTML");
    }

    private static PbFindSizeError getNoSizeObjectError() {
        return getError(NO_SIZE_OBJECT_CODE, "The HTML doesn't contain a size object");
    }

    private static PbFindSizeError getNoSizeValueError() {
        return getError(NO_SIZE_VALUE_CODE, "The size object doesn't contain a value");
    }

    private static PbFindSizeError getSizeUnparsedError() {
        return getError(SIZE_UNPARSED_CODE, "The size value has a wrong format");
    }

    private static PbFindSizeError getError(final int code, final String description) {

        return new PbFindSizeError(code, description);
    }
}
