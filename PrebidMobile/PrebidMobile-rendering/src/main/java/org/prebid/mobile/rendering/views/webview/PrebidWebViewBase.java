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

import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.sdk.deviceData.managers.DeviceInfoManager;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

import java.lang.ref.WeakReference;

//Equivalent of adBase
public class PrebidWebViewBase extends FrameLayout
    implements PreloadManager.PreloadedListener, MraidEventsManager.MraidListener {

    private final String TAG = PrebidWebViewBase.class.getSimpleName();
    public static final int WEBVIEW_DESTROY_DELAY_MS = 1000;

    protected Context mContext;
    private final Handler mHandler;

    protected WebViewBase mOldWebViewBase;

    protected WebViewDelegate mWebViewDelegate;

    protected HTMLCreative mCreative;

    protected WebViewBase mWebView;
    protected WebViewBanner mMraidWebView;
    protected int mWidth, mHeight, mDefinedWidthForExpand, mDefinedHeightForExpand;
    protected InterstitialManager mInterstitialManager;


    private int mScreenVisibility;

    protected WebViewBase mCurrentWebViewBase;

    protected Animation mFadeInAnimation;
    protected Animation mFadeOutAnimation;

    public PrebidWebViewBase(Context context, InterstitialManager interstitialManager) {
        //a null context to super(), a framelayout, could crash. So, catch this exception
        super(context);
        mContext = context;
        mInterstitialManager = interstitialManager;
        mScreenVisibility = getVisibility();
        mHandler = new Handler(Looper.getMainLooper());
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

        WebView currentWebView = (mWebView != null) ? mWebView : mMraidWebView;

        // IMPORTANT: Delayed execution was implemented due to this issue: jira/browse/MOBILE-5380
        // We need to give OMID time to finish method execution inside the webview
        mHandler.removeCallbacksAndMessages(null);
        mHandler.postDelayed(new WebViewCleanupRunnable(currentWebView), WEBVIEW_DESTROY_DELAY_MS);
    }

    public void initMraidExpanded() {
        runOnUiThread(() -> {
            try {
                readyForMraidExpanded();
            }
            catch (Exception e) {
                OXLog.error(TAG, "initMraidExpanded failed: " + Log.getStackTraceString(e));
            }
        });
    }

    private void readyForMraidExpanded() {
        if (mMraidWebView != null && mMraidWebView.getMRAIDInterface() != null) {
            mMraidWebView.getMRAIDInterface().onReadyExpanded();
        }
    }

    @Override
    public void preloaded(WebViewBase adBaseView) {
        //do it in child
    }

    public void handleOpen(String url) {
        if (mCurrentWebViewBase != null && mCurrentWebViewBase.getMRAIDInterface() != null) {
            mCurrentWebViewBase.getMRAIDInterface().open(url);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasWindowFocus) {
        super.onWindowFocusChanged(hasWindowFocus);
        int visibility = (!hasWindowFocus ? View.INVISIBLE : View.VISIBLE);
        if (Utils.hasScreenVisibilityChanged(mScreenVisibility, visibility)) {
            //visibility has changed. Send the changed value for mraid update for banners
            mScreenVisibility = visibility;
            if (mCurrentWebViewBase != null && mCurrentWebViewBase.getMRAIDInterface() != null) {
                mCurrentWebViewBase.getMRAIDInterface().handleScreenViewabilityChange(Utils.isScreenVisible(mScreenVisibility));
            }
        }
    }

    @Override
    public void openExternalLink(String url) {
        //No need to separate the apis for mraid & non-mraid as they all go to the same methods.
        if (mWebViewDelegate != null) {
            mWebViewDelegate.webViewShouldOpenExternalLink(url);
        }
    }

    @Override
    public void openMraidExternalLink(String url) {
        if (mWebViewDelegate != null) {
            mWebViewDelegate.webViewShouldOpenMRAIDLink(url);
        }
    }

    @Override
    public void onAdWebViewWindowFocusChanged(boolean hasFocus) {
        if (mCreative != null) {
            mCreative.changeVisibilityTrackerState(hasFocus);
        }
    }

    public void onViewExposureChange(ViewExposure viewExposure) {
        if (mCurrentWebViewBase != null && mCurrentWebViewBase.getMRAIDInterface() != null) {
            mCurrentWebViewBase.getMRAIDInterface().getJsExecutor().executeExposureChange(viewExposure);
        }
    }

    public WebViewBase getOldWebView() {
        return mOldWebViewBase;
    }

    public void setOldWebView(WebViewBase oldWebView) {
        mOldWebViewBase = oldWebView;
    }

    public void setWebViewDelegate(WebViewDelegate delegate) {
        mWebViewDelegate = delegate;
    }

    public HTMLCreative getCreative() {
        return mCreative;
    }

    public void setCreative(HTMLCreative creative) {
        mCreative = creative;
    }

    public WebViewBase getWebView() {
        return mWebView;
    }

    public WebViewBanner getMraidWebView() {
        return mMraidWebView;
    }

    //gets expand properties & also a close view(irrespective of usecustomclose is false)
    public void loadMraidExpandProperties() {
        //do it in child classs
    }

    protected void renderAdView(WebViewBase webViewBase) {
        if (webViewBase == null) {
            OXLog.warn(TAG, "WebviewBase is null");
            return;
        }
        if (getContext() != null) {
            mFadeInAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_in);
        }

        if (webViewBase.isMRAID() && webViewBase.getMRAIDInterface() != null) {
            webViewBase.getMRAIDInterface().getJsExecutor().executeOnViewableChange(true);
        }

        webViewBase.startAnimation(mFadeInAnimation);
        webViewBase.setVisibility(View.VISIBLE);

        displayAdViewPlacement(webViewBase);
    }

    protected void displayAdViewPlacement(WebViewBase webViewBase) {
        renderPlacement(webViewBase, mWidth, mHeight);

        if (webViewBase.getAdWidth() != 0) {
            getLayoutParams().width = webViewBase.getAdWidth();
        }

        if (webViewBase.getAdHeight() != 0) {
            getLayoutParams().height = webViewBase.getAdHeight();
        }
        invalidate();
    }

    private void renderPlacement(WebViewBase webViewBase, int width, int height) {
        if (mContext == null) {
            OXLog.warn(TAG, "Context is null");
            return;
        }

        if (webViewBase == null) {
            OXLog.warn(TAG, "WebviewBase is null");
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
            if (mWidth < deviceHeight) {
                factor = factor * deviceWidth / mWidth;
            }
            else {
                factor = factor * deviceHeight / mWidth;
            }
        }
        else {
            if (mWidth < deviceWidth) {
                factor = factor * deviceWidth / mWidth;
            }
            else {
                factor = factor * deviceWidth / mWidth;
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

    private static final class WebViewCleanupRunnable implements Runnable {
        private static final String TAG = WebViewCleanupRunnable.class.getSimpleName();

        private final WeakReference<WebView> mWeakWebView;

        WebViewCleanupRunnable(WebView webViewBase) {
            mWeakWebView = new WeakReference<>(webViewBase);
        }

        @Override
        public void run() {
            WebView webViewBase = mWeakWebView.get();
            if (webViewBase == null) {
                Log.d(TAG, "Unable to execute destroy on WebView. WebView is null.");
                return;
            }
            //MOBILE-2950 ARKAI3 - Inline Video of the webview is not stopped on back key press
            webViewBase.destroy();
        }
    }
}
