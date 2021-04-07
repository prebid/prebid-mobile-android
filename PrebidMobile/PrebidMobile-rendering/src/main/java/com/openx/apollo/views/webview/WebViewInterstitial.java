package com.openx.apollo.views.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.openx.apollo.utils.helpers.HandlerQueueManager;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.webview.PreloadManager.PreloadedListener;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.InterstitialJSInterface;
import com.openx.apollo.views.webview.mraid.JsExecutor;

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
