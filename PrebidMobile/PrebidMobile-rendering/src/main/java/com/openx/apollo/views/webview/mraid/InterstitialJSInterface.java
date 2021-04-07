package com.openx.apollo.views.webview.mraid;

import android.content.Context;
import android.webkit.JavascriptInterface;

import com.openx.apollo.views.webview.WebViewBase;

public class InterstitialJSInterface extends BaseJSInterface {
    private static final String TAG = InterstitialJSInterface.class.getSimpleName();

    /**
     * Instantiate the interface and set the context
     */
    public InterstitialJSInterface(Context context, WebViewBase adBaseView, JsExecutor jsExecutor) {
        super(context, adBaseView, jsExecutor);
    }

    @Override
    @JavascriptInterface
    public String getPlacementType() {

        return "interstitial";
    }

    @Override
    @JavascriptInterface
    public void expand() {
    }
}
