package com.openx.apollo.views.webview.mraid;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.openx.apollo.views.webview.WebViewBase;

public class BannerJSInterface extends BaseJSInterface {
    /**
     * Instantiate the interface and set the context
     */
    public BannerJSInterface(Context context, WebViewBase adBaseView, JsExecutor jsExecutor) {
        super(context, adBaseView, jsExecutor);
    }

    @Override
    @JavascriptInterface
    public String getPlacementType() {
        return "inline";
    }
}
