package org.prebid.mobile.rendering.views.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
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
        mMRAIDBridgeName = name;
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
        OXLog.debug(TAG, "JS bridge initialized");
        setBaseJSInterface(mraid);
    }
}
