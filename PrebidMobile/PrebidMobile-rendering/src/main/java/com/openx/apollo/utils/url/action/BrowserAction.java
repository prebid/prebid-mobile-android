package com.openx.apollo.utils.url.action;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.webkit.URLUtil;

import androidx.annotation.Nullable;

import com.openx.apollo.listeners.OnBrowserActionResultListener;
import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.helpers.ExternalViewerUtils;
import com.openx.apollo.utils.url.ActionNotResolvedException;
import com.openx.apollo.utils.url.UrlHandler;
import com.openx.apollo.views.browser.AdBrowserActivity;

import static com.openx.apollo.utils.helpers.ExternalViewerUtils.isActivityCallable;

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
        return ApolloSettings.SCHEME_HTTP.equals(scheme) || ApolloSettings.SCHEME_HTTPS.equals(scheme);
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
               && (isActivityCallable(context, externalBrowserIntent) || isActivityCallable(context, internalBrowserIntent));
    }

    @Override
    public boolean shouldBeTriggeredByUserAction() {
        return true;
    }
}
