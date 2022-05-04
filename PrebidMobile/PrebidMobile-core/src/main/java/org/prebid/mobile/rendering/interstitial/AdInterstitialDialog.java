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

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.Views;

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
        this.adViewContainer = adViewContainer;


        preInit();
        if (this.interstitialManager.getInterstitialDisplayProperties() != null) {
            this.adViewContainer.setBackgroundColor(this.interstitialManager.getInterstitialDisplayProperties()
                                                                            .getPubBackGroundOpacity());
        }

        setListeners();
        webViewBase.setDialog(this);
    }

    private void setListeners() {
        setOnCancelListener(dialog -> {
            try {
                if (webViewBase.isMRAID() && jsExecutor != null) {
                    webViewBase.getMRAIDInterface().onStateChange(JSInterface.STATE_DEFAULT);
                    webViewBase.detachFromParent();
                }
            }
            catch (Exception e) {
                LogUtil.error(TAG, "Interstitial ad closed but post-close events failed: " + Log.getStackTraceString(e));
            }
        });
    }

    @Override
    protected void handleCloseClick() {
        interstitialManager.interstitialClosed(webViewBase);
    }

    @Override
    protected void handleDialogShow() {
        Views.removeFromParent(adViewContainer);
        addContentView(adViewContainer,
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                )
        );
    }

    public void nullifyDialog() {
        cancel();
        cleanup();
    }
}
