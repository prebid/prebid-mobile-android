package org.prebid.mobile.prebidserver;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.text.TextUtils;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.core.LogUtil;
import org.prebid.mobile.prebidserver.internal.Settings;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;

class ServerConnector extends AsyncTask<Object, Object, JSONObject> {

    interface ServerListener {
        void onServerResponded(JSONObject response);
    }

    private JSONObject postData;
    private WeakReference<ServerListener> listener;
    private String url;
    private Context context;

    ServerConnector(JSONObject postData, ServerListener listener, String url, Context context) {
        this.postData = postData;
        this.listener = new WeakReference<ServerListener>(listener);
        this.url = url;
        this.context = context;
    }

    @Override
    protected JSONObject doInBackground(Object... voids) {
        try {
            URL url = new URL(this.url);

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            String existingCookie = getExistingCookie();
            if (existingCookie != null) {
                conn.setRequestProperty(Settings.COOKIE_HEADER, existingCookie);
            } // todo still pass cookie if limit ad tracking?
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(Settings.REQUEST_TIME_OUR_MILLIS);

            // Add post data
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            wr.write(postData.toString());
            wr.flush();

            // Start the connection
            conn.connect();

            // Read request response
            int httpResult = conn.getResponseCode();

            if (httpResult == HttpURLConnection.HTTP_OK) {
                StringBuilder builder = new StringBuilder();
                InputStream is = conn.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                String result = builder.toString();
                JSONObject response = new JSONObject(result);
                httpCookieSync(conn.getHeaderFields());
                // in the future, this can be improved to parse response base on request versions
                return response;
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace(); // catches SocketTimeOutException, etc
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        ServerListener listener = this.listener.get();
        if (listener != null) {
            listener.onServerResponded(jsonObject);
        }
    }

    /**
     * Synchronize the uuid2 cookie to the Webview Cookie Jar
     * This is only done if there is no present cookie.
     *
     * @param headers headers to extract cookies from for syncing
     */
    @SuppressWarnings("deprecation")
    private void httpCookieSync(Map<String, List<String>> headers) {
        if (headers == null || headers.isEmpty()) return;
        CookieManager cm = CookieManager.getInstance();
        if (cm == null) {
            LogUtil.i(Settings.TAG, "Unable to find a CookieManager");
            return;
        }
        try {
            String existingUUID = getExistingCookie();

            for (Map.Entry<String, List<String>> entry : headers.entrySet()) {
                String key = entry.getKey();
                // Only "Set-cookie" and "Set-cookie2" pair will be parsed
                if (key != null && (key.equalsIgnoreCase(Settings.VERSION_ZERO_HEADER)
                        || key.equalsIgnoreCase(Settings.VERSION_ONE_HEADER))) {
                    for (String cookieStr : entry.getValue()) {
                        if (!TextUtils.isEmpty(cookieStr) && cookieStr.contains(Settings.AN_UUID)) {
                            // pass uuid2 to WebView Cookie jar if it's empty or outdated
                            if (existingUUID == null || !cookieStr.contains(existingUUID)) {
                                cm.setCookie(Settings.COOKIE_DOMAIN, cookieStr);
                                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                                    // CookieSyncManager is deprecated in API 21 Lollipop
                                    CookieSyncManager.createInstance(context);
                                    CookieSyncManager csm = CookieSyncManager.getInstance();
                                    if (csm == null) {
                                        LogUtil.i(Settings.TAG, "Unable to find a CookieSyncManager");
                                        return;
                                    }
                                    csm.sync();
                                } else {
                                    cm.flush();
                                }
                            }
                        }
                    }
                }
            }
        } catch (IllegalStateException ise) {
        } catch (Exception e) {
        }
    }

    private String getExistingCookie() {
        try {
            CookieSyncManager.createInstance(context);
            CookieManager cm = CookieManager.getInstance();
            if (cm != null) {
                String wvcookie = cm.getCookie(Settings.COOKIE_DOMAIN);
                if (!TextUtils.isEmpty(wvcookie)) {
                    String[] existingCookies = wvcookie.split("; ");
                    for (String cookie : existingCookies) {
                        if (cookie != null && cookie.contains(Settings.AN_UUID)) {
                            return cookie;
                        }
                    }
                }
            }
        } catch (Exception e) {
        }
        return null;
    }

}
