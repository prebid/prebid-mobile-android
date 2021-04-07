package com.openx.apollo.mraid.methods.network;

import android.os.AsyncTask;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import com.openx.apollo.sdk.ApolloSettings;
import com.openx.apollo.utils.logger.OXLog;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import static com.openx.apollo.mraid.methods.network.GetOriginalUrlTask.MAX_REDIRECTS;

@VisibleForTesting
public class UrlResolutionTask extends AsyncTask<String, Void, String> {
    private static final String TAG = UrlResolutionTask.class.getSimpleName();

    @NonNull private final UrlResolutionListener mListener;

    public UrlResolutionTask(@NonNull UrlResolutionListener listener) {
        mListener = listener;
    }

    @Nullable
    @Override
    protected String doInBackground(@Nullable String... urls) {
        if (urls == null || urls.length == 0) {
            return null;
        }

        String previousUrl = null;
        try {
            String locationUrl = urls[0];

            int redirectCount = 0;
            while (locationUrl != null && redirectCount < MAX_REDIRECTS) {
                // if location url is not http(s), assume it's an Android deep link
                // this scheme will fail URL validation so we have to check early
                if (!locationUrl.startsWith(ApolloSettings.SCHEME_HTTP)) {
                    return locationUrl;
                }

                previousUrl = locationUrl;
                locationUrl = getRedirectLocation(locationUrl);
                redirectCount++;
            }
        }
        catch (IOException e) {
            return null;
        }
        catch (URISyntaxException e) {
            return null;
        }
        catch (NullPointerException e) {
            return null;
        }

        return previousUrl;
    }

    @Nullable
    private String getRedirectLocation(@NonNull final String urlString) throws IOException,
                                                                               URISyntaxException {
        final URL url = new URL(urlString);

        HttpURLConnection httpUrlConnection = null;
        try {
            httpUrlConnection = (HttpURLConnection) url.openConnection();
            httpUrlConnection.setInstanceFollowRedirects(false);

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
                    OXLog.error(TAG, "IOException when closing httpUrlConnection. Ignoring.");
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
                OXLog.error(TAG, "Invalid URL redirection. baseUrl=" + baseUrl + "\n redirectUrl=" + redirectUrl);
                throw new URISyntaxException(redirectUrl, "Unable to parse invalid URL");
            }
            catch (NullPointerException e) {
                OXLog.error(TAG, "Invalid URL redirection. baseUrl=" + baseUrl + "\n redirectUrl=" + redirectUrl);
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
            mListener.onSuccess(resolvedUrl);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();

        mListener.onFailure("Task for resolving url was cancelled", null);
    }

    public interface UrlResolutionListener {
        void onSuccess(@NonNull final String resolvedUrl);

        void onFailure(@NonNull final String message, @Nullable final Throwable throwable);
    }
}