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

package org.prebid.mobile.rendering.views.browser;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.VideoView;
import org.prebid.mobile.rendering.utils.broadcast.local.BaseLocalBroadcastReceiver;
import org.prebid.mobile.rendering.utils.constants.IntentActions;

/**
 * Custom browser for internal usage. Responsible for modal advertisement
 * showing.
 */
@SuppressLint("NewApi")
public final class AdBrowserActivity extends Activity
    implements AdBrowserActivityWebViewClient.AdBrowserWebViewClientListener {

    private static final String TAG = AdBrowserActivity.class.getSimpleName();

    public static final String EXTRA_DENSITY_SCALING_ENABLED = "densityScalingEnabled";
    public static final String EXTRA_IS_VIDEO = "EXTRA_IS_VIDEO";
    public static final String EXTRA_BROADCAST_ID = "EXTRA_BROADCAST_ID";
    public static final String EXTRA_URL = "EXTRA_URL";
    public static final String EXTRA_SHOULD_FIRE_EVENTS = "EXTRA_SHOULD_FIRE_EVENTS";
    public static final String EXTRA_ALLOW_ORIENTATION_CHANGES = "EXTRA_ALLOW_ORIENTATION_CHANGES";

    private static final int BROWSER_CONTROLS_ID = 235799;

    private WebView webView;
    private VideoView videoView;
    private BrowserControls browserControls;

    private boolean isVideo;
    private boolean shouldFireEvents;
    private int broadcastId;
    private String url;

    //jira/browse/MOBILE-1222 - Enable physical BACK button of the device in the in-app browser in Android
    @Override
    public boolean onKeyDown(
            int keyCode,
            KeyEvent event
    ) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (webView != null) {
                webView.goBack();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (videoView != null) {
            videoView.suspend();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (videoView != null) {
            videoView.resume();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWindow();

        final Bundle extras = getIntent().getExtras();
        claimExtras(extras);

        if (isVideo) {
            handleVideoDisplay();
        } else {
            handleWebViewDisplay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Solves memory leak
        if (webView != null) {
            webView.destroy();
        }

        if (videoView != null) {
            videoView.suspend();
        }
    }

    // Ignore back press on ad browser, except on video since there is no close
    @Override
    public void onBackPressed() {
        if (isVideo) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageFinished() {
        if (browserControls != null) {
            browserControls.updateNavigationButtonsState();
        }
    }

    @Override
    public void onUrlHandleSuccess() {
        finish();
    }

    private void initWindow() {
        final Window window = getWindow();
        window.setBackgroundDrawable(new ColorDrawable(0xFFFFFFFF));
        window.setFlags(WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED, WindowManager.LayoutParams.FLAG_HARDWARE_ACCELERATED);
        window.setSoftInputMode(EditorInfo.IME_ACTION_DONE);
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void claimExtras(Bundle extras) {
        if (extras == null) {
            return;
        }

        url = extras.getString(EXTRA_URL, null);
        shouldFireEvents = extras.getBoolean(EXTRA_SHOULD_FIRE_EVENTS, true);
        isVideo = extras.getBoolean(EXTRA_IS_VIDEO, false);
        broadcastId = extras.getInt(EXTRA_BROADCAST_ID, -1);
    }

    private void handleWebViewDisplay() {
        initBrowserControls();

        RelativeLayout contentLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams barLayoutParams = null;
        RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );

        if (!TextUtils.isEmpty(url)) {
            webView = new WebView(this);
            setWebViewSettings();

            webView.setWebViewClient(new AdBrowserActivityWebViewClient(AdBrowserActivity.this));
            webView.loadUrl(url);

            barLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
            if (browserControls != null) {
                browserControls.showNavigationControls();
            }

            webViewLayoutParams.addRule(RelativeLayout.BELOW, BROWSER_CONTROLS_ID);
        }

        if (webView != null) {
            contentLayout.addView(webView, webViewLayoutParams);
        }

        if (browserControls != null) {
            contentLayout.addView(browserControls, barLayoutParams);
        }

        setContentView(contentLayout);
    }

    private void handleVideoDisplay() {
        videoView = new VideoView(this);
        RelativeLayout content = new RelativeLayout(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(
                LayoutParams.MATCH_PARENT,
                LayoutParams.MATCH_PARENT
        );
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        content.addView(videoView, lp);
        setContentView(content);
        MediaController mc = new MediaController(this);
        videoView.setMediaController(mc);
        videoView.setVideoURI(Uri.parse(url));
        videoView.start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewSettings() {

        if (webView != null) {
            webView.getSettings().setJavaScriptEnabled(true);
            webView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            webView.getSettings().setPluginState(WebSettings.PluginState.OFF);
            webView.setHorizontalScrollBarEnabled(false);
            webView.setVerticalScrollBarEnabled(false);
            webView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

            webView.getSettings().setBuiltInZoomControls(true);

            //MOB-2160 - Ad Browser: Leaking memory on zoom.(Though it looks like a chromiun webview problem)
            //Activity browser.AdBrowserActivity has leaked window android.widget.ZoomButtonsController$Container
            webView.getSettings().setDisplayZoomControls(false);

            webView.getSettings().setLoadWithOverviewMode(true);
            webView.getSettings().setUseWideViewPort(true);
        }
    }

    private void initBrowserControls() {
        BrowserControls controls = new BrowserControls(this, new BrowserControlsEventsListener() {

            @Override
            public void onRelaod() {
                if (webView != null) {
                    webView.reload();
                }
            }

            @Override
            public void onGoForward() {
                if (webView != null) {
                    webView.goForward();
                }
            }

            @Override
            public void onGoBack() {
                if (webView != null) {
                    webView.goBack();
                }
            }

            @Override
            public String getCurrentURL() {
                if (webView != null) {
                    return webView.getUrl();
                }
                return null;
            }

            @Override
            public void closeBrowser() {
                finish();
                sendLocalBroadcast(IntentActions.ACTION_BROWSER_CLOSE);
            }

            @Override
            public boolean canGoForward() {
                if (webView != null) {
                    return webView.canGoForward();
                }
                return false;
            }

            @Override
            public boolean canGoBack() {
                if (webView != null) {
                    return webView.canGoBack();
                }
                return false;
            }
        });
        controls.setId(BROWSER_CONTROLS_ID);
        browserControls = controls;
    }

    private void sendLocalBroadcast(String action) {
        if (!shouldFireEvents) {
            return;
        }

        BaseLocalBroadcastReceiver.sendLocalBroadcast(getApplicationContext(), broadcastId, action);
    }
}