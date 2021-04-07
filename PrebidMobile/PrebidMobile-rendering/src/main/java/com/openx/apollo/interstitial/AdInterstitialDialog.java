package com.openx.apollo.interstitial;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.JSInterface;
import com.openx.apollo.views.webview.mraid.Views;

@SuppressLint("NewApi")
public class AdInterstitialDialog extends AdBaseDialog {
    private static final String TAG = AdInterstitialDialog.class.getSimpleName();

    /**
     * @param context                  activity context.
     * @param webViewBaseLocal         webview with ad.
     * @param adViewContainer          container for ad.
     */
    public AdInterstitialDialog(Context context, WebViewBase webViewBaseLocal,
                                FrameLayout adViewContainer,
                                InterstitialManager interstitialManager) {
        super(context, webViewBaseLocal, interstitialManager);
        mAdViewContainer = adViewContainer;


        preInit();
        if (mInterstitialManager.getInterstitialDisplayProperties() != null) {
            mAdViewContainer.setBackgroundColor(mInterstitialManager.getInterstitialDisplayProperties().getPubBackGroundOpacity());
        }

        setListeners();
        mWebViewBase.setDialog(this);
    }

    private void setListeners() {
        setOnCancelListener(dialog -> {
            try {
                if (mWebViewBase.isMRAID() && mJsExecutor != null) {
                    mWebViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_DEFAULT);
                    mWebViewBase.detachFromParent();
                }
            }
            catch (Exception e) {
                OXLog.error(TAG, "Interstitial ad closed but post-close events failed: " + Log.getStackTraceString(e));
            }
        });
    }

    @Override
    protected void handleCloseClick() {
        mInterstitialManager.interstitialClosed(mWebViewBase);
    }

    @Override
    protected void handleDialogShow() {
        Views.removeFromParent(mAdViewContainer);
        if (mAdIndicatorView != null) {
            Views.removeFromParent(mAdIndicatorView);
            mAdViewContainer.addView(mAdIndicatorView);
        }
        addContentView(mAdViewContainer,
                       new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                       RelativeLayout.LayoutParams.MATCH_PARENT)
        );
    }

    public void nullifyDialog() {
        cancel();
        cleanup();
    }
}
