package org.prebid.mobile.rendering.networking;

import android.os.AsyncTask;

import androidx.annotation.Nullable;

import org.apache.http.conn.ConnectTimeoutException;
import org.prebid.mobile.rendering.networking.exception.BaseExceptionHolder;
import org.prebid.mobile.rendering.sdk.PrebidRenderingSettings;
import org.prebid.mobile.rendering.utils.helpers.Utils;
import org.prebid.mobile.rendering.utils.logger.OXLog;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Locale;

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

    protected static final String USER_AGENT_HEADER = "User-Agent";
    protected static final String ACCEPT_LANGUAGE_HEADER = "Accept-Language";
    protected static final String ACCEPT_HEADER = "Accept";
    protected static final String ACCEPT_HEADER_VALUE = "application/x-www-form-urlencoded,application/json,text/plain,text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8";
    protected static final String CONTENT_TYPE_HEADER = "Content-Type";
    protected static final String CONTENT_TYPE_HEADER_VALUE = "application/json";

    protected GetUrlResult mResult;

    private long mStart;
    private BaseResponseHandler mResponseHandler;
    private URLConnection mConnection = null;

    /**
     * Creates a network object
     *
     * @param handler instance of a class handling ad server responses (like , InterstitialSwitchActivity)
     */
    public BaseNetworkTask(BaseResponseHandler handler) {
        mResponseHandler = handler;
        mResult = new GetUrlResult();
    }

    @Override
    protected GetUrlResult doInBackground(GetUrlParams... params) {
        return processDoInBackground(params);
    }

    @Override
    protected void onPostExecute(GetUrlResult urlResult) {
        if (urlResult == null) {
            OXLog.debug(TAG, "URL result is null");
            return;
        }
        if (mResponseHandler == null) {
            OXLog.debug(TAG, "No ResponseHandler on: may be a tracking event");
            return;
        }

        //For debugging purposes. Helps in client issues, if any.
        OXLog.debug(TAG, "Result: " + urlResult.responseString);

        long stop = System.currentTimeMillis();
        long delta = stop - mStart;
        urlResult.responseTime = delta;
        if (urlResult.getException() != null) {
            ((ResponseHandler) mResponseHandler).onErrorWithException(urlResult.getException(), delta);
            return;
        }

        //differentiate between vast response & normal tracking response
        //Ex: <VAST version="2.0"> </VAST> is a wrong response for av calls. So should fail
        if (urlResult.responseString != null && urlResult.responseString.length() < 100 && urlResult.responseString.contains("<VAST")) {
            ((ResponseHandler) mResponseHandler).onError("Invalid VAST Response: less than 100 characters.", delta);
        }
        else {
            ((ResponseHandler) mResponseHandler).onResponse(urlResult);
        }
    }

    @Override
    protected void onCancelled() {
        super.onCancelled();
        OXLog.debug(TAG, "Request cancelled. Disconnecting connection");
        if (mConnection instanceof HttpURLConnection) {
            ((HttpURLConnection) mConnection).disconnect();
        }
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {
        super.onProgressUpdate(progress);
    }

    /*  NOTE THIS GETS OVERRIDDEN IN CHILD CLASS */
    public GetUrlResult customParser(int code, URLConnection urlConnection) {
        return mResult;
    }

    public GetUrlResult sendRequest(GetUrlParams param) throws Exception {
        OXLog.debug(TAG, "url: " + param.url);
        OXLog.debug(TAG, "queryParams: " + param.queryParams);

        int responseCode = 0;
        mConnection = setHttpURLConnectionProperty(param);

        if (mConnection instanceof HttpURLConnection) {
            responseCode = ((HttpURLConnection) mConnection).getResponseCode();
        }

        if (Utils.isNotBlank(param.name)
            && !DOWNLOAD_TASK.equals(param.name)
            && !REDIRECT_TASK.equals(param.name)) {
            mResult = parseHttpURLResponse(responseCode);
        }
        mResult = customParser(responseCode, mConnection);
        mResult.statusCode = responseCode;
        return mResult;
    }

    public boolean validParams(GetUrlParams... params) {
        if (params == null || params[0] == null) {
            mResult.setException(new Exception("Invalid Params"));
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
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
            final char[] buffer = new char[1024];
            int charsRead;
            while ((charsRead = reader.read(buffer, 0, buffer.length)) > 0) {
                response.append(buffer, 0, charsRead);
            }
        }

        return response.toString();
    }

    private GetUrlResult processDoInBackground(GetUrlParams... params) {
        GetUrlParams param;

        if (isCancelled()) {
            return mResult;
        }
        else if (validParams(params) && !isCancelled()) {
            param = params[0];
            try {
                mStart = System.currentTimeMillis();
                mResult = sendRequest(param);
            }
            catch (MalformedURLException e) {
                OXLog.warn(TAG, "Network Error: MalformedURLException" + e.getMessage());
                // This error will be handled in onPostExecute()- so no need to handle here - Nice
                mResult.setException(e);
            }
            catch (SocketTimeoutException e) {
                OXLog.warn(TAG, "Network Error: SocketTimeoutException" + e.getMessage());
                mResult.setException(e);
            }
            catch (ConnectTimeoutException e) {
                OXLog.warn(TAG, "Network Error: ConnectTimeoutException" + e.getMessage());
                mResult.setException(e);
            }
            catch (IOException e) {
                OXLog.warn(TAG, "Network Error: IOException" + e.getMessage());
                mResult.setException(e);
            }
            catch (Exception e) {
                OXLog.warn(TAG, "Network Error: Exception" + e.getMessage());
                mResult.setException(e);
            }
            finally {
                if (mConnection instanceof HttpURLConnection) {
                    ((HttpURLConnection) mConnection).disconnect();
                }
            }
        }
        else {
            mResult = null;
        }

        return mResult;
    }

    private URLConnection setHttpURLConnectionProperty(GetUrlParams param) throws Exception {
        URL url = new URL(param.url);
        mConnection = url.openConnection();
        if (mConnection instanceof HttpURLConnection) {
            ((HttpURLConnection) mConnection).setRequestMethod(param.requestType);
            ((HttpURLConnection) mConnection).setInstanceFollowRedirects(false);
        }

        mConnection.setRequestProperty(USER_AGENT_HEADER, param.userAgent);
        mConnection.setRequestProperty(ACCEPT_LANGUAGE_HEADER, Locale.getDefault().toString());
        mConnection.setRequestProperty(ACCEPT_HEADER, ACCEPT_HEADER_VALUE);
        mConnection.setRequestProperty(CONTENT_TYPE_HEADER, CONTENT_TYPE_HEADER_VALUE);

        mConnection.setReadTimeout(SOCKET_TIMEOUT);
        mConnection.setConnectTimeout(PrebidRenderingSettings.getTimeoutMillis());

        if ("POST".equals(param.requestType)) {
            // Send post request
            mConnection.setDoOutput(true);
            DataOutputStream wr = null;
            try {
                wr = new DataOutputStream(mConnection.getOutputStream());
                if (param.queryParams != null) {
                    wr.writeBytes(param.queryParams);
                }
            }
            finally {
                if (wr != null) {
                    wr.flush();
                    wr.close();
                }
            }
        }

        mConnection = openConnectionCheckRedirects(mConnection);
        return mConnection;
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

                OXLog.debug(TAG, (location == null)
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
                    OXLog.error(TAG, error);
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
            response = readResponse(mConnection.getInputStream());
        }
        else if (httpURLResponseCode >= 400 && httpURLResponseCode < 600) {
            String status = String.format(Locale.getDefault(), "Code %d. %s", httpURLResponseCode, readResponse(((HttpURLConnection) mConnection).getErrorStream()));
            OXLog.error(TAG, status);
            throw new Exception(status);
        }
        else {
            String error = String.format("Bad server response - [HTTP Response code of %s]", httpURLResponseCode);
            OXLog.error(TAG, error);
            throw new Exception(error);
        }
        mResult.responseString = response;

        return mResult;
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
    }
}