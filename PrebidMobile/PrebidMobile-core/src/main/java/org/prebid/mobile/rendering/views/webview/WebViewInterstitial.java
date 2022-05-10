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

package org.prebid.mobile.rendering.views.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.prebid.mobile.rendering.views.webview.PreloadManager.PreloadedListener;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.InterstitialJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;

public class WebViewInterstitial extends WebViewBase {

    private static final String TAG = WebViewInterstitial.class.getSimpleName();

    public WebViewInterstitial(Context context, String html, int width, int height, PreloadedListener preloadedListener, MraidEventsManager.MraidListener mraidListener) {
        super(context, html, width, height, preloadedListener, mraidListener);
    }

    public void setJSName(String name) {
        MRAIDBridgeName = name;
    }

    @Override
    public void init() {
        initWebView();
        setMRAIDInterface();
    }

    public void setMRAIDInterface() {
        BaseJSInterface mraid = new InterstitialJSInterface(getContext(), this, new JsExecutor(this,
                                                                                               new Handler(Looper.getMainLooper()),
                                                                                               new HandlerQueueManager()));

        addJavascriptInterface(mraid, "jsBridge");
        LogUtil.debug(TAG, "JS bridge initialized");
        setBaseJSInterface(mraid);
    }
}
