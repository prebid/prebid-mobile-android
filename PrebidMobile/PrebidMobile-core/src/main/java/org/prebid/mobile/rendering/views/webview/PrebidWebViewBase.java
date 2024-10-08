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
import android.content.res.Configuration;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.webkit.WebView;
import android.widget.FrameLayout;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

//Equivalent of adBase
public class PrebidWebViewBase extends FrameLayout implements PreloadManager.PreloadedListener, MraidEventsManager.MraidListener {

    private final String TAG = PrebidWebViewBase.class.getSimpleName();
    public static final int WEBVIEW_DESTROY_DELAY_MS = 1000;

    protected Context context;
    private final Handler handler;

    protected WebViewBase oldWebViewBase;

    protected WebViewDelegate webViewDelegate;

    protected HTMLCreative creative;

    protected WebViewBase webView;
    protected WebViewBanner mraidWebView;
    protected int width, height, definedWidthForExpand, definedHeightForExpand;
    protected InterstitialManager interstitialManager;


    private int screenVisibility;

    protected WebViewBase currentWebViewBase;

    protected Animation fadeInAnimation;
    protected Animation fadeOutAnimation;

    public PrebidWebViewBase(
            Context context,
            InterstitialManager interstitialManager
    ) {
        //a null context to super(), a framelayout, could crash. So, catch this exception
        super(context);
        this.context = context;
        this.interstitialManager = interstitialManager;
        screenVisibility = getVisibility();
        handler = new Handler(Looper.getMainLooper());
    }

    public void initTwoPartAndLoad(String url) {
        //do it in banner child class
    }

    public void loadHTML(String html, int width, int height) {
        //call child's
    }

