package org.prebid.mobile.rendering.views.webview;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;

import org.json.JSONObject;
import org.prebid.mobile.rendering.R;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.handler.FetchPropertiesHandler;
import org.prebid.mobile.rendering.sdk.JSLibraryManager;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

public class OpenXWebViewBanner extends OpenXWebViewBase
    implements PreloadManager.PreloadedListener, MraidEventsManager.MraidListener {

    private static final String TAG = OpenXWebViewBanner.class.getSimpleName();

    private final FetchPropertiesHandler.FetchPropertyCallback mExpandPropertiesCallback = new FetchPropertiesHandler.FetchPropertyCallback() {
        @Override
        public void onResult(String propertyJson) {
            handleExpandPropertiesResult(propertyJson);
        }

        @Override
        public void onError(Throwable throwable) {
            OXLog.error(TAG, "executeGetExpandProperties failed: " + Log.getStackTraceString(throwable));
        }
    };

    public OpenXWebViewBanner(Context context, InterstitialManager interstitialManager) {
        super(context, interstitialManager);
        setId(R.id.web_view_banner);
    }

    //gets expand properties & also a close view(irrespective of usecustomclose is false)
    public void loadMraidExpandProperties() {
        Context context = getContext();
        if (!(context instanceof Activity)) {
            OXLog.warn(TAG, "Context is null or is not activity context");
            return;
        }

        /*
         * If it's MRAID, we have to check the Ad designer's request to launch
         * the ad in a particular expanded size by checking the ad's
         * ExpandProperties per the MRAID spec. So we go to the js and extract these
         * properties and then the layout gets built based on these things.
         */
        //Fix MOBILE-2944 App crash navigating MRAID ad
        final WebViewBase currentWebView = (mWebView != null) ? mWebView : mMraidWebView;

        if (currentWebView != null) {
            currentWebView.getMRAIDInterface()
                          .getJsExecutor()
                          .executeGetExpandProperties(new FetchPropertiesHandler(mExpandPropertiesCallback));
        }
        else {
            OXLog.warn(TAG, "Error getting expand properties");
        }
    }

    @Override
    public void initTwoPartAndLoad(String url) {

        LayoutParams layoutParams = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        setLayoutParams(layoutParams);
        //A null context can crash with an exception in webView creation through WebViewBanner. Catch it
        mMraidWebView = new WebViewBanner(mContext, this, this);
        mMraidWebView.setJSName("twopart");

        String script = JSLibraryManager.getInstance(mMraidWebView.getContext()).getMRAIDScript();
        //inject mraid.js
        mMraidWebView.setMraidAdAssetsLoadListener(mMraidWebView, script);

        mMraidWebView.loadUrl(url);
    }

    @Override
    public void loadHTML(String html, int width, int height) {
        FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT);

        setLayoutParams(layoutParams);
        mWidth = width;
        mHeight = height;
        //A null context can crash with an exception in webView creation through WebViewBanner. Catch it
        mWebView = new WebViewBanner(mContext, html, width, height, this, this);
        mWebView.setJSName("1part");
        mWebView.initContainsIFrame(mCreative.getCreativeModel().getHtml());
        mWebView.setTargetUrl(mCreative.getCreativeModel().getTargetUrl());
        mWebView.loadAd();
    }

    @Override
    public void preloaded(WebViewBase adBaseView) {

        if (adBaseView == null) {

            //This should never happen.
            OXLog.error(TAG, "Failed to preload a banner ad. Webview is null.");

            if (mWebViewDelegate != null) {
                mWebViewDelegate.webViewFailedToLoad(new AdException(AdException.INTERNAL_ERROR, "Preloaded adview is null!"));
            }

            return;
        }
        mCurrentWebViewBase = adBaseView;
        if (mCurrentWebViewBase.mMRAIDBridgeName.equals("twopart")) {
            //SHould have expanded url here, as last param
            mInterstitialManager.displayOpenXWebViewForMRAID(mMraidWebView, true);
        }
        else {
            if (adBaseView.getParent() == null) {

                if (getChildCount() >= 1) {

                    OXLog.debug(TAG, "Adding second view");
                    //safe removal from parent before adding
                    Views.removeFromParent(adBaseView);

                    addView(adBaseView, 1);
                    adBaseView.bringToFront();
                    swapWebViews();
                }
                else {
                    OXLog.debug(TAG, "Adding first view");
                    //safe removal from parent before adding
                    Views.removeFromParent(adBaseView);

                    addView(adBaseView, 0);
                    renderAdView(adBaseView);
                }
            }
            else {
                OXLog.debug(TAG, "Adding the only view");

                adBaseView.bringToFront();
                swapWebViews();
            }
        }
        /*
         * This postInvalidate fixes the cosmetic issue that KitKat created with the white banner
         * fragment/remnant showing up at the bottom of the screen.
         */
        if (mContext instanceof Activity) {
            ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content).postInvalidate();
            ((Activity) mContext).getWindow().getDecorView().findViewById(android.R.id.content).postInvalidateDelayed(100);
        }

        if (mWebViewDelegate != null) {
            mWebViewDelegate.webViewReadyToDisplay();
        }
    }

    protected void swapWebViews() {
        if (getContext() != null) {
            mFadeOutAnimation = AnimationUtils.loadAnimation(getContext(), android.R.anim.fade_out);
        }
        final WebViewBase frontAdView = (WebViewBase) getChildAt(0);
        WebViewBase backAdView = (WebViewBase) getChildAt(1);

        if (frontAdView != null) {
            frontAdView.startAnimation(mFadeOutAnimation);
            frontAdView.setVisibility(GONE);
        }

        if (backAdView != null) {
            renderAdView(backAdView);
            backAdView.bringToFront();
        }
    }

    private void handleExpandPropertiesResult(String expandProperties) {
        JSONObject jsonExpandProperties;

        WebViewBase currentWebView = (mWebView != null)
                                     ? mWebView
                                     : mMraidWebView;
        final MraidVariableContainer mraidVariableContainer = currentWebView.getMRAIDInterface().getMraidVariableContainer();
        mraidVariableContainer.setExpandProperties(expandProperties);

        try {
            jsonExpandProperties = new JSONObject(expandProperties);

            mDefinedWidthForExpand = jsonExpandProperties.optInt("width", 0);
            mDefinedHeightForExpand = jsonExpandProperties.optInt("height", 0);

            if (mInterstitialManager.getInterstitialDisplayProperties() != null) {
                mInterstitialManager.getInterstitialDisplayProperties().expandWidth = mDefinedWidthForExpand;
                mInterstitialManager.getInterstitialDisplayProperties().expandHeight = mDefinedHeightForExpand;
            }
        }
        catch (Exception e) {
            OXLog.error(TAG, "handleExpandPropertiesResult: Failed. Reason: " + Log.getStackTraceString(e));
        }
    }
}
