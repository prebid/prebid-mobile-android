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

package org.prebid.mobile.renderingtestapp.mock;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.prebid.mobile.renderingtestapp.utils.InputStreamUtils;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class MockServerManager {

    private final static String ENDPOINT_CLEAR_LOGS = "https://10.0.2.2:8000/api/clear_logs";
    private final static String ENDPOINT_GET_LOGS = "https://10.0.2.2:8000/api/logs";

    private final static String PROPERTY_METHOD = "method";
    private final static String PROPERTY_URL = "url";
    private final static String PROPERTY_BODY_SIZE = "bodySize";
    private final static String PROPERTY_BODY = "body";
    private final static String PROPERTY_NAME = "name";
    private final static String PROPERTY_VALUE = "value";
    private final static String PROPERTY_QUERY_STRING = "queryString";
    private final static String PROPERTY_REQUEST = "request";
    private final static String PROPERTY_HOST = "host";
    private final static String PROPERTY_PATH = "path";

    public MockServerManager() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{
            new X509TrustManager() {
                public X509Certificate[] getAcceptedIssuers() {
                    return new X509Certificate[0];
                }

                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }};

        // Ignore differences between given hostname and certificate hostname
        HostnameVerifier hv = (hostname, session) -> true;

        // Install the all-trusting trust manager
        SSLContext sc = null;
        try {
            sc = SSLContext.getInstance("SSL");
        }
        catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        try {
            sc.init(null, trustAllCerts, new SecureRandom());
        }
        catch (KeyManagementException e) {
            e.printStackTrace();
        }
        HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        HttpsURLConnection.setDefaultHostnameVerifier(hv);
    }

    public JsonArray getLogs(String requestPrefix) {
        JsonArray results = new JsonArray();
        for (MockRequestModel model : getLoggedRequests()) {
            if (model.getPath().contains(requestPrefix)) {
                JsonObject object = new JsonObject();
                JsonObject requestObject = new JsonObject();
                JsonArray queryStringArray = new JsonArray();
                requestObject.addProperty(PROPERTY_METHOD, model.getMethod());
                requestObject.addProperty(PROPERTY_URL, "https://" + model.getHost() + model.getPath());
                requestObject.addProperty(PROPERTY_BODY_SIZE, model.getBody().length());
                requestObject.addProperty(PROPERTY_BODY, model.getBody());
                for (Map.Entry<String, String> entry : model.getQueryString().entrySet()) {
                    JsonObject entryObject = new JsonObject();
                    entryObject.addProperty(PROPERTY_NAME, entry.getKey());
                    entryObject.addProperty(PROPERTY_VALUE, entry.getValue());
                    queryStringArray.add(entryObject);
                }
                requestObject.add(PROPERTY_QUERY_STRING, queryStringArray);
                object.add(PROPERTY_REQUEST, requestObject);
                results.add(object);
            }
        }

        return results;
    }

    public JsonArray getLogs() {
        return getLogs("");
    }

    public void clearLogs() {
        try {
            URLConnection connection = new URL(ENDPOINT_CLEAR_LOGS).openConnection();
            connection.getInputStream();
        }
        catch (IOException e) {
        }
    }

    public void waitForEvent(String eventName, int expectedOccurrences, int timeout)
    throws TimeoutException, InterruptedException {
        waitForEvent(eventName, expectedOccurrences, timeout, 0);
    }

    public int getCountOfEvent(String eventName) {
        int result = 0;
        List<MockRequestModel> loggedRequests = getLoggedRequests();

        for (MockRequestModel request : loggedRequests) {
            if (request.getPath().contains(eventName)) {
                result++;
            }
        }

        return result;
    }

    public void waitForEvent(String eventName, int expectedOccurrences, int timeout, int delay)
    throws InterruptedException, TimeoutException {
        Thread.sleep(delay * 1000);
        int actualOccurrences = 0;
        for (int i = 0; i <= timeout; i++) {
            actualOccurrences = getCountOfEvent(eventName);
            if (actualOccurrences == expectedOccurrences) {
                return;
            }
            Thread.sleep(1000);
        }
        throw new TimeoutException(String.format("Expected %d occurrences of <%s> but found %d",
                                                 expectedOccurrences,
                                                 eventName,
                                                 actualOccurrences));
    }

    public void assertHarIsNotEmpty() throws AssertionError {
        Assert.assertFalse("Har Log is empty", getLoggedRequests().isEmpty());
    }

    public void waitForEvent(String eventName, int expectedOccurrences)
    throws TimeoutException, InterruptedException {
        waitForEvent(eventName, expectedOccurrences, 0, 0);
    }

    private static Map<String, String> jsonToMap(JSONObject jsonObject) throws JSONException {
        HashMap<String, String> result = new HashMap<>();
        Iterator<String> keysIterator = jsonObject.keys();
        while (keysIterator.hasNext()) {
            String key = keysIterator.next();
            String value = jsonObject.get(key).toString();
            if (value.contains("\"")) {
                value = value.substring(value.indexOf('"') + 1, value.lastIndexOf('"'));
            }
            result.put(key, value);
        }
        return result;
    }

    private List<MockRequestModel> getLoggedRequests() {
        ArrayList<MockRequestModel> result = new ArrayList<>();
        try {
            URLConnection connection = new URL(ENDPOINT_GET_LOGS).openConnection();
            String data = InputStreamUtils.convert(connection.getInputStream());
            JSONObject jsonObject = new JSONObject(data);
            JSONArray requests = (JSONArray) jsonObject.get("requests");
            for (int i = 0; i < requests.length(); i++) {
                JSONObject request = (JSONObject) requests.get(i);
                result.add(new MockRequestModel(request.getString(PROPERTY_PATH),
                                                request.getString(PROPERTY_HOST),
                                                request.getString(PROPERTY_METHOD),
                                                URLDecoder.decode(request.getString(PROPERTY_BODY), "UTF-8"),
                                                jsonToMap(((JSONObject) request.get(PROPERTY_QUERY_STRING)))));
            }
            return result;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed request to mock server: " + e.toString());
        }
        catch (JSONException e) {
            throw new RuntimeException("Failed request to mock server: " + e.toString());
        }
    }
}


