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

import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;

import java.lang.ref.WeakReference;

public class MraidEventHandlerNotifierRunnable implements Runnable {
    private static final String TAG = MraidEventHandlerNotifierRunnable.class.getSimpleName();

    private final WeakReference<HTMLCreative> mWeakHtmlCreative;
    private final WeakReference<WebViewBase> mWeakWebViewBase;
    private final WeakReference<JsExecutor> mWeakJsExecutor;

    private MraidEvent mMraidEvent;

    public MraidEventHandlerNotifierRunnable(HTMLCreative htmlCreative,
                                             WebViewBase webViewBase,
                                             MraidEvent mraidEvent,
                                             JsExecutor jsExecutor) {
        mWeakHtmlCreative = new WeakReference<>(htmlCreative);
        mWeakWebViewBase = new WeakReference<>(webViewBase);
        mWeakJsExecutor = new WeakReference<>(jsExecutor);
        mMraidEvent = mraidEvent;
    }

    @Override
    public void run() {
        HTMLCreative htmlCreative = mWeakHtmlCreative.get();
        WebViewBase webViewBase = mWeakWebViewBase.get();
        if (htmlCreative == null || webViewBase == null) {
            LogUtil.debug(TAG, "Unable to pass event to handler. HtmlCreative or webviewBase is null");
            return;
        }
        htmlCreative.handleMRAIDEventsInCreative(mMraidEvent, webViewBase);

        final JsExecutor jsExecutor = mWeakJsExecutor.get();
        if (jsExecutor == null) {
            LogUtil.debug(TAG, "Unable to executeNativeCallComplete(). JsExecutor is null.");
            return;
        }

        jsExecutor.executeNativeCallComplete();
    }
}
