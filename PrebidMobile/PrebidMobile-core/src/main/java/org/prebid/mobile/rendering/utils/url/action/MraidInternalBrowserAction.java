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

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;

import java.lang.ref.WeakReference;

public class MraidInternalBrowserAction implements UrlAction {

    private static final String TAG = MraidInternalBrowserAction.class.getSimpleName();

    private final WeakReference<BaseJSInterface> JSInterfaceWeakReference;
    private final int broadcastId;

    public MraidInternalBrowserAction(
            BaseJSInterface jsInterface,
            int broadcastId
    ) {
        JSInterfaceWeakReference = new WeakReference<>(jsInterface);
        this.broadcastId = broadcastId;
    }

    @Override
    public boolean shouldOverrideUrlLoading(Uri uri) {
        String scheme = uri.getScheme();
        return PrebidMobile.SCHEME_HTTP.equals(scheme) || PrebidMobile.SCHEME_HTTPS.equals(scheme);
    }

    @Override
    public void performAction(Context context, UrlHandler urlHandler, Uri uri)
    throws ActionNotResolvedException {
        BaseJSInterface baseJSInterface = JSInterfaceWeakReference.get();
        if (baseJSInterface == null) {
            throw new ActionNotResolvedException("Action can't be handled. BaseJSInterface is null");
        }

        handleInternalBrowserAction(context, baseJSInterface, uri.toString());
    }

    @Override
    public boolean shouldBeTriggeredByUserAction() {
        return true;
    }

    @VisibleForTesting
    void handleInternalBrowserAction(final Context context, BaseJSInterface baseJSInterface, String url) {
        baseJSInterface.followToOriginalUrl(url, new RedirectUrlListener() {
            @Override
            public void onSuccess(String url, String contentType) {
                if (Utils.isMraidActionUrl(url) && context != null) {
                    LogUtil.debug(TAG, "Redirection succeeded");

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    try {
                        ExternalViewerUtils.startActivity(context.getApplicationContext(), intent);
                    } catch (ActivityNotFoundException e) {
                        LogUtil.error(TAG, "Unable to open url " + url + ". Activity was not found");
                    }
                } else if (url != null && (url.startsWith(PrebidMobile.SCHEME_HTTP) || url.startsWith(PrebidMobile.SCHEME_HTTPS))) {
                    if (Utils.isVideoContent(contentType)) {
                        baseJSInterface.playVideo(url);
                    } else {
                        launchBrowserActivity(context, baseJSInterface, url);
                    }
                }
            }

            @Override
            public void onFailed() {
                // Nothing to do
                LogUtil.debug(TAG, "Open: redirection failed");
            }
        });
    }

    @VisibleForTesting
    void launchBrowserActivity(final Context context, BaseJSInterface jsInterface, String url) {
        final MraidVariableContainer mraidVariableContainer = jsInterface.getMraidVariableContainer();

        if (url != null) {
            mraidVariableContainer.setUrlForLaunching(url);
        }

        ExternalViewerUtils.startBrowser(context, mraidVariableContainer.getUrlForLaunching(), broadcastId, true, null);
    }
}
