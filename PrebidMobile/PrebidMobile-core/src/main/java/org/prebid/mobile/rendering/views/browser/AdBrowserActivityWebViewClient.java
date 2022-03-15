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

package org.prebid.mobile.rendering.views.browser;

import android.webkit.WebView;
import android.webkit.WebViewClient;
import org.prebid.mobile.rendering.utils.logger.LogUtil;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkAction;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkPlusAction;
import org.prebid.mobile.rendering.utils.url.action.UrlAction;

class AdBrowserActivityWebViewClient extends WebViewClient {
    private static final String TAG = AdBrowserActivityWebViewClient.class.getSimpleName();

    private AdBrowserWebViewClientListener mAdBrowserWebViewClientListener;

    private boolean mUrlHandleInProgress;

    AdBrowserActivityWebViewClient(AdBrowserWebViewClientListener adBrowserWebViewClientListener) {
        mAdBrowserWebViewClientListener = adBrowserWebViewClientListener;
    }

    @Override
    public void onPageFinished(WebView view, String url) {
        if (mAdBrowserWebViewClientListener != null) {
            mAdBrowserWebViewClientListener.onPageFinished();
        }
        super.onPageFinished(view, url);
    }

    @Override
    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        UrlHandler urlHandler = createUrlHandler();
        if (mUrlHandleInProgress) {
            return false;
        }
        else {
            mUrlHandleInProgress = true;
            return urlHandler
                .handleResolvedUrl(view.getContext(),
                                   url,
                                   null,
                                   true); // navigation is performed by user
        }
    }

    private UrlHandler createUrlHandler() {
        return new UrlHandler.Builder()
            .withDeepLinkPlusAction(new DeepLinkPlusAction())
            .withDeepLinkAction(new DeepLinkAction())
            .withResultListener(new UrlHandler.UrlHandlerResultListener() {
                @Override
                public void onSuccess(String url, UrlAction urlAction) {
                    mUrlHandleInProgress = false;
                    if (mAdBrowserWebViewClientListener != null) {
                        mAdBrowserWebViewClientListener.onUrlHandleSuccess();
                    }
                }

                @Override
                public void onFailure(String url) {
                    LogUtil.debug(TAG, "Failed to handleUrl: " + url);
                    mUrlHandleInProgress = false;
                }
            })
            .build();
    }

    public interface AdBrowserWebViewClientListener {
        void onPageFinished();

        void onUrlHandleSuccess();
    }
}
