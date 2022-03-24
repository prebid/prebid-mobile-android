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

package org.prebid.mobile.drprebid.validation;

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.ValueCallback;
import android.webkit.WebView;
import org.prebid.mobile.LogUtil;

import java.util.ArrayList;
import java.util.List;

interface OnWebViewListener {
    void success(String html);
    void failure();
}

final class AdViewUtils {

    private static final String INNER_HTML_SCRIPT = "document.body.innerHTML";

    private AdViewUtils() { }

    public static void findHtml(View adView, final OnWebViewListener handler) {

        if (adView == null) {
            handler.failure();
            return;
        }

        List<WebView> webViewList = new ArrayList<>(2);
        recursivelyFindWebViewList(adView, webViewList);
        if (webViewList.size() == 0) {
            handler.failure();
            return;
        }

        findSizeInWebViewListAsync(webViewList, handler);
    }

    static void recursivelyFindWebViewList(View view, List<WebView> webViewList) {
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

    static void findSizeInWebViewListAsync(final List<WebView> webViewList, final OnWebViewListener handler) {

        int currentAndroidApi = Build.VERSION.SDK_INT;
        int necessaryAndroidApi = Build.VERSION_CODES.KITKAT;

        if (currentAndroidApi >= necessaryAndroidApi) {
            LogUtil.debug("webViewList size:" + webViewList.size());

            int lastIndex = webViewList.size() - 1;
            iterateWebViewListAsync(webViewList, lastIndex, handler);

        } else {
            handler.failure();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    static void iterateWebViewListAsync(final List<WebView> webViewList, final int index, final OnWebViewListener handler) {

        final WebView webView = webViewList.get(index);

        webView.evaluateJavascript(INNER_HTML_SCRIPT, new ValueCallback<String>() {

            private void processNextWebViewOrFail() {

                int nextIndex = index - 1;

                if (nextIndex >= 0) {
                    iterateWebViewListAsync(webViewList, nextIndex, handler);
                } else {
                    handler.failure();
                }
            }

            @Override
            public void onReceiveValue(String html) {

                if (html != null) {
                    handler.success(html);
                } else {
                    processNextWebViewOrFail();
                }
            }
        });
    }
}

