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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.webview.AdWebViewClient.AdAssetsLoadedListener;
import org.prebid.mobile.rendering.views.webview.mraid.MraidWebViewClient;

public class AdWebView extends WebView {

    private static final String TAG = AdWebView.class.getSimpleName();
    protected Integer scale;
    private AdWebViewClient adWebViewClient;
    protected int width, height;
    protected String domain;

    public AdWebView(Context context) {
        super(context);

        //This sets the jsinterface listener to handle open for both mraid & nonmraid ads.
        init();
    }

    @Override
    public void setInitialScale(int scaleInPercent) {
        scale = scaleInPercent;
    }

    public String getInitialScaleValue() {
        if (scale != null) {
            return String.valueOf((float) scale / 100f);
        }

        return null;
    }

    public void setMraidAdAssetsLoadListener(AdAssetsLoadedListener adAssetsLoadedListener,
                                             String mraidScript) {
        if (adWebViewClient == null) {
            adWebViewClient = new MraidWebViewClient(adAssetsLoadedListener, mraidScript);
        }
        setWebViewClient(adWebViewClient);
    }

    protected void init() {
        initializeWebView();
        initializeWebSettings();
    }

    protected void initializeWebView() {
        setScrollBarStyle(SCROLLBARS_INSIDE_OVERLAY);
        setFocusable(true);

        setHorizontalScrollBarEnabled(false);
        setVerticalScrollBarEnabled(false);
    }

    protected void initializeWebSettings() {
        WebSettings webSettings = getSettings();
        int screenWidth = 0;
        int screenHeight = 0;

        if (getContext() != null) {
            WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
            screenWidth = Utils.getScreenWidth(windowManager);
            screenHeight = Utils.getScreenHeight(windowManager);
        }

        if (this instanceof WebViewInterstitial) {
            calculateScaleForResize(screenWidth, screenHeight, width, height);
        } else {
            webSettings.setLoadWithOverviewMode(true);
        }

        initBaseWebSettings(webSettings);
        if (Utils.atLeastKitKat()) {
            getSettings().setSupportZoom(false);

            webSettings.setUseWideViewPort(true);
            webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        } else {
            webSettings.setSupportZoom(true);
        }
    }

    private void calculateScaleForResize(
        int screenWidth,
        int screenHeight,
        int creativeWidth,
        int creativeHeight
    ) {
        double screenRatio = ((double) screenWidth) / screenHeight;
        double creativeRatio = ((double) creativeWidth) / creativeHeight;

        double scaledScreenWidth = screenWidth / densityScalingFactor();
        double scaledScreenHeight = screenHeight / densityScalingFactor();

        double initialScale;
        double factor;
        boolean creativeRatioIsLess = creativeRatio <= screenRatio;

        if (scaledScreenWidth >= creativeWidth && scaledScreenHeight >= creativeHeight) {
            setInitialScale(100);
        } else {
            if (creativeRatioIsLess) {
                initialScale = scaledScreenWidth / creativeWidth;
                double newCreativeHeight = creativeHeight * initialScale;
                factor = newCreativeHeight / scaledScreenHeight;
            } else {
                initialScale = scaledScreenHeight / creativeHeight;
                double newCreativeWidth = creativeWidth * initialScale;
                factor = newCreativeWidth / scaledScreenWidth;
            }

            int scaleInPercent = (int) (initialScale / factor * 100);
            setInitialScale(scaleInPercent);
            Log.d(TAG, "Using custom WebView scale: " + scaleInPercent);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initBaseWebSettings(WebSettings webSettings) {
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setPluginState(WebSettings.PluginState.OFF);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setUseWideViewPort(true);
    }

    public double densityScalingFactor() {
        double densityScaleFactor = 0;
        if (getContext() != null) {
            densityScaleFactor = getContext().getResources().getDisplayMetrics().density;
        }
        return densityScaleFactor;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }
}
