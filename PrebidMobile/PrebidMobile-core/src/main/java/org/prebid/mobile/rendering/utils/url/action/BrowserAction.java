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

package org.prebid.mobile.rendering.utils.url.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;
import androidx.annotation.Nullable;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.listeners.OnBrowserActionResultListener;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;

public class BrowserAction implements UrlAction {

    private final int broadcastId;
    @Nullable private final OnBrowserActionResultListener onBrowserActionResultListener;

    public BrowserAction(
            int broadcastId,
            @Nullable OnBrowserActionResultListener onBrowserActionResultListener
    ) {
        this.broadcastId = broadcastId;
        this.onBrowserActionResultListener = onBrowserActionResultListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(Uri uri) {
        String scheme = uri.getScheme();
        return PrebidMobile.SCHEME_HTTP.equals(scheme) || PrebidMobile.SCHEME_HTTPS.equals(scheme);
    }

    @Override
    public void performAction(Context context, UrlHandler urlHandler, Uri uri)
    throws ActionNotResolvedException {
        if (!canHandleLink(context, uri)) {
            throw new ActionNotResolvedException("performAction(): Failed. Url is invalid or there is no activity to handle action.");
        }

        ExternalViewerUtils.startBrowser(context, uri.toString(), broadcastId, true, onBrowserActionResultListener);
    }

    private boolean canHandleLink(Context context, Uri uri) {
        Intent externalBrowserIntent = new Intent(Intent.ACTION_VIEW, uri);
        Intent internalBrowserIntent = new Intent(context, AdBrowserActivity.class);

        return URLUtil.isValidUrl(uri.toString())
               && (ExternalViewerUtils.isActivityCallable(context, externalBrowserIntent) || ExternalViewerUtils.isActivityCallable(context, internalBrowserIntent));
    }

    @Override
    public boolean shouldBeTriggeredByUserAction() {
        return true;
    }
}
