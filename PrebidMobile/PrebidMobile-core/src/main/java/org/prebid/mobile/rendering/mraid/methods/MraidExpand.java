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
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.interstitial.AdExpandedDialog;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

public class MraidExpand {

    public static final String TAG = MraidExpand.class.getSimpleName();
    private WebViewBase webViewBanner;

    private BaseJSInterface jsi;
    private InterstitialManager interstitialManager;
    private Context context;
    private AdBaseDialog expandedDialog;

    private boolean mraidExpanded;

    public MraidExpand(
            Context context,
            WebViewBase adBaseView,
            InterstitialManager interstitialManager
    ) {
        this.context = context;
        webViewBanner = adBaseView;
        jsi = adBaseView.getMRAIDInterface();
        this.interstitialManager = interstitialManager;
    }

    public void expand(final String url, final CompletedCallBack completedCallBack) {

        jsi.followToOriginalUrl(url, new RedirectUrlListener() {
            @Override
            public void onSuccess(
                    final String url,
                    String contentType
            ) {

                if (Utils.isVideoContent(contentType)) {
                    jsi.playVideo(url);
                } else {
                    performExpand(url, completedCallBack);
                }
            }

            @Override
            public void onFailed() {
                LogUtil.debug(TAG, "Expand failed");
                // Nothing to do
            }
        });
    }

    public void setDisplayView(View displayView) {
        if (expandedDialog != null) {
            expandedDialog.setDisplayView(displayView);
        }
    }

    public AdBaseDialog getInterstitialViewController() {
        return expandedDialog;
    }

    public void nullifyDialog() {
        if (expandedDialog != null) {
            expandedDialog.cancel();
            expandedDialog.cleanup();
            expandedDialog = null;
        }
    }

    public void destroy() {
        if (jsi != null) {
            Views.removeFromParent(jsi.getDefaultAdContainer());
        }
        if (expandedDialog != null) {
            expandedDialog.dismiss();
        }
        webViewBanner = null;
    }

    /**
     * Return true if MRAID expand is enabled, otherwise - false.
     * This flag is used to enable/disable MRAID expand property.
     */
    public boolean isMraidExpanded() {
        return mraidExpanded;
    }

    /**
     * Set MRAID expand flag to true if MRAID expand is enabled, otherwise - false.
     * This flag is used to enable/disable MRAID expand property.
     */
    public void setMraidExpanded(boolean mraidExpanded) {
        this.mraidExpanded = mraidExpanded;
    }

    private void performExpand(String url, CompletedCallBack completedCallBack) {
        final Context context = this.context;
        if (context == null) {
            LogUtil.error(TAG, "Context is null");
            return;
        }

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() -> {
            try {
                final MraidVariableContainer mraidVariableContainer = jsi.getMraidVariableContainer();

                String state = mraidVariableContainer.getCurrentState();

                if (isContainerStateInvalid(state)) {
                    LogUtil.debug(TAG, "handleExpand: Skipping. Wrong container state: " + state);
                    return;
                }

                jsi.setDefaultLayoutParams(webViewBanner.getLayoutParams());

                if (url != null) {
                    mraidVariableContainer.setUrlForLaunching(url);
                }

                showExpandDialog(context, completedCallBack);
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Expand failed: " + Log.getStackTraceString(e));
            }
        });
    }

    private boolean isContainerStateInvalid(String state) {
        return TextUtils.isEmpty(state)
               || state.equals(JSInterface.STATE_LOADING)
               || state.equals(JSInterface.STATE_HIDDEN)
               || state.equals(JSInterface.STATE_EXPANDED);
    }

    @VisibleForTesting
    void showExpandDialog(Context context, CompletedCallBack completedCallBack) {
        if (!(context instanceof Activity) || ((Activity) context).isFinishing()) {
            LogUtil.error(TAG, "Context is not activity or activity is finishing, can not show expand dialog");
            return;
        }

        expandedDialog = new AdExpandedDialog(context, webViewBanner, interstitialManager);//HAS be on UI thread
        //Workaround fix for a random crash on multiple clicks on 2part expand and press back key
        expandedDialog.show();
        if (completedCallBack != null) {
            completedCallBack.expandDialogShown();
        }
    }
}
