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

package org.prebid.mobile.rendering.mraid.methods;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;

import java.lang.ref.WeakReference;

public class MraidEventHandlerNotifierRunnable implements Runnable {

    private static final String TAG = MraidEventHandlerNotifierRunnable.class.getSimpleName();

    private final WeakReference<HTMLCreative> weakHtmlCreative;
    private final WeakReference<WebViewBase> weakWebViewBase;
    private final WeakReference<JsExecutor> weakJsExecutor;

    private MraidEvent mraidEvent;

    public MraidEventHandlerNotifierRunnable(
            HTMLCreative htmlCreative,
            WebViewBase webViewBase,
            MraidEvent mraidEvent,
            JsExecutor jsExecutor
    ) {
        weakHtmlCreative = new WeakReference<>(htmlCreative);
        weakWebViewBase = new WeakReference<>(webViewBase);
        weakJsExecutor = new WeakReference<>(jsExecutor);
        this.mraidEvent = mraidEvent;
    }

    @Override
    public void run() {
        HTMLCreative htmlCreative = weakHtmlCreative.get();
        WebViewBase webViewBase = weakWebViewBase.get();
        if (htmlCreative == null || webViewBase == null) {
            LogUtil.debug(TAG, "Unable to pass event to handler. HtmlCreative or webviewBase is null");
            return;
        }
        htmlCreative.handleMRAIDEventsInCreative(mraidEvent, webViewBase);

        final JsExecutor jsExecutor = weakJsExecutor.get();
        if (jsExecutor == null) {
            LogUtil.debug(TAG, "Unable to executeNativeCallComplete(). JsExecutor is null.");
            return;
        }

        jsExecutor.executeNativeCallComplete();
    }
}
