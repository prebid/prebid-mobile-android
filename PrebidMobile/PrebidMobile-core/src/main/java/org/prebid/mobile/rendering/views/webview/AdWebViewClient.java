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

import android.graphics.Bitmap;
import android.graphics.Color;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.prebid.mobile.LogUtil;

import java.util.HashSet;

import static android.webkit.WebView.HitTestResult.SRC_ANCHOR_TYPE;
import static android.webkit.WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE;

public class AdWebViewClient extends WebViewClient {

    private static final String TAG = AdWebViewClient.class.getSimpleName();

    private static final String JS__GET_RENDERED_HTML = "javascript:window.HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');";

    protected AdAssetsLoadedListener adAssetsLoadedListener;

    private boolean loadingFinished = true;

    private HashSet<String> urls = new HashSet<>();
    private String pageUrl;

    public interface AdAssetsLoadedListener {

        void startLoadingAssets();

        void adAssetsLoaded();

        void notifyMraidScriptInjected();

    }

    public AdWebViewClient(AdAssetsLoadedListener adAssetsLoadedListener) {
        this.adAssetsLoadedListener = adAssetsLoadedListener;
    }

    @Override
    public void onPageStarted(WebView view, String url, Bitmap favicon) {
        if (view == null) {
            LogUtil.error(TAG, "onPageStarted failed, WebView is null");
            return;
        }

        try {
            super.onPageStarted(view, url, favicon);

            pageUrl = url;

            loadingFinished = false;

            adAssetsLoadedListener.startLoadingAssets();
        }
        catch (Exception e) {
            LogUtil.error(TAG, "onPageStarted failed for url: " + url + " : " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (view == null) {
            LogUtil.error(TAG, "onPageFinished failed, WebView is null");
            return;
        }
        LogUtil.debug(TAG, "onPageFinished: " + view);
        try {

            adAssetsLoadedListener.adAssetsLoaded();

            view.setBackgroundColor(Color.TRANSPARENT);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "onPageFinished failed for url: " + url + " : " + Log.getStackTraceString(e));
        }
    }

    @Override
    public void onLoadResource(WebView view, String url) {
        if (view == null) {
            LogUtil.error(TAG, "onPageStarted failed, WebView is null");
            return;
        }

        if (url != null && url.equals(pageUrl)) {
            return;
        }
        try {
            WebViewBase webViewBase = (WebViewBase) view;

            // IMPORTANT: The code below should be performed only to the iframe of the raw ad (without SDK injections).
            // Need to check webViewBase.containsIFrame() because displayed ad could contain another
            // injected script (for example OpenMeasurement) that initializes own resources.
            // Otherwise, we could get an error: jira/browse/MOBILE-5100
            if (webViewBase.containsIFrame() && webViewBase.isClicked() && !urls.contains(url) && view.getHitTestResult() != null && (view.getHitTestResult()
                                                                                                                                          .getType() == SRC_ANCHOR_TYPE || view.getHitTestResult()
                                                                                                                                                                               .getType() == SRC_IMAGE_ANCHOR_TYPE)) {

                // stop loading the iframe or whatever
                // instead simply change the location of the webview - this will
                // trigger our shouldOverrideUrlLoading
                reloadUrl(view, url);
            }

            urls.add(url);
            super.onLoadResource(view, url);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "onLoadResource failed for url: " + url + " : " + Log.getStackTraceString(e));
        }
    }

    //gets called when an ad is clicked by user. Takes user to the browser or such
    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        LogUtil.debug(TAG, "shouldOverrideUrlLoading, url: " + url);
        if (view == null) {
            LogUtil.error(TAG, "onPageStarted failed, WebView is null");
            return false;
        }

        try {
            WebViewBase webViewBase = ((WebViewBase) view);

            if (!webViewBase.isClicked()) {
                // Get the rendered HTML
                reloadUrl(view, JS__GET_RENDERED_HTML);
                return true;
            }

            handleWebViewClick(url, webViewBase);
        }
        catch (Exception e) {
            LogUtil.error(TAG, "shouldOverrideUrlLoading failed for url: " + url + " : " + Log.getStackTraceString(e));
        }
        return true;
    }

    boolean isLoadingFinished() {
        return loadingFinished;
    }

    @Override
    public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
        super.onReceivedError(view, errorCode, description, failingUrl);
    }

    @Override
    public boolean shouldOverrideKeyEvent(WebView view, KeyEvent event) {
        return super.shouldOverrideKeyEvent(view, event);
    }

    private void reloadUrl(WebView view, String s) {
        view.stopLoading();

        // Get the rendered HTML
        view.loadUrl(s);
    }

    private void handleWebViewClick(String url, WebViewBase webViewBase) {
        urls.clear();

        loadingFinished = false;

        String targetUrl = webViewBase.getTargetUrl();
        url = TextUtils.isEmpty(targetUrl) ? url : targetUrl;

        if (webViewBase.canHandleClick()) {
            loadingFinished = true;
            urls.clear();

            //all(generally non-mraid) comes here - open/click here
            webViewBase.mraidListener.openExternalLink(url);
        }
    }
}
