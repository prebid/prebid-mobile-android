/*
 *    Copyright 2020-2021 Prebid.org, Inc.
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

package org.prebid.mobile.http;

import android.os.Looper;
import android.support.annotation.MainThread;

import org.json.JSONException;
import org.json.JSONObject;
import org.prebid.mobile.BidLog;
import org.prebid.mobile.LogUtil;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.ResultCode;
import org.prebid.mobile.tasksmanager.TasksManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public abstract class HTTPPost {

    static final String COOKIE_HEADER = "Cookie";

    public HTTPPost() {
        super();
    }

    public void execute() {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            TasksManager.getInstance().executeOnBackgroundThread(new Runnable() {
                @Override
                public void run() {
                    TaskResult<JSONObject> response = makeHttpRequest();
                    postResultOnMainThread(response);
                }
            });
        } else {
            TaskResult<JSONObject> response = makeHttpRequest();
            postResultOnMainThread(response);
        }
    }

    protected TaskResult<JSONObject> makeHttpRequest() {
        try {
            long demandFetchStartTime = System.currentTimeMillis();

            BidLog.BidLogEntry entry = new BidLog.BidLogEntry();

            URL url = new URL(getUrl());
            entry.setRequestUrl(getUrl());

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("Accept", "application/json");
            if(canIAccessDeviceData()) {
                String existingCookie = getExistingCookie();
                if (existingCookie != null) {
                    conn.setRequestProperty(COOKIE_HEADER, existingCookie);
                } // todo still pass cookie if limit ad tracking?
            }
            conn.setRequestMethod("POST");
            conn.setConnectTimeout(PrebidMobile.getTimeoutMillis());

            // Add post data
            OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
            JSONObject postData = getPostData();
            String postString = postData.toString();
            LogUtil.d("Sending request for auction " + getAuctionId() + " with post data: " + postString);
            wr.write(postString);
            wr.flush();

            entry.setRequestBody(postString);

            // Start the connection
            conn.connect();

            // Read request response
            int httpResult = conn.getResponseCode();
            long demandFetchEndTime = System.currentTimeMillis();

            entry.setResponseCode(httpResult);

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
                entry.setResponse(result);
                JSONObject response = new JSONObject(result);
                httpCookieSync(conn.getHeaderFields());
                // in the future, this can be improved to parse response base on request versions
                if (!isTimeoutMillisUpdated()) {
                    int tmaxRequest = -1;
                    try {
                        tmaxRequest = response.getJSONObject("ext").getInt("tmaxrequest");
                    } catch (JSONException e) {
                        // ignore this
                    }
                    if (tmaxRequest >= 0) {
                        PrebidMobile.setTimeoutMillis(Math.min((int) (demandFetchEndTime - demandFetchStartTime) + tmaxRequest + 200, 2000)); // adding 200ms as safe time
                        setTimeoutMillisUpdated(true);
                    }
                }

                BidLog.getInstance().setLastEntry(entry);

                return new TaskResult<>(response);
            } else if (httpResult >= HttpURLConnection.HTTP_BAD_REQUEST) {
                StringBuilder builder = new StringBuilder();
                InputStream is = conn.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(is, "utf-8"));
                String line;
                while ((line = reader.readLine()) != null) {
                    builder.append(line);
                }
                reader.close();
                is.close();
                String result = builder.toString();
                entry.setResponse(result);
                LogUtil.d("Getting response for auction " + getAuctionId() + ": " + result);
                Pattern storedRequestNotFound = Pattern.compile("^Invalid request: Stored Request with ID=\".*\" not found.");
                Pattern storedImpNotFound = Pattern.compile("^Invalid request: Stored Imp with ID=\".*\" not found.");
                Pattern invalidBannerSize = Pattern.compile("^Invalid request: Request imp\\[\\d\\].banner.format\\[\\d\\] must define non-zero \"h\" and \"w\" properties.");
                Pattern invalidInterstitialSize = Pattern.compile("Invalid request: Unable to set interstitial size list");
                Matcher m = storedRequestNotFound.matcher(result);
                Matcher m2 = invalidBannerSize.matcher(result);
                Matcher m3 = storedImpNotFound.matcher(result);
                Matcher m4 = invalidInterstitialSize.matcher(result);

                BidLog.getInstance().setLastEntry(entry);

                if (m.find() || result.contains("No stored request")) {
                    return new TaskResult<>(ResultCode.INVALID_ACCOUNT_ID);
                } else if (m3.find() || result.contains("No stored imp")) {
                    return new TaskResult<>(ResultCode.INVALID_CONFIG_ID);
                } else if (m2.find() || m4.find() || result.contains("Request imp[0].banner.format")) {
                    return new TaskResult<>(ResultCode.INVALID_SIZE);
                } else {
                    return new TaskResult<>(ResultCode.PREBID_SERVER_ERROR);
                }
            }

        } catch (MalformedURLException e) {
            return new TaskResult<>(e);
        } catch (UnsupportedEncodingException e) {
            return new TaskResult<>(e);
        } catch (SocketTimeoutException ex) {
            return new TaskResult<>(ResultCode.TIMEOUT);
        } catch (IOException e) {
            return new TaskResult<>(e);
        } catch (JSONException e) {
            return new TaskResult<>(e);
        } catch (NoContextException ex) {
            return new TaskResult<>(ResultCode.INVALID_CONTEXT);
        } catch (Exception e) {
            return new TaskResult<>(e);
        }
        return new TaskResult<>(new RuntimeException("ServerConnector exception"));
    }

    private void postResultOnMainThread(final TaskResult<JSONObject> result) {
        TasksManager.getInstance().executeOnMainThread(new Runnable() {
            @Override
            public void run() {
                onPostExecute(result);
            }
        });
    }

    @MainThread
    protected abstract void onPostExecute(TaskResult<JSONObject> response);

    protected abstract String getUrl();

    protected abstract void setTimeoutMillisUpdated(boolean b);

    protected abstract boolean isTimeoutMillisUpdated();

    protected abstract String getAuctionId();

    protected abstract JSONObject getPostData() throws NoContextException;

    protected abstract boolean canIAccessDeviceData();

    protected abstract String getExistingCookie();

    protected abstract void httpCookieSync(Map<String, List<String>> headerFields);
}

