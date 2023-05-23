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

package org.prebid.mobile.rendering.networking;

import android.os.AsyncTask;

import androidx.annotation.Nullable;
import androidx.annotation.VisibleForTesting;

import org.apache.http.conn.ConnectTimeoutException;
import org.jetbrains.annotations.NotNull;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.rendering.loading.FileDownloadTask;
import org.prebid.mobile.rendering.networking.exception.BaseExceptionHolder;
import org.prebid.mobile.rendering.utils.helpers.Utils;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;
import java.util.Map;

/**
 * Performs HTTP communication in the background, i.e. off the UI thread.
 */
public class BaseNetworkTask
    extends AsyncTask<BaseNetworkTask.GetUrlParams, Integer, BaseNetworkTask.GetUrlResult> {
    private static final String TAG = BaseNetworkTask.class.getSimpleName();

    public static final int TIMEOUT_DEFAULT = 2000;
    public static final int SOCKET_TIMEOUT = 3000;

    public static final int MAX_REDIRECTS_COUNT = 5;

    public static final String REDIRECT_TASK = "RedirectTask";
    public static final String DOWNLOAD_TASK = "DownloadTask";
    public static final String STATUS_TASK = "StatusTask";

    protected static final String USER_AGENT_HEADER = "User-Agent";
    protected static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    protected static final String ACCEPT_HEADER = "Accept";
    protected static final String ACCEPT_HEADER_VALUE = "application/x-www-form-urlencoded,application/json,text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    protected static final String CONTENT_TYPE_HEADER = "Content-Type";
    protected static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    protected GetUrlResult result;

    private long start;
    private BaseResponseHandler responseHandler;
    private URLConnection connection = null;

    /**
     * Creates a network object
     *
     * @param handler instance of a class handling ad server responses (like , InterstitialSwitchActivity)
     */
    public BaseNetworkTask(BaseResponseHandler handler) {
        responseHandler = handler;
        result = new GetUrlResult();
    }

    @Override
    protected GetUrlResult doInBackground(GetUrlParams... params) {
        return processDoInBackground(params);
    }

    @Override
    protected void onPostExecute(GetUrlResult urlResult) {
        if (urlResult == null) {
            LogUtil.debug(TAG, "URL result is null");
            destroy();
            return;
        }
        if (responseHandler == null) {
            LogUtil.debug(TAG, "No ResponseHandler on: may be a tracking event");
            destroy();
            return;
        }

        //For debugging purposes. Helps in client issues, if any.
        LogUtil.debug(TAG, "Result: " + urlResult.responseString);

        long stop = System.currentTimeMillis();
        long delta = stop - start;
        urlResult.responseTime = delta;
        if (urlResult.getException() != null) {
            ((ResponseHandler) responseHandler).onErrorWithException(urlResult.getException(), delta);
            destroy();
            return;
        }

        //differentiate between vast response & normal tracking response
        //Ex: <VAST version="2.0"> </VAST> is a wrong response for av calls. So should fail
        if (urlResult.responseString != null && urlResult.responseString.length() < 100 && urlResult.responseString.contains("<VAST")) {
            ((ResponseHandler) responseHandler).onError("Invalid VAST Response: less than 100 characters.", delta);
        } else {
            ((ResponseHandler) responseHandler).onResponse(urlResult);
        }

        destroy();
    }

    @Override
    protected void onCancelled(GetUrlResult getUrlResult) {
        super.onCancelled(getUrlResult);
        LogUtil.debug(TAG, "Request cancelled. Disconnecting connection");
        destroy();
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }

    public void destroy() {
        responseHandler = null;
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection) connection).disconnect();
        }
    }

    /*  NOTE THIS GETS OVERRIDDEN IN CHILD CLASS */
    public GetUrlResult customParser(int code, URLConnection urlConnection) {
        return result;
    }

    public GetUrlResult sendRequest(GetUrlParams param) throws Exception {
        if (param.url.isEmpty()) {
            LogUtil.error(TAG, "url is empty. Set url in PrebidMobile (PrebidRenderingSettings).");
        }
        LogUtil.debug(TAG, "url: " + param.url);
        LogUtil.debug(TAG, "queryParams: " + param.queryParams);

        int responseCode = 0;
        connection = setHttpURLConnectionProperty(param);

        if (connection instanceof HttpURLConnection) {
            responseCode = ((HttpURLConnection) connection).getResponseCode();
        }

        if (Utils.isNotBlank(param.name) && !DOWNLOAD_TASK.equals(param.name) && !REDIRECT_TASK.equals(param.name) && !STATUS_TASK.equals(param.name)) {
            result = parseHttpURLResponse(responseCode);
        }
        result = customParser(responseCode, connection);
        result.statusCode = responseCode;
        return result;
    }

    public boolean validParams(GetUrlParams... params) {
        if (params == null || params[0] == null) {
            result.setException(new Exception("Invalid Params"));
            return false;
        }
        return true;
    }

    /**
     * Reads server response from <code>InputStream<code/> and returns a string response.
     * Handles stream closing properly.
     *
     * @param inputStream stream to read response from.
     * @return A String containing server response or null if input stream is null.
     * @throws IOException when failing to close the stream.
     */
    protected String readResponse(
        @Nullable
            InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return null;
        }

        StringBuilder response = new StringBuilder();
        boolean runAtLeastOnce = false;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            final char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer, 0, buffer.length)) > 0) {
                runAtLeastOnce = true;
                response.append(buffer, 0, charsRead);
            }
        } catch (Exception exception) {
            if (runAtLeastOnce) {
                LogUtil.error(TAG, "Exception in readResponse(): " + exception.getMessage());
            } else {
                LogUtil.error(TAG, "Empty response: " + exception.getMessage());
            }
        }

        return response.toString();
    }

    private GetUrlResult processDoInBackground(GetUrlParams... params) {
        GetUrlParams param;

        if (isCancelled()) {
            return result;
        }
        else if (validParams(params) && !isCancelled()) {
            param = params[0];
            try {
                start = System.currentTimeMillis();
                result = sendRequest(param);
            }
            catch (MalformedURLException e) {
                LogUtil.warning(TAG, "Network Error: MalformedURLException" + e.getMessage());
                // This error will be handled in onPostExecute()- so no need to handle here - Nice
                result.setException(e);
            }
            catch (SocketTimeoutException e) {
                LogUtil.warning(TAG, "Network Error: SocketTimeoutException" + e.getMessage());
                result.setException(e);
            }
            catch (ConnectTimeoutException e) {
                LogUtil.warning(TAG, "Network Error: ConnectTimeoutException" + e.getMessage());
                result.setException(e);
            }
            catch (IOException e) {
                LogUtil.warning(TAG, "Network Error: IOException" + e.getMessage());
                result.setException(e);
            }
            catch (Exception e) {
                LogUtil.warning(TAG, "Network Error: Exception" + e.getMessage());
                result.setException(e);
            }
            finally {
                if (connection instanceof HttpURLConnection) {
                    ((HttpURLConnection) connection).disconnect();
                }
            }
        }
        else {
            result = null;
        }

        return result;
    }

    private URLConnection setHttpURLConnectionProperty(GetUrlParams param) throws Exception {
        String queryParams = "";
        if (param.requestType.equals("GET") && param.queryParams != null) {
            queryParams = "?" + param.queryParams;
        }
        URL url = new URL(param.url + queryParams);
        connection = url.openConnection();
        if (connection instanceof HttpURLConnection) {
            ((HttpURLConnection) connection).setRequestMethod(param.requestType);
            ((HttpURLConnection) connection).setInstanceFollowRedirects(false);
        }

        connection.setRequestProperty(USER_AGENT_HEADER, param.userAgent);
        connection.setRequestProperty(ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());
        connection.setRequestProperty(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
        connection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE);
        this.setCustomHeadersIfAvailable(connection);

        connection.setConnectTimeout(PrebidMobile.getTimeoutMillis());
        if (!(this instanceof FileDownloadTask)) {
            connection.setReadTimeout(SOCKET_TIMEOUT);
        }

        if ("POST".equals(param.requestType)) {
            // Send post request
            connection.setDoOutput(true);
            DataOutputStream wr = null;
            try {
                wr = new DataOutputStream(connection.getOutputStream());
                if (param.queryParams != null) {
                    sendRequest(param.queryParams, wr);
                }
            } finally {
                if (wr != null) {
                    wr.flush();
                    wr.close();
                }
            }
        }

        connection = openConnectionCheckRedirects(connection);
        return connection;
    }

    @VisibleForTesting
    protected static void sendRequest(@NotNull String requestBody, @NotNull OutputStream requestStream) throws IOException {
        byte[] bytes = requestBody.getBytes();
        for (byte b : bytes) {
            requestStream.write(b);
        }
    }

    private void setCustomHeadersIfAvailable(URLConnection connection) {
        if (!PrebidMobile.getCustomHeaders().isEmpty()) {
            for (Map.Entry<String, String> customHeader : PrebidMobile.getCustomHeaders().entrySet()) {
                connection.setRequestProperty(customHeader.getKey(), customHeader.getValue());
            }
        }
    }

    private URLConnection openConnectionCheckRedirects(URLConnection connection) throws Exception {
        boolean redirected;
        int redirects = 0;
        do {
            redirected = false;
            int status = 0;

            if (connection instanceof HttpURLConnection) {
                status = ((HttpURLConnection) connection).getResponseCode();
            }

            if (status >= 300 && status <= 307 && status != 306 && status != HttpURLConnection.HTTP_NOT_MODIFIED) {
                URL base = connection.getURL();
                String location = connection.getHeaderField("Location");

                LogUtil.debug(TAG, (location == null)
                        ? "not found location"
                        : "location = " + location);
                URL target = null;
                if (location != null) {
                    target = new URL(base, location);
                }

                ((HttpURLConnection) connection).disconnect();

                // Redirection should be allowed only for HTTP and HTTPS
                // and should be limited to 5 redirections at most.
                if (target == null || !(target.getProtocol().equals("http")
                                        || target.getProtocol().equals("https"))
                    || redirects >= MAX_REDIRECTS_COUNT) {
                    String error = String.format("Bad server response - [HTTP Response code of %s]", status);
                    LogUtil.error(TAG, error);
                    throw new Exception(error);
                }
                redirected = true;
                connection = target.openConnection();
                redirects++;
            }
        }
        while (redirected);
        return connection;
    }

    private GetUrlResult parseHttpURLResponse(int httpURLResponseCode) throws Exception {

        //Do all parsing in the caller class, because there is no generic way of processing this response
        String response = "";

        if (httpURLResponseCode == 200) {
            response = readResponse(connection.getInputStream());
        }
        else if (httpURLResponseCode >= 400 && httpURLResponseCode < 600) {
            String status = String.format(
                    Locale.getDefault(),
                    "Code %d. %s",
                    httpURLResponseCode,
                    readResponse(((HttpURLConnection) connection).getErrorStream())
            );
            LogUtil.error(TAG, status);
            throw new Exception(status);
        }
        else {
            String error = String.format("Bad server response - [HTTP Response code of %s]", httpURLResponseCode);
            if (httpURLResponseCode == 204) error = "Response code 204. No bids.";
            LogUtil.error(TAG, error);
            throw new Exception(error);
        }
        result.responseString = response;

        return result;
    }

    public static class GetUrlParams {
        public String url;
        public String queryParams;
        public String name;
        public String userAgent;
        public String requestType;
    }

    public static class GetUrlResult extends BaseExceptionHolder {
        public String responseString;
        public int statusCode;
        public long responseTime;
        public String originalUrl;
        public String contentType;
        public String[] JSRedirectURI;

        public boolean isOkStatusCode() {
            return statusCode >= 200 && statusCode < 300;
        }

    }
}