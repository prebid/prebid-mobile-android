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

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBanner;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

public class MraidClose {
    private static final String TAG = MraidClose.class.getSimpleName();
    private WebViewBase mWebViewBase;
    private BaseJSInterface mJsi;
    private Context mContext;

    public MraidClose(Context context, BaseJSInterface jsInterface, WebViewBase adBaseView) {
        mContext = context;
        mWebViewBase = adBaseView;
        mJsi = jsInterface;
    }

    public void closeThroughJS() {
        final Context context = mContext;
        if (context == null) {
            LogUtil.error(TAG, "Context is null");
            return;
        }

        Handler uiHandler = new Handler(Looper.getMainLooper());
        uiHandler.post(() -> {
            try {
                String state = mJsi.getMraidVariableContainer().getCurrentState();
                WebViewBase webViewBase = mWebViewBase;

                if (isContainerStateInvalid(state)) {
                    LogUtil.debug(TAG, "closeThroughJS: Skipping. Wrong container state: " + state);
                    return;
                }

                changeState(state);

                if (webViewBase instanceof WebViewBanner && webViewBase.getMRAIDInterface().getDefaultLayoutParams() != null) {
                    webViewBase.setLayoutParams(webViewBase.getMRAIDInterface().getDefaultLayoutParams());
                }
            }
            catch (Exception e) {
                LogUtil.error(TAG, "closeThroughJS failed: " + Log.getStackTraceString(e));
            }
        });
    }

    private void changeState(String state) {
        switch (state) {
            case JSInterface.STATE_EXPANDED:
            case JSInterface.STATE_RESIZED:

                if (mContext instanceof AdBrowserActivity) {
                    ((AdBrowserActivity) mContext).finish();
                }
                else if (mWebViewBase.getDialog() != null) {
                    //Unregister orientation change listener & cancel the dialog on close of an expanded ad.
                    mWebViewBase.getDialog().cleanup();
                    mWebViewBase.setDialog(null);
                }
                else {
                    FrameLayout frameLayout = (FrameLayout) mWebViewBase.getParent();
                    removeParent(frameLayout);

                    addWebViewToContainer(mWebViewBase);

                    //Add expanded view into rootView as well  & remove this null chk once done. Shud work for both expand & resize

                    if (mJsi.getRootView() != null) {
                        mJsi.getRootView().removeView(frameLayout);
                    }
                }
                mJsi.onStateChange(JSInterface.STATE_DEFAULT);
                break;
            case JSInterface.STATE_DEFAULT:
                makeViewInvisible();
                mJsi.onStateChange(JSInterface.STATE_HIDDEN);
                break;
        }
    }

    private void addWebViewToContainer(WebViewBase webViewBase) {
        PrebidWebViewBase defaultContainer = (PrebidWebViewBase) webViewBase.getPreloadedListener();
        if (defaultContainer != null) {
            defaultContainer.addView(webViewBase, 0);
            defaultContainer.setVisibility(View.VISIBLE);
        }
    }

    private void removeParent(FrameLayout frameLayout) {

        if (frameLayout != null) {
            frameLayout.removeView(mWebViewBase);
        }
        else {
            //This should never happen though!
            //Because, if close is called, that would mean, it's parent is always a CloseableLayout(for expanded/resized banners & interstitials )
            //Hence, the previous if block should suffice.
            //But adding it to fix any crash, if above is not the case.
            Views.removeFromParent(mWebViewBase);
        }
    }

    private void makeViewInvisible() {
        Handler handler = new Handler(Looper.getMainLooper());
        handler.post(() -> {
            if (mWebViewBase == null) {
                LogUtil.error(TAG, "makeViewInvisible failed: webViewBase is null");
                return;
            }
            mWebViewBase.setVisibility(View.INVISIBLE);
        });
    }

    private boolean isContainerStateInvalid(String state) {
        return TextUtils.isEmpty(state)
               || state.equals(JSInterface.STATE_LOADING)
               || state.equals(JSInterface.STATE_HIDDEN);
    }
}
