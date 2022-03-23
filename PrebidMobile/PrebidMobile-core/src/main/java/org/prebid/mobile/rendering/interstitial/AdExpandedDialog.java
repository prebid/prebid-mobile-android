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

package org.prebid.mobile.rendering.interstitial;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

//Used for MRAID ad expansion
public class AdExpandedDialog extends AdBaseDialog {
    private static final String TAG = AdExpandedDialog.class.getSimpleName();

    public AdExpandedDialog(final Context context, final WebViewBase webViewBaseLocal, InterstitialManager interstitialManager) {
        super(context, webViewBaseLocal, interstitialManager);

        //On MRAID expand we should not remove the old adview such that when the user closes the expanded ad
        //they see the old ad.

        preInit();

        if (mWebViewBase != null && mWebViewBase.isMRAID()) {
            mWebViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_EXPANDED);
        }
        setOnCancelListener(dialog -> {
            try {
                if (mWebViewBase != null) {
                    //detach from closecontainer
                    mWebViewBase.detachFromParent();
                    //add it back to WebView.
                    PrebidWebViewBase defaultContainer = (PrebidWebViewBase) mWebViewBase.getPreloadedListener();

                    //use getPreloadedListener() to get defaultContainer, as mDefaultContainer is not initiated for non-mraid cases(such as interstitials)
                    defaultContainer.addView(mWebViewBase);
                    ////IMP - get the default state
                    defaultContainer.setVisibility(View.VISIBLE);
                    //do not ever call prebidWebView.visible. It makes the default adview on click of expand to be blank.
                    if (context instanceof Activity) {
                        ((Activity) context).setRequestedOrientation(mInitialOrientation);
                    }
                    else {
                        LogUtil.error(TAG, "Context is not Activity, can not set orientation");
                    }

                    mWebViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_DEFAULT);
                }
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Expanded ad closed but post-close events failed: " + Log.getStackTraceString(e));
            }
        });

        mWebViewBase.setDialog(this);
    }

    @Override
    protected void handleCloseClick() {
        mInterstitialManager.interstitialClosed(mWebViewBase);
    }

    @Override
    protected void handleDialogShow() {
        Views.removeFromParent(mAdViewContainer);
        addContentView(mAdViewContainer,
                                   new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                                                                   RelativeLayout.LayoutParams.MATCH_PARENT)
        );
    }
}
