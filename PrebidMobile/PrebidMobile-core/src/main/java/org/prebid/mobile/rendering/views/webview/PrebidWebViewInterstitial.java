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

package org.prebid.mobile.rendering.views.webview;

import android.content.Context;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;

public class PrebidWebViewInterstitial extends PrebidWebViewBase
    implements PreloadManager.PreloadedListener, MraidEventsManager.MraidListener {

    private final String TAG = PrebidWebViewInterstitial.class.getSimpleName();

    public PrebidWebViewInterstitial(Context context, InterstitialManager interstitialManager) {
        super(context, interstitialManager);
    }

    @Override
    public void loadHTML(String html, int width, int height) {
        LayoutParams layoutParams = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

        setLayoutParams(layoutParams);
        this.width = width;
        this.height = height;
        //A null context can crash with an exception in webView creation through WebViewBanner. Catch it
        webView = new WebViewInterstitial(context, html, width, height, this, this);
        webView.setJSName("WebViewInterstitial");
        webView.initContainsIFrame(creative.getCreativeModel().getHtml());
        webView.setTargetUrl(creative.getCreativeModel().getTargetUrl());
        webView.loadAd();
    }

    @Override
    public void preloaded(WebViewBase adBaseView) {
        if (adBaseView == null) {
            //This should never happen.
            LogUtil.error(TAG, "Failed to preload an interstitial. Webview is null.");

            if (webViewDelegate != null) {
                webViewDelegate.webViewFailedToLoad(new AdException(
                        AdException.INTERNAL_ERROR,
                        "Preloaded adview is null!"
                ));
            }
            return;
        }
        currentWebViewBase = adBaseView;

        if (webViewDelegate != null) {
            webViewDelegate.webViewReadyToDisplay();
        }
    }
}