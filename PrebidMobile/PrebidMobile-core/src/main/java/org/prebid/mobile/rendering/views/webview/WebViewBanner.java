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
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.prebid.mobile.rendering.views.webview.mraid.BannerJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;

public class WebViewBanner extends WebViewBase {

    private static final String TAG = WebViewBanner.class.getSimpleName();
    private MraidEvent mraidEvent;

    public WebViewBanner(Context context, String html, int width, int height, PreloadManager.PreloadedListener preloadedListener, MraidEventsManager.MraidListener mraidListener) {
        super(context, html, width, height, preloadedListener, mraidListener);
    }

    //2nd webview for 2-part expand
    public WebViewBanner(Context context, PreloadManager.PreloadedListener preloadedListener, MraidEventsManager.MraidListener mraidListener) {
        super(context, preloadedListener, mraidListener);

        init();
    }

    @Override
    public void init() {
        //imp for the mraid to work(2 part expand mainly)
        initWebView();
        setMRAIDInterface();
    }

    public MraidEvent getMraidEvent() {
        return mraidEvent;
    }

    public void setMraidEvent(MraidEvent event) {
        mraidEvent = event;
    }

    public void setMRAIDInterface() {
        BaseJSInterface mraid = new BannerJSInterface(getContext(), this, new JsExecutor(this,
                                                                                         new Handler(Looper.getMainLooper()),
                                                                                         new HandlerQueueManager()));

        addJavascriptInterface(mraid, "jsBridge");
        LogUtil.debug(TAG, "JS bridge initialized");
        setBaseJSInterface(mraid);
    }
}
