package com.openx.apollo.utils.url.action;

import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;

import com.openx.apollo.utils.helpers.ExternalViewerUtils;
import com.openx.apollo.utils.url.ActionNotResolvedException;
import com.openx.apollo.utils.url.UrlHandler;

import static com.openx.apollo.sdk.ApolloSettings.SCHEME_HTTP;
import static com.openx.apollo.sdk.ApolloSettings.SCHEME_HTTPS;

public class DeepLinkAction implements UrlAction {
    @Override
    public boolean shouldOverrideUrlLoading(Uri uri) {
        final String scheme = uri.getScheme();
        return !TextUtils.isEmpty(scheme)
               && !(SCHEME_HTTP.equals(scheme) || SCHEME_HTTPS.equals(scheme))
               && !DeepLinkPlusAction.SCHEME_DEEPLINK_PLUS.equals(scheme);
    }

    @Override
    public void performAction(Context context, UrlHandler urlHandler, Uri uri)
    throws ActionNotResolvedException {
        ExternalViewerUtils.launchApplicationUrl(context, uri);
    }

    @Override
    public boolean shouldBeTriggeredByUserAction() {
        return true;
    }
}
