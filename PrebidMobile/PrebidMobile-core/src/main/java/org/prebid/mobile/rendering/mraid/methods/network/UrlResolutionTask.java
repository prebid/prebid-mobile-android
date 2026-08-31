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

package org.prebid.mobile.rendering.mraid.methods.network;

import android.os.AsyncTask;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;

import java.io.IOException;
import java.io.InputStream;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLHandshakeException;

@VisibleForTesting
public class UrlResolutionTask extends AsyncTask<String, Void, String> {
    private static final String TAG = UrlResolutionTask.class.getSimpleName();

    @NonNull private final UrlResolutionListener listener;

    public UrlResolutionTask(@NonNull UrlResolutionListener listener) {
        this.listener = listener;
    }

    @Nullable
    @Override
    protected String doInBackground(@Nullable String... urls) {
        if (urls == null || urls.length == 0) {
            return null;
        }

        String previousUrl = null;
        // One budget for the whole chain rather than per hop, so a slow host cannot
        // multiply the delay by MAX_REDIRECTS before the browser is handed anything.
        final long deadlineNanos = System.nanoTime()
                + TimeUnit.MILLISECONDS.toNanos(resolutionBudgetMillis());
        try {
            String locationUrl = urls[0];

            int redirectCount = 0;
            while (locationUrl != null && redirectCount < GetOriginalUrlTask.MAX_REDIRECTS) {
                // if location url is not http(s), assume it's an Android deep link
                // this scheme will fail URL validation so we have to check early
                if (!locationUrl.startsWith(PrebidMobile.SCHEME_HTTP)) {
                    return locationUrl;
                }

                long remainingMillis = TimeUnit.NANOSECONDS.toMillis(deadlineNanos - System.nanoTime());
                if (remainingMillis <= 0) {
                    LogUtil.debug(TAG, "Url resolution budget spent; opening " + locationUrl);
                    return locationUrl;
                }

                previousUrl = locationUrl;
                locationUrl = getRedirectLocation(locationUrl, (int) remainingMillis);
                redirectCount++;
            }
        }
        catch (UnknownHostException | ConnectException | SSLHandshakeException e) {
            // The destination is known to be unreachable: DNS did not resolve, the
            // connection was refused, or TLS could not be established. Opening the
            // browser there would fail too, and it would record a click that never
            // landed, so this stays cancelled.
            LogUtil.debug(TAG, "Url resolution failed for " + previousUrl + ": " + e.getMessage());
            return null;
        }
        catch (IOException e) {
            // Everything else, including a stalled host and an abrupt disconnect. The
            // JDK does not classify these consistently -- whether a reset surfaces as
            // SocketException or as SocketTimeoutException depends on OS-level timing
            // of the RST against the read -- so reachability must not be inferred from
            // the exception type here. The URL in hand is one we had already decided to
            // follow, so hand it to the browser and let it finish the chain.
            LogUtil.debug(TAG, "Url resolution stopped at " + previousUrl + ": " + e.getMessage());
            return previousUrl;
        }
        catch (URISyntaxException | NullPointerException e) {
            // A malformed redirect target stays cancelled rather than opening an
            // intermediary URL, as resolveRedirectLocation intends.
            return null;
        }

        return previousUrl;
    }

    /** Read timeout for a single hop, mirroring {@link BaseNetworkTask}. */
    private static int readTimeoutMillis() {
        return PrebidMobile.getTimeoutModified()
                ? PrebidMobile.getTimeoutMillis()
                : BaseNetworkTask.SOCKET_TIMEOUT;
    }

    /** Total time allowed to resolve the chain: one connect plus one read. */
    private static int resolutionBudgetMillis() {
        return PrebidMobile.getTimeoutMillis() + readTimeoutMillis();
    }

    @Nullable
    private String getRedirectLocation(@NonNull final String urlString, final int budgetMillis)
    throws IOException, URISyntaxException {
        final URL url = new URL(urlString);

        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setInstanceFollowRedirects(false);
            // HttpURLConnection defaults to no timeout at all. Capping each hop at
            // what is left of the budget also bounds the stream close below, which
            // would otherwise wait a further read timeout after a stalled hop.
            httpUrlConnection.setConnectTimeout(Math.min(PrebidMobile.getTimeoutMillis(), budgetMillis));
            httpUrlConnection.setReadTimeout(Math.min(readTimeoutMillis(), budgetMillis));

            return resolveRedirectLocation(urlString, httpUrlConnection);
        }
        finally {
            if (httpUrlConnection != null) {
                try {
                    final InputStream is = httpUrlConnection.getInputStream();
                    if (is != null) {
                        is.close();
                    }
                }
                catch (IOException e) {
                    LogUtil.error(TAG, "IOException when closing httpUrlConnection. Ignoring.");
                }
                httpUrlConnection.disconnect();
            }
        }
    }

    @VisibleForTesting
    @Nullable
    static String resolveRedirectLocation(@NonNull final String baseUrl,
                                          @NonNull final HttpURLConnection httpUrlConnection)
    throws IOException, URISyntaxException {
        final URI baseUri = new URI(baseUrl);
        final int responseCode = httpUrlConnection.getResponseCode();
        final String redirectUrl = httpUrlConnection.getHeaderField("location");
        String result = null;

        if (responseCode >= 300 && responseCode < 400) {
            try {
                // If redirectUrl is a relative path, then resolve() will correctly complete the path;
                // otherwise, resolve() will return the redirectUrl
                result = baseUri.resolve(redirectUrl).toString();
            }
            catch (IllegalArgumentException e) {
                // Ensure the request is cancelled instead of resolving an intermediary URL
                LogUtil.error(TAG, "Invalid URL redirection. baseUrl=" + baseUrl + "\n redirectUrl=" + redirectUrl);
                throw new URISyntaxException(redirectUrl, "Unable to parse invalid URL");
            }
            catch (NullPointerException e) {
                LogUtil.error(TAG, "Invalid URL redirection. baseUrl=" + baseUrl + "\n redirectUrl=" + redirectUrl);
                throw e;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(@Nullable final String resolvedUrl) {
        super.onPostExecute(resolvedUrl);

        if (isCancelled() || resolvedUrl == null) {
            onCancelled();
        }
        else {
            listener.onSuccess(resolvedUrl);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        listener.onFailure("Task for resolving url was cancelled", null);
    }

    public interface UrlResolutionListener {
        void onSuccess(@NonNull final String resolvedUrl);

        void onFailure(@NonNull final String message, @Nullable final Throwable throwable);
    }
}