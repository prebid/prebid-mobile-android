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
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;

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

        // Read the timeout configuration once. PrebidMobile's statics are settable from
        // any thread and this runs on THREAD_POOL_EXECUTOR, so re-reading them per hop
        // against a deadline frozen here would let the two disagree mid-chain. Clamping
        // at zero also keeps a negative value away from setConnectTimeout, which would
        // throw.
        final int configuredConnectMillis = Math.max(0, PrebidMobile.getTimeoutMillis());
        final int configuredReadMillis = Math.max(0, BaseNetworkTask.readTimeoutMillis());
        // Summed as a long: both are ints, and their sum overflows for a very large
        // configured timeout, which would put the deadline in the past and stop the
        // chain being resolved at all.
        final long budgetMillis = Math.max(1L, (long) configuredConnectMillis + configuredReadMillis);

        String previousUrl = null;
        // One budget for the whole chain rather than per hop, so a slow host cannot
        // multiply the delay by MAX_REDIRECTS before the browser is handed anything.
        final long deadlineNanos = System.nanoTime() + TimeUnit.MILLISECONDS.toNanos(budgetMillis);
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
                locationUrl = getRedirectLocation(
                        locationUrl, remainingMillis, configuredConnectMillis, configuredReadMillis);
                redirectCount++;
            }
        }
        catch (MalformedURLException e) {
            // A target java.net.URI accepts but java.net.URL cannot parse -- a
            // non-numeric port, say. URI treats that authority as registry-based and
            // resolves it happily, so the failure only surfaces on the next hop, and
            // as an IOException rather than a URISyntaxException. It is a malformed
            // target either way and must not reach an Intent.
            LogUtil.error(TAG, "Malformed redirect target: " + previousUrl);
            return null;
        }
        catch (UnknownHostException | ConnectException e) {
            // DNS did not resolve, or the connection was refused. Neither depends on
            // which HTTP stack asks, so the browser would fail here too; cancelling
            // avoids recording a click that cannot land. This applies at any point in
            // the chain: a hop confirmed unreachable is not worth handing over, even
            // when an earlier hop succeeded.
            //
            // Deliberately not SSLHandshakeException. This probes with
            // HttpURLConnection, which does not chase Authority Information Access for
            // a server missing an intermediate certificate, while Chrome and WebView
            // do -- so a handshake failure here does not predict one in the browser,
            // and cancelling on it would drop clicks to pages a device renders fine.
            LogUtil.debug(TAG, "Destination unreachable at " + previousUrl + ": " + e.getMessage());
            return null;
        }
        catch (IOException e) {
            // Everything else: a stalled host, an abrupt disconnect, a TLS failure this
            // stack is stricter about than a browser. The JDK does not classify these
            // consistently -- whether a reset surfaces as SocketException or as
            // SocketTimeoutException depends on OS-level timing of the RST against the
            // read -- so reachability must not be inferred from the exception type. The
            // URL in hand is one we had already decided to follow, so hand it to the
            // browser and let it finish the chain.
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

    /**
     * A timeout bounded by what is left of the chain budget. A configured value of
     * zero means "unset" here rather than URLConnection's "wait forever", which is the
     * hang this class exists to prevent.
     */
    private static int boundedTimeout(final int configuredMillis, final long capMillis) {
        final long cap = Math.min(Integer.MAX_VALUE, Math.max(1L, capMillis));
        return (int) (configuredMillis > 0 ? Math.min(configuredMillis, cap) : cap);
    }

    @Nullable
    private String getRedirectLocation(
            @NonNull final String urlString,
            final long remainingMillis,
            final int configuredConnectMillis,
            final int configuredReadMillis
    ) throws IOException, URISyntaxException {
        final URL url = new URL(urlString);

        // Split what is left of the chain budget across this hop's two blocking phases.
        // Capping each at the full remainder independently would let one hop run for
        // roughly twice it, which is what the whole-chain bound is meant to stop.
        final int connectMillis = boundedTimeout(configuredConnectMillis, remainingMillis / 2);
        final int readMillis = boundedTimeout(configuredReadMillis, remainingMillis - connectMillis);

        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setInstanceFollowRedirects(false);
            // HttpURLConnection defaults to no timeout at all.
            httpUrlConnection.setConnectTimeout(connectMillis);
            httpUrlConnection.setReadTimeout(readMillis);

            return resolveRedirectLocation(urlString, httpUrlConnection);
        }
        finally {
            if (httpUrlConnection != null) {
                // Only the status line and the Location header are read and the
                // connection is discarded immediately, so there is no body worth
                // draining for reuse -- and draining it would block for a further read
                // timeout after a hop that has already stalled.
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