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
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkAction;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkPlusAction;
import org.prebid.mobile.rendering.utils.url.action.MraidInternalBrowserAction;
import org.prebid.mobile.rendering.utils.url.action.UrlAction;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;

public class MraidUrlHandler {

    public static final String TAG = MraidUrlHandler.class.getSimpleName();

    private final Context context;
    private final BaseJSInterface jsi;

    private boolean urlHandleInProgress;

    public MraidUrlHandler(
            Context context,
            BaseJSInterface jsInterface
    ) {
        this.context = context;
        jsi = jsInterface;
    }

    public void open(
            String url,
            int broadcastId
    ) {

        if (!urlHandleInProgress) {
            urlHandleInProgress = true;
            createUrlHandler(broadcastId).handleUrl(context, url, null, true); // navigation is performed by user
        }
    }

    public void destroy() {
        if (jsi != null) {
            jsi.destroy();
        }
    }

    @VisibleForTesting
    UrlHandler createUrlHandler(int broadcastId) {
        return new UrlHandler.Builder().withDeepLinkPlusAction(new DeepLinkPlusAction())
                                       .withDeepLinkAction(new DeepLinkAction())
                                       .withMraidInternalBrowserAction(new MraidInternalBrowserAction(jsi, broadcastId))
            .withResultListener(new UrlHandler.UrlHandlerResultListener() {
                @Override
                public void onSuccess(String url, UrlAction urlAction) {
                    urlHandleInProgress = false;
                }

                @Override
                public void onFailure(String url) {
                    urlHandleInProgress = false;
                    LogUtil.debug(TAG, "Failed to handleUrl: " + url);
                }
            })
            .build();
    }
}
