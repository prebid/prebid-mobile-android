package org.prebid.mobile.rendering.views.webview;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;

import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.webview.AdWebViewClient.AdAssetsLoadedListener;
import org.prebid.mobile.rendering.views.webview.mraid.MraidWebViewClient;

public class AdWebView extends WebView {
    private static final String TAG = AdWebView.class.getSimpleName();
    private Integer mScale;
    private AdWebViewClient mAdWebViewClient;
    protected int mWidth, mHeight;
    protected String mDomain;

    public AdWebView(Context context) {
        super(context);

        //This sets the jsinterface listener to handle open for both mraid & nonmraid ads.
        init();
    }

    @Override
    public void setInitialScale(int scaleInPercent) {
        mScale = scaleInPercent;
    }

    public String getInitialScaleValue() {
        if (mScale != null) {
            return String.valueOf((float) mScale / 100f);
        }

        return null;
    }

    public void setMraidAdAssetsLoadListener(AdAssetsLoadedListener adAssetsLoadedListener,
                                             String mraidScript) {
        if (mAdWebViewClient == null) {
            mAdWebViewClient = new MraidWebViewClient(adAssetsLoadedListener, mraidScript);
        }
        setWebViewClient(mAdWebViewClient);
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

        int deviceWidth = Math.min(screenWidth, screenHeight);
        int deviceHeight = Math.max(screenWidth, screenHeight);

        float factor = calculateFactor(deviceWidth, deviceHeight, mWidth);
        setInitialScale(Math.round(factor));

        initBaseWebSettings(webSettings);
        if (Utils.atLeastKitKat()) {
            getSettings().setSupportZoom(false);

            webSettings.setUseWideViewPort(true);
            webSettings.setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        }
        else {
            webSettings.setSupportZoom(true);
        }
    }

    @SuppressLint("SetJavaScriptEnabled")
    private void initBaseWebSettings(WebSettings webSettings){
        webSettings.setJavaScriptEnabled(true);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(false);
        webSettings.setPluginState(WebSettings.PluginState.OFF);
        webSettings.setCacheMode(WebSettings.LOAD_NO_CACHE);
        webSettings.setLoadWithOverviewMode(true);
    }

    private float calculateFactor(int deviceWidth, int deviceHeight, int creativeWidth) {
        float factor = 100.0f;

        DeviceInfoManager deviceManager = ManagersResolver.getInstance().getDeviceManager();

        int orientation = deviceManager.getDeviceOrientation();

        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if (creativeWidth < deviceHeight) {
                factor = factor * deviceWidth / creativeWidth;
            }
            else {
                factor = factor * deviceHeight / creativeWidth + 1;
            }
        }
        else {
            if (creativeWidth < deviceWidth) {
                factor = factor * deviceWidth / creativeWidth;
            }
            else {
                factor = factor * deviceWidth / creativeWidth + 1;
            }
        }

        if (factor > 100.0f * densityScalingFactor()) {
            factor = (float) (100.0f * densityScalingFactor());
        }
        return factor;
    }

    public double densityScalingFactor() {
        double densityScaleFactor = 0;
        if (getContext() != null) {
            densityScaleFactor = getContext().getResources().getDisplayMetrics().density;
        }
        return densityScaleFactor;
    }

    public void setDomain(String domain) {
        mDomain = domain;
    }
}
