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

package org.prebid.mobile.rendering.views.webview.mraid;

import android.net.Uri;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.rendering.mraid.MraidEnv;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.views.webview.AdWebViewClient;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.Locale;

/**
 * Handles injecting the MRAID javascript to the 2nd webview, when encountering mraid.js urls
 */
public class MraidWebViewClient extends AdWebViewClient {
    private static String TAG = MraidWebViewClient.class.getSimpleName();
    private static final String MRAID_JS = "mraid.js";

    private String mraidInjectionJavascript;

    public MraidWebViewClient(AdAssetsLoadedListener adAssetsLoadedListener, String mraidScript) {
        super(adAssetsLoadedListener);
        mraidInjectionJavascript = "javascript:" + MraidEnv.getWindowMraidEnv() + mraidScript;
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
        if (Utils.isNotBlank(mraidInjectionJavascript)) {
            adAssetsLoadedListener.notifyMraidScriptInjected();
            InputStream data = new ByteArrayInputStream(mraidInjectionJavascript.getBytes());
            return new WebResourceResponse("text/javascript", "UTF-8", data);
        } else {
            LogUtil.error(TAG, "Failed to inject mraid.js into twoPart mraid webview");
        }
        return null;
    }
}