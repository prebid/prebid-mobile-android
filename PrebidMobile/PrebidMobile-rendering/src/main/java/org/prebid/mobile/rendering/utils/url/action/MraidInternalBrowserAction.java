package org.prebid.mobile.rendering.utils.url.action;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.VisibleForTesting;

import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.ExternalViewerUtils;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;
import org.prebid.mobile.rendering.utils.url.ActionNotResolvedException;
import org.prebid.mobile.rendering.utils.url.UrlHandler;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;

import java.lang.ref.WeakReference;

public class MraidInternalBrowserAction implements UrlAction {
    private static final String TAG = MraidInternalBrowserAction.class.getSimpleName();

    private final WeakReference<BaseJSInterface> mJSInterfaceWeakReference;
    private final int mBroadcastId;

    public MraidInternalBrowserAction(BaseJSInterface jsInterface, int broadcastId) {
        mJSInterfaceWeakReference = new WeakReference<>(jsInterface);
        mBroadcastId = broadcastId;
    }

    @Override
    public boolean shouldOverrideUrlLoading(Uri uri) {
        String scheme = uri.getScheme();
        return PrebidRenderingSettings.SCHEME_HTTP.equals(scheme) || PrebidRenderingSettings.SCHEME_HTTPS.equals(scheme);
    }

    @Override
    public void performAction(Context context, UrlHandler urlHandler, Uri uri)
    throws ActionNotResolvedException {
        BaseJSInterface baseJSInterface = mJSInterfaceWeakReference.get();
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
                    OXLog.debug(TAG, "Redirection succeeded");

                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    try {
                        context.getApplicationContext().startActivity(intent);
                    }
                    catch (ActivityNotFoundException e) {
                        OXLog.error(TAG, "Unable to open url " + url + ". Activity was not found");
                    }
                }
                else if (url != null && (url.startsWith(PrebidRenderingSettings.SCHEME_HTTP) || url.startsWith(PrebidRenderingSettings.SCHEME_HTTPS))) {
                    if (Utils.isVideoContent(contentType)) {
                        baseJSInterface.playVideo(url);
                    }
                    else {
                        launchBrowserActivity(context, baseJSInterface, url);
                    }
                }
            }

            @Override
            public void onFailed() {
                // Nothing to do
                OXLog.debug(TAG, "Open: redirection failed");
            }
        });
    }

    @VisibleForTesting
    void launchBrowserActivity(final Context context, BaseJSInterface jsInterface, String url) {
        final MraidVariableContainer mraidVariableContainer = jsInterface.getMraidVariableContainer();

        if (url != null) {
            mraidVariableContainer.setUrlForLaunching(url);
        }

        ExternalViewerUtils.startBrowser(context, mraidVariableContainer.getUrlForLaunching(),  mBroadcastId,
                                         true, null);
    }
}
