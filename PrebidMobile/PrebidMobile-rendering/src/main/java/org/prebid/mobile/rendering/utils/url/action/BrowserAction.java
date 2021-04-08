package org.prebid.mobile.rendering.utils.url.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;

import androidx.annotation.Nullable;

import org.prebid.mobile.rendering.listeners.OnBrowserActionResultListener;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;

public class BrowserAction implements UrlAction {
    private final int mBroadcastId;
    @Nullable private final OnBrowserActionResultListener mOnBrowserActionResultListener;

    public BrowserAction(int broadcastId, @Nullable OnBrowserActionResultListener onBrowserActionResultListener) {
        mBroadcastId = broadcastId;
        mOnBrowserActionResultListener = onBrowserActionResultListener;
    }

    @Override
    public boolean shouldOverrideUrlLoading(Uri uri) {
        String scheme = uri.getScheme();
        return PrebidRenderingSettings.SCHEME_HTTP.equals(scheme) || PrebidRenderingSettings.SCHEME_HTTPS.equals(scheme);
    }

    @Override
    public void performAction(Context context, UrlHandler urlHandler, Uri uri)
    throws ActionNotResolvedException {
        if (!canHandleLink(context, uri)) {
            throw new ActionNotResolvedException("performAction(): Failed. Url is invalid or there is no activity to handle action.");
        }

        ExternalViewerUtils.startBrowser(context, uri.toString(), mBroadcastId, true, mOnBrowserActionResultListener);
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
