package org.prebid.mobile.rendering.views.webview;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.webview.mraid.BannerJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;

public class WebViewBanner extends WebViewBase {

    private static final String TAG = WebViewBanner.class.getSimpleName();
    private MraidEvent mMraidEvent;

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
        return mMraidEvent;
    }

    public void setMraidEvent(MraidEvent event) {
        mMraidEvent = event;
    }

    public void setMRAIDInterface() {
        BaseJSInterface mraid = new BannerJSInterface(getContext(), this, new JsExecutor(this,
                                                                                         new Handler(Looper.getMainLooper()),
                                                                                         new HandlerQueueManager()));

        addJavascriptInterface(mraid, "jsBridge");
        OXLog.debug(TAG, "JS bridge initialized");
        setBaseJSInterface(mraid);
    }
}
