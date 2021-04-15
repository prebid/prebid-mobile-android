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

package org.prebid.mobile.rendering.mraid.methods;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.interstitial.AdExpandedDialog;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

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
