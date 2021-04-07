package com.openx.apollo.views.webview.mraid;

import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;

import androidx.annotation.VisibleForTesting;

import com.openx.apollo.mraid.MraidEnv;
import com.openx.apollo.utils.helpers.Utils;
import com.openx.apollo.utils.logger.OXLog;
import com.openx.apollo.views.webview.AdWebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * Handles injecting the MRAID javascript to the 2nd webview, when encountering mraid.js urls
 */
public class MraidWebViewClient extends AdWebViewClient {
    private static String TAG = MraidWebViewClient.class.getSimpleName();
    private static final String MRAID_JS = "mraid.js";

    private String mMraidInjectionJavascript;

    public MraidWebViewClient(AdAssetsLoadedListener adAssetsLoadedListener, String mraidScript) {
        super(adAssetsLoadedListener);
        mMraidInjectionJavascript = "javascript:" + MraidEnv.getWindowMraidEnv() + mraidScript;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(final WebView view, final String url) {

        if (matchesInjectionUrl(url)) {
            return createMraidInjectionResponse();
        }
        else {
            return super.shouldInterceptRequest(view, url);
        }
    }

    @VisibleForTesting
    boolean matchesInjectionUrl(final String url) {
        final Uri uri = Uri.parse(url.toLowerCase(Locale.US));
        return MRAID_JS.equals(uri.getLastPathSegment());
    }

    private WebResourceResponse createMraidInjectionResponse() {
        if (Utils.isNotBlank(mMraidInjectionJavascript)) {
            mAdAssetsLoadedListener.notifyMraidScriptInjected();
            InputStream data = new ByteArrayInputStream(mMraidInjectionJavascript.getBytes());
            return new WebResourceResponse("text/javascript", "UTF-8", data);
        }
        else {
            OXLog.error(TAG, "Failed to inject mraid.js into twoPart mraid webview");
        }
        return null;
    }
}