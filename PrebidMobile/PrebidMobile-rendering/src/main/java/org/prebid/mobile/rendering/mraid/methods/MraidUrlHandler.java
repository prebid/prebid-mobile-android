package org.prebid.mobile.rendering.mraid.methods;

import android.content.Context;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkAction;
import org.prebid.mobile.rendering.utils.url.action.DeepLinkPlusAction;
import org.prebid.mobile.rendering.utils.url.action.MraidInternalBrowserAction;
import org.prebid.mobile.rendering.utils.url.action.UrlAction;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;

public class MraidUrlHandler {

    public static final String TAG = MraidUrlHandler.class.getSimpleName();

    private final Context mContext;
    private final BaseJSInterface mJsi;

    private boolean mUrlHandleInProgress;

    public MraidUrlHandler(Context context, BaseJSInterface jsInterface) {
        mContext = context;
        mJsi = jsInterface;
    }

    public void open(String url, int broadcastId) {

        if (!mUrlHandleInProgress) {
            mUrlHandleInProgress = true;
            createUrlHandler(broadcastId)
                .handleUrl(mContext,
                           url,
                           null,
                           true); // navigation is performed by user
        }
    }

    public void destroy() {
        if (mJsi != null) {
            mJsi.destroy();
        }
    }

    @VisibleForTesting
    UrlHandler createUrlHandler(int broadcastId) {
        return new UrlHandler.Builder()
            .withDeepLinkPlusAction(new DeepLinkPlusAction())
            .withDeepLinkAction(new DeepLinkAction())
            .withMraidInternalBrowserAction(new MraidInternalBrowserAction(mJsi, broadcastId))
            .withResultListener(new UrlHandler.UrlHandlerResultListener() {
                @Override
                public void onSuccess(String url, UrlAction urlAction) {
                    mUrlHandleInProgress = false;
                }

                @Override
                public void onFailure(String url) {
                    mUrlHandleInProgress = false;
                    OXLog.debug(TAG, "Failed to handleUrl: " + url);
                }
            })
            .build();
    }
}