    public void destroy() {
        Views.removeFromParent(this);
        removeAllViews();

        WebView currentWebView = (webView != null) ? webView : mraidWebView;

        // IMPORTANT: Delayed execution was implemented due to this issue: jira/browse/MOBILE-5380
        // We need to give OMID time to finish method execution inside the webview
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new WebViewCleanupRunnable(currentWebView), WEBVIEW_DESTROY_DELAY_MS);
        webView = null;
        mraidWebView = null;
    }

    public void initMraidExpanded() {
        runOnUiThread(() -> {
            try {
                readyForMraidExpanded();
            }
            catch (Exception e) {
                LogUtil.error(TAG, "initMraidExpanded failed: " + Log.getStackTraceString(e));
            }
        });
    }

    private void readyForMraidExpanded() {
        if (mraidWebView != null && mraidWebView.getMRAIDInterface() != null) {
            mraidWebView.getMRAIDInterface().onReadyExpanded();
        }
    }

    @Override
    public void preloaded(WebViewBase adBaseView) {
        //do it in child
    }

    public void handleOpen(String url) {
        if (currentWebViewBase != null && currentWebViewBase.getMRAIDInterface() != null) {
            currentWebViewBase.getMRAIDInterface().open(url);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int visibility = (!hasWindowFocus ? View.INVISIBLE : View.VISIBLE);
        if (Utils.hasScreenVisibilityChanged(screenVisibility, visibility)) {
            //visibility has changed. Send the changed value for mraid update for banners
            screenVisibility = visibility;
            if (currentWebViewBase != null && currentWebViewBase.getMRAIDInterface() != null) {
                currentWebViewBase.getMRAIDInterface()
                                  .handleScreenViewabilityChange(Utils.isScreenVisible(screenVisibility));
            }
        }
    }

    @Override
    public void openExternalLink(String url) {
        //No need to separate the apis for mraid & non-mraid as they all go to the same methods.
        if (webViewDelegate != null) {
            webViewDelegate.webViewShouldOpenExternalLink(url);
        }
    }

    @Override
    public void openMraidExternalLink(String url) {
        if (webViewDelegate != null) {
            webViewDelegate.webViewShouldOpenMRAIDLink(url);
        }
    }

    @Override
    public void onAdWebViewWindowFocusChanged(boolean hasFocus) {
        if (creative != null) {
            creative.changeVisibilityTrackerState(hasFocus);
        }
    }

    public void onViewExposureChange(ViewExposure viewExposure) {
        if (currentWebViewBase != null && currentWebViewBase.getMRAIDInterface() != null) {
            currentWebViewBase.getMRAIDInterface().getJsExecutor().executeExposureChange(viewExposure);
        }
    }

    public WebViewBase getOldWebView() {
        return oldWebViewBase;
    }

    public void setOldWebView(WebViewBase oldWebView) {
        oldWebViewBase = oldWebView;
    }

    public void setWebViewDelegate(WebViewDelegate delegate) {
        webViewDelegate = delegate;
    }

    public HTMLCreative getCreative() {
        return creative;
    }

    public void setCreative(HTMLCreative creative) {
        this.creative = creative;
    }

    public WebViewBase getWebView() {
        return webView;
    }

    public WebViewBanner getMraidWebView() {
        return mraidWebView;
    }

    //gets expand properties & also a close view(irrespective of usecustomclose is false)
    public void loadMraidExpandProperties() {
        //do it in child classs
    }

    protected void renderAdView(WebViewBase webViewBase) {
        if (webViewBase == null) {
            LogUtil.warning(TAG, "WebviewBase is null");
            return;
        }
        if (getContext() != null) {
            fadeInAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        }

        if (webViewBase.isMRAID() && webViewBase.getMRAIDInterface() != null) {
            webViewBase.getMRAIDInterface().getJsExecutor().executeOnViewableChange(true);
        }

        webViewBase.startAnimation(fadeInAnimation);
        webViewBase.setVisibility(View.VISIBLE);

        displayAdViewPlacement(webViewBase);
    }

    protected void displayAdViewPlacement(WebViewBase webViewBase) {
        renderPlacement(webViewBase, width, height);

        if (webViewBase.getAdWidth() != 0) {
            getLayoutParams().width = webViewBase.getAdWidth();
        }

        if (webViewBase.getAdHeight() != 0) {
            getLayoutParams().height = webViewBase.getAdHeight();
        }
        invalidate();
    }

    private void renderPlacement(WebViewBase webViewBase, int width, int height) {
        if (context == null) {
            LogUtil.warning(TAG, "Context is null");
            return;
        }

        if (webViewBase == null) {
            LogUtil.warning(TAG, "WebviewBase is null");
            return;
        }

        int orientation = Configuration.ORIENTATION_UNDEFINED;

        WindowManager windowManager = (WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE);
        int screenWidth = Utils.getScreenWidth(windowManager);
        int screenHeight = Utils.getScreenHeight(windowManager);

        int deviceWidth = Math.min(screenWidth, screenHeight);
        int deviceHeight = Math.max(screenWidth, screenHeight);

        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();
        if (deviceManager != null) {
            orientation = deviceManager.getDeviceOrientation();
        }

        float factor = getScaleFactor(webViewBase, orientation, deviceWidth, deviceHeight);

        webViewBase.setAdWidth(Math.round((width * factor)));
        webViewBase.setAdHeight(Math.round((height * factor)));
    }

    private float getScaleFactor(WebViewBase webViewBase, int orientation, int deviceWidth, int deviceHeight) {
        float factor = 1.0f;

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (width < deviceHeight) {
                factor = factor * deviceWidth / width;
            } else {
                factor = factor * deviceHeight / width;
            }
        }
        else {
            if (width < deviceWidth) {
                factor = factor * deviceWidth / width;
            } else {
                factor = factor * deviceWidth / width;
            }
        }

        if (factor > webViewBase.densityScalingFactor()) {
            factor = (float) (1.0f * webViewBase.densityScalingFactor());
        }

        return factor;
    }

    protected void runOnUiThread(Runnable runnable) {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(runnable);
    }

    public void setActionUrl(ActionUrl actionUrl) {
        webView.setActionUrl(actionUrl);
    }

    private static final class WebViewCleanupRunnable implements Runnable {
        private static final String TAG = WebViewCleanupRunnable.class.getSimpleName();

        private final WeakReference<WebView> weakWebView;

        WebViewCleanupRunnable(WebView webViewBase) {
            weakWebView = new WeakReference<>(webViewBase);
        }

        @Override
        public void run() {
            WebView webViewBase = weakWebView.get();
            if (webViewBase == null) {
                LogUtil.debug(TAG, "Unable to execute destroy on WebView. WebView is null.");
                return;
            }
            //MOBILE-2950 ARKAI3 - Inline Video of the webview is not stopped on back key press
            webViewBase.destroy();
        }
    }
}
