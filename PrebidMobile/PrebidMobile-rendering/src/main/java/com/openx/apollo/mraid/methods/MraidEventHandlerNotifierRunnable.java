package com.openx.apollo.mraid.methods;

import com.openx.apollo.models.HTMLCreative;
import com.openx.apollo.models.internal.MraidEvent;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.JsExecutor;

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
            OXLog.debug(TAG, "Unable to pass event to handler. HtmlCreative or webviewBase is null");
            return;
        }
        htmlCreative.handleMRAIDEventsInCreative(mMraidEvent, webViewBase);

        final JsExecutor jsExecutor = mWeakJsExecutor.get();
        if (jsExecutor == null) {
            OXLog.debug(TAG, "Unable to executeNativeCallComplete(). JsExecutor is null.");
            return;
        }

        jsExecutor.executeNativeCallComplete();
    }
}
