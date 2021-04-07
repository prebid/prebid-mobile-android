package com.openx.apollo.mraid.methods;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.openx.apollo.interstitial.AdBaseDialog;
import com.openx.apollo.interstitial.AdExpandedDialog;
import com.openx.apollo.models.internal.MraidVariableContainer;
import com.openx.apollo.mraid.methods.network.RedirectUrlListener;
import com.openx.apollo.utils.helpers.Utils;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;
import com.openx.apollo.views.webview.mraid.Views;

public class MraidExpand {
    public static final String TAG = MraidExpand.class.getSimpleName();
    private WebViewBase mWebViewBanner;

    private BaseJSInterface mJsi;
    private InterstitialManager mInterstitialManager;
    private Context mContext;
    private AdBaseDialog mExpandedDialog;

    private boolean mMraidExpanded;

    public MraidExpand(Context context, WebViewBase adBaseView, InterstitialManager interstitialManager) {
        mContext = context;
        mWebViewBanner = adBaseView;
        mJsi = adBaseView.getMRAIDInterface();
        mInterstitialManager = interstitialManager;
    }

    public void expand(final String url, final CompletedCallBack completedCallBack) {

        mJsi.followToOriginalUrl(url, new RedirectUrlListener() {
            @Override
            public void onSuccess(final String url, String contentType) {

                if (Utils.isVideoContent(contentType)) {
                    mJsi.playVideo(url);
                }
                else {
                    performExpand(url, completedCallBack);
                }
            }

            @Override
            public void onFailed() {
                OXLog.debug(TAG, "Expand failed");
                // Nothing to do
            }
        });
    }

    public void setDisplayView(View displayView) {
        if (mExpandedDialog != null) {
            mExpandedDialog.setDisplayView(displayView);
        }
    }

    public AdBaseDialog getInterstitialViewController() {
        return mExpandedDialog;
    }

    public void nullifyDialog() {
        if (mExpandedDialog != null) {
            mExpandedDialog.cancel();
            mExpandedDialog.cleanup();
            mExpandedDialog = null;
        }
    }

    public void destroy() {
        if (mJsi != null) {
            Views.removeFromParent(mJsi.getDefaultAdContainer());
        }
        if (mExpandedDialog != null) {
            mExpandedDialog.dismiss();
        }
    }

    /**
     * Return true if MRAID expand is enabled, otherwise - false.
     * This flag is used to enable/disable MRAID expand property.
     */
    public boolean isMraidExpanded() {
        return mMraidExpanded;
    }

    /**
     * Set MRAID expand flag to true if MRAID expand is enabled, otherwise - false.
     * This flag is used to enable/disable MRAID expand property.
     */
    public void setMraidExpanded(boolean mraidExpanded) {
        this.mMraidExpanded = mraidExpanded;
    }

    private void performExpand(String url, CompletedCallBack completedCallBack) {
        final Context context = mContext;
        if (context == null) {
            OXLog.error(TAG, "Context is null");
            return;
        }

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() -> {
            try {
                final MraidVariableContainer mraidVariableContainer = mJsi.getMraidVariableContainer();

                String state = mraidVariableContainer.getCurrentState();

                if (isContainerStateInvalid(state)) {
                    OXLog.debug(TAG, "handleExpand: Skipping. Wrong container state: " + state);
                    return;
                }

                mJsi.setDefaultLayoutParams(mWebViewBanner.getLayoutParams());

                if (url != null) {
                    mraidVariableContainer.setUrlForLaunching(url);
                }

                showExpandDialog(completedCallBack, context);
            }
            catch (Exception e) {
                OXLog.error(TAG, "Expand failed: " + Log.getStackTraceString(e));
            }
        });
    }

    private void showExpandDialog(CompletedCallBack completedCallBack, Context context) {
        if (!(context instanceof Activity) || ((Activity) context).isFinishing()) {
            OXLog.error(TAG, "Context is not activity or activity is finishing, can not show expand dialog");
            return;
        }

        mExpandedDialog = new AdExpandedDialog(context, mWebViewBanner, mInterstitialManager);//HAS be on UI thread
        ((AdExpandedDialog) mExpandedDialog).showAdIndicator();
        //Workaround fix for a random crash on multiple clicks on 2part expand and press back key
        mExpandedDialog.show();
        if (completedCallBack != null) {
            completedCallBack.expandDialogShown();
        }
    }

    private boolean isContainerStateInvalid(String state) {
        return TextUtils.isEmpty(state)
               || state.equals(JSInterface.STATE_LOADING)
               || state.equals(JSInterface.STATE_HIDDEN)
               || state.equals(JSInterface.STATE_EXPANDED);
    }
}
