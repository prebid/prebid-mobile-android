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
import android.net.Uri;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.networking.tracking.TrackingManager;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;

import java.util.List;

public class DeepLinkPlusAction implements UrlAction {
    private static final String TAG = DeepLinkPlusAction.class.getSimpleName();

    public static final String SCHEME_DEEPLINK_PLUS = "deeplink+";
    private static final String HOST_NAVIGATE = "navigate";

    static final String QUERY_PRIMARY_URL = "primaryUrl";
    static final String QUERY_PRIMARY_TRACKING_URL = "primaryTrackingUrl";
    static final String QUERY_FALLBACK_URL = "fallbackUrl";
    static final String QUERY_FALLBACK_TRACKING_URL = "fallbackTrackingUrl";

    @Override
    public boolean shouldOverrideUrlLoading(Uri uri) {
        return SCHEME_DEEPLINK_PLUS.equalsIgnoreCase(uri.getScheme());
    }

    @Override
    public void performAction(Context context, UrlHandler urlHandler, Uri uri)
    throws ActionNotResolvedException {
        if (!HOST_NAVIGATE.equalsIgnoreCase(uri.getHost())) {
            throw new ActionNotResolvedException("Deeplink+ URL did not have 'navigate' as the host.");
        }

        final String primaryUrl;
        final List<String> primaryTrackingUrls;
        final String fallbackUrl;
        final List<String> fallbackTrackingUrls;
        try {
            primaryUrl = uri.getQueryParameter(QUERY_PRIMARY_URL);
            primaryTrackingUrls = uri.getQueryParameters(QUERY_PRIMARY_TRACKING_URL);
            fallbackUrl = uri.getQueryParameter(QUERY_FALLBACK_URL);
            fallbackTrackingUrls = uri.getQueryParameters(QUERY_FALLBACK_TRACKING_URL);
        }
        catch (UnsupportedOperationException e) {
            // If the URL is not hierarchical, getQueryParameter[s] will throw
            // UnsupportedOperationException (see https://developer.android.com/reference/android/net/Uri.html#getQueryParameter(java.lang.String)
            throw new ActionNotResolvedException("Deeplink+ URL was not a hierarchical URI.");
        }

        if (primaryUrl == null) {
            throw new ActionNotResolvedException("Deeplink+ did not have 'primaryUrl' query param.");
        }

        final Uri primaryUri = Uri.parse(primaryUrl);
        if (shouldOverrideUrlLoading(primaryUri)) {
            // Nested Deeplink+ URLs are not allowed
            throw new ActionNotResolvedException("Deeplink+ had another Deeplink+ as the 'primaryUrl'.");
        }

        // 2. Attempt to handle the primary URL
        try {
            ExternalViewerUtils.launchApplicationUrl(context, primaryUri);
            TrackingManager.getInstance().fireEventTrackingURLs(primaryTrackingUrls);
            return;
        }
        catch (ActionNotResolvedException e) {
            LogUtil.debug(TAG, "performAction(): Primary URL failed. Attempting to process fallback URL");
        }

        // 3. Attempt to handle the fallback URL
        if (fallbackUrl == null) {
            throw new ActionNotResolvedException("Unable to handle 'primaryUrl' for Deeplink+ and 'fallbackUrl' was missing.");
        }

        if (shouldOverrideUrlLoading(Uri.parse(fallbackUrl))) {
            // Nested Deeplink+ URLs are not allowed
            throw new ActionNotResolvedException("Deeplink+ URL had another Deeplink+ URL as the 'fallbackUrl'.");
        }

        // UrlAction.handleUrl already verified this comes from a user interaction
        urlHandler.handleUrl(context, fallbackUrl, fallbackTrackingUrls, true);
    }

    @Override
    public boolean shouldBeTriggeredByUserAction() {
        return true;
    }
}
