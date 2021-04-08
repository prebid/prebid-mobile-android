package org.prebid.mobile.rendering.views.webview;

import android.content.Context;

import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

public class OpenXWebViewInterstitial extends OpenXWebViewBase
    implements PreloadManager.PreloadedListener, MraidEventsManager.MraidListener {

    private final String TAG = OpenXWebViewInterstitial.class.getSimpleName();

    public OpenXWebViewInterstitial(Context context, InterstitialManager interstitialManager) {
        super(context, interstitialManager);
    }

    @Override
    public void loadHTML(String html, int width, int height) {
        LayoutParams layoutParams = new LayoutParams(
            LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        setLayoutParams(layoutParams);
        mWidth = width;
        mHeight = height;
        //A null context can crash with an exception in webView creation through WebViewBanner. Catch it
        mWebView = new WebViewInterstitial(mContext, html, width, height, this, this);
        mWebView.setJSName("WebViewInterstitial");
        mWebView.initContainsIFrame(mCreative.getCreativeModel().getHtml());
        mWebView.setTargetUrl(mCreative.getCreativeModel().getTargetUrl());
        mWebView.loadAd();
    }

    @Override
    public void preloaded(WebViewBase adBaseView) {
        if (adBaseView == null) {
            //This should never happen.
            OXLog.error(TAG, "Failed to preload an interstitial. Webview is null.");

            if (mWebViewDelegate != null) {
                mWebViewDelegate.webViewFailedToLoad(new AdException(AdException.INTERNAL_ERROR, "Preloaded adview is null!"));
            }
            return;
        }
        mCurrentWebViewBase = adBaseView;

        if (mWebViewDelegate != null) {
            mWebViewDelegate.webViewReadyToDisplay();
        }
    }
}