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

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.prebid.mobile.rendering.networking.BaseNetworkTask.REDIRECT_TASK;

import androidx.test.filters.Suppress;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.PrebidMobile;
import org.robolectric.RobolectricTestRunner;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.HashMap;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

@RunWith(RobolectricTestRunner.class)
public class BaseNetworkTaskTest {

    private MockWebServer server;
    private boolean success;
    private BaseNetworkTask.GetUrlResult response;
    private String msg;
    private BaseNetworkTask.GetUrlParams params;
    private Exception exception;

    ResponseHandler baseResponseHandler = new ResponseHandler() {

        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            success = true;
            BaseNetworkTaskTest.this.response = response;
        }

        @Override
        public void onError(String msg, long responseTime) {
            success = false;
            BaseNetworkTaskTest.this.msg = msg;
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            success = false;
            exception = e;
        }
    };

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        params = new BaseNetworkTask.GetUrlParams();
        params.name = "TESTFIRST";
        params.userAgent = "user-agent";
        HttpUrl baseUrl = server.url("/first");
        params.url = baseUrl.url().toString();
        params.requestType = "GET";
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Suppress
    public void testBaseNetworkNullHandlerParam() {
        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(null);
        assertNotNull(baseNetworkTask);
    }

    @Test
    public void testValidParams() {
        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(null);
        assertEquals(false, baseNetworkTask.validParams((BaseNetworkTask.GetUrlParams) null));

        BaseNetworkTask.GetUrlParams paramsValid = new BaseNetworkTask.GetUrlParams();
        assertTrue(baseNetworkTask.validParams(paramsValid));
    }

    @Test
    public void testCustomHeaders() throws InterruptedException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("test body"));
        HashMap<String, String> customHeaders = new HashMap<>();
        customHeaders.put("TEST_HEADER_A", "HEADER_VALUE_1");
        customHeaders.put("TEST_HEADER_B", "HEADER_VALUE_2");
        PrebidMobile.setCustomHeaders(customHeaders);

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        RecordedRequest request = server.takeRequest();
        Assert.assertEquals("HEADER_VALUE_1", request.getHeader("TEST_HEADER_A"));
        Assert.assertEquals("HEADER_VALUE_2", request.getHeader("TEST_HEADER_B"));
    }

    @Test
    public void testSuccessDoInBackground() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is google!"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("This is google!", response.responseString);
        Assert.assertEquals(true, success);
    }

    @Test
    public void test400ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(400).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(exception.getLocalizedMessage().contains("Code 400"));
        Assert.assertEquals(false, success);
    }

    @Test
    public void test401ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(401).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(exception.getLocalizedMessage().contains("Code 401"));
        Assert.assertEquals(false, success);
    }

    @Test
    public void test402ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(402).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(exception.getLocalizedMessage().contains("Code 402"));
        Assert.assertEquals(false, success);
    }

    @Test
    public void test403ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(403).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(exception.getLocalizedMessage().contains("Code 403"));
        Assert.assertEquals(false, success);
    }

    @Test
    public void test404ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(404).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        MatcherAssert.assertThat(exception.getLocalizedMessage(), containsString("404"));
        Assert.assertFalse(success);
    }

    @Test
    public void test405ExceptionError() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(405).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(exception.getLocalizedMessage().contains("Code 405"));
        Assert.assertEquals(false, success);
    }

    @Test
    public void testEmptyVast() throws IOException {
        server.enqueue(new MockResponse().setResponseCode(200).setBody("<VAST version=\"2.0\"></VAST>"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Invalid VAST Response: less than 100 characters.", msg);
        Assert.assertEquals(false, success);
    }

    @Test
    public void test301Redirect() throws IOException {
        server.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + server.url("/new-path"))
                            .setBody("This page has moved!"));
        server.enqueue(new MockResponse().setBody("This is the new location!"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        params.name = REDIRECT_TASK;

        try {
            baseNetworkTask.execute(params);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(true, success);
    }

    @Test
    public void test302Redirect() {
        server.enqueue(new MockResponse().setResponseCode(302)
                                         .addHeader("Location: " + server.url("/new-path"))
                                         .setBody("This page has moved"));
        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        params.name = REDIRECT_TASK;

        try {
            baseNetworkTask.execute(params);
        } catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(true, success);
    }

    @Test
    public void testSpecialSymbols() throws IOException {
        ByteArrayOutputStream request = new ByteArrayOutputStream();

        BaseNetworkTask.sendRequest("{\"app\":\"天気\"}", request);

        Assert.assertEquals("{\"app\":\"天気\"}", request.toString());
    }

}
