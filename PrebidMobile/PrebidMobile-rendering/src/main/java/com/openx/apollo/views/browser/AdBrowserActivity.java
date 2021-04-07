package com.openx.apollo.views.browser;

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

import com.openx.apollo.utils.broadcast.local.BaseLocalBroadcastReceiver;
import com.openx.apollo.utils.constants.IntentActions;

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

    private WebView mWebView;
    private VideoView mVideoView;
    private BrowserControls mBrowserControls;

    private boolean mIsVideo;
    private boolean mShouldFireEvents;
    private int mBroadcastId;
    private String mUrl;

    //https://jira.corp.openx.com/browse/MOBILE-1222 - Enable physical BACK button of the device in the in-app browser in Android
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {
            if (mWebView != null) {
                mWebView.goBack();
            }
            finish();
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mVideoView != null) {
            mVideoView.suspend();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mVideoView != null) {
            mVideoView.resume();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initWindow();

        final Bundle extras = getIntent().getExtras();
        claimExtras(extras);

        if (mIsVideo) {
            handleVideoDisplay();
        }
        else {
            handleWebViewDisplay();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Solves memory leak
        if (mWebView != null) {
            mWebView.destroy();
        }

        if (mVideoView != null) {
            mVideoView.suspend();
        }
    }

    // Ignore back press on ad browser, except on video since there is no close
    @Override
    public void onBackPressed() {
        if (mIsVideo) {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageFinished() {
        if (mBrowserControls != null) {
            mBrowserControls.updateNavigationButtonsState();
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

        mUrl = extras.getString(EXTRA_URL, null);
        mShouldFireEvents = extras.getBoolean(EXTRA_SHOULD_FIRE_EVENTS, true);
        mIsVideo = extras.getBoolean(EXTRA_IS_VIDEO, false);
        mBroadcastId = extras.getInt(EXTRA_BROADCAST_ID, -1);
    }

    private void handleWebViewDisplay() {
        initBrowserControls();

        RelativeLayout contentLayout = new RelativeLayout(this);
        RelativeLayout.LayoutParams barLayoutParams = null;
        RelativeLayout.LayoutParams webViewLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                                                          LayoutParams.MATCH_PARENT);

        if (!TextUtils.isEmpty(mUrl)) {
            mWebView = new WebView(this);
            setWebViewSettings();

            mWebView.setWebViewClient(new AdBrowserActivityWebViewClient(AdBrowserActivity.this));
            mWebView.loadUrl(mUrl);

            barLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                                                              LayoutParams.WRAP_CONTENT);
            if (mBrowserControls != null) {
                mBrowserControls.showNavigationControls();
            }

            webViewLayoutParams.addRule(RelativeLayout.BELOW, BROWSER_CONTROLS_ID);
        }

        if (mWebView != null) {
            contentLayout.addView(mWebView, webViewLayoutParams);
        }

        if (mBrowserControls != null) {
            contentLayout.addView(mBrowserControls, barLayoutParams);
        }

        setContentView(contentLayout);
    }

    private void handleVideoDisplay() {
        mVideoView = new VideoView(this);
        RelativeLayout content = new RelativeLayout(this);
        RelativeLayout.LayoutParams lp = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
        lp.addRule(RelativeLayout.CENTER_IN_PARENT);
        content.addView(mVideoView, lp);
        setContentView(content);
        MediaController mc = new MediaController(this);
        mVideoView.setMediaController(mc);
        mVideoView.setVideoURI(Uri.parse(mUrl));
        mVideoView.start();
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void setWebViewSettings() {

        if (mWebView != null) {
            mWebView.getSettings().setJavaScriptEnabled(true);
            mWebView.getSettings().setJavaScriptCanOpenWindowsAutomatically(false);
            mWebView.getSettings().setPluginState(WebSettings.PluginState.OFF);
            mWebView.setHorizontalScrollBarEnabled(false);
            mWebView.setVerticalScrollBarEnabled(false);
            mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);

            mWebView.getSettings().setBuiltInZoomControls(true);

            //MOB-2160 - Ad Browser: Leaking memory on zoom.(Though it looks like a chromiun webview problem)
            //Activity com.openx.apollo.views.browser.AdBrowserActivity has leaked window android.widget.ZoomButtonsController$Container
            mWebView.getSettings().setDisplayZoomControls(false);

            mWebView.getSettings().setLoadWithOverviewMode(true);
            mWebView.getSettings().setUseWideViewPort(true);
        }
    }

    private void initBrowserControls() {
        BrowserControls controls = new BrowserControls(this, new BrowserControlsEventsListener() {

            @Override
            public void onRelaod() {
                if (mWebView != null) {
                    mWebView.reload();
                }
            }

            @Override
            public void onGoForward() {
                if (mWebView != null) {
                    mWebView.goForward();
                }
            }

            @Override
            public void onGoBack() {
                if (mWebView != null) {
                    mWebView.goBack();
                }
            }

            @Override
            public String getCurrentURL() {
                if (mWebView != null) {
                    return mWebView.getUrl();
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
                if (mWebView != null) {
                    return mWebView.canGoForward();
                }
                return false;
            }

            @Override
            public boolean canGoBack() {
                if (mWebView != null) {
                    return mWebView.canGoBack();
                }
                return false;
            }
        });
        controls.setId(BROWSER_CONTROLS_ID);
        mBrowserControls = controls;
    }

    private void sendLocalBroadcast(String action) {
        if (!mShouldFireEvents) {
            return;
        }

        BaseLocalBroadcastReceiver.sendLocalBroadcast(getApplicationContext(),
                                                      mBroadcastId,
                                                      action);
    }
}