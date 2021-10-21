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

import androidx.test.filters.Suppress;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.prebid.mobile.rendering.networking.BaseNetworkTask.REDIRECT_TASK;

@RunWith(RobolectricTestRunner.class)
public class BaseNetworkTaskTest {

    private MockWebServer mServer;
    private boolean sSuccess;
    private BaseNetworkTask.GetUrlResult mResponse;
    private String mMsg;
    private BaseNetworkTask.GetUrlParams mParams;
    private Exception mException;

    ResponseHandler baseResponseHandler = new ResponseHandler() {

        @Override
        public void onResponse(BaseNetworkTask.GetUrlResult response) {
            sSuccess = true;
            mResponse = response;
        }

        @Override
        public void onError(String msg, long responseTime) {
            sSuccess = false;
            mMsg = msg;
        }

        @Override
        public void onErrorWithException(Exception e, long responseTime) {
            sSuccess = false;
            mException = e;
        }
    };

    @Before
    public void setUp() throws Exception {
        mServer = new MockWebServer();
        mParams = new BaseNetworkTask.GetUrlParams();
        mParams.name = "TESTFIRST";
        mParams.userAgent = "user-agent";
        HttpUrl baseUrl = mServer.url("/first");
        mParams.url = baseUrl.url().toString();
        mParams.requestType = "GET";
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
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
    public void testSuccessDoInBackground() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("This is google!"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("This is google!", mResponse.responseString);
        Assert.assertEquals(true, sSuccess);
    }

    @Test
    public void test400ExceptionError() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(400).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(mException.getLocalizedMessage().contains("Code 400"));
        Assert.assertEquals(false, sSuccess);
    }

    @Test
    public void test401ExceptionError() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(401).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(mException.getLocalizedMessage().contains("Code 401"));
        Assert.assertEquals(false, sSuccess);
    }

    @Test
    public void test402ExceptionError() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(402).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(mException.getLocalizedMessage().contains("Code 402"));
        Assert.assertEquals(false, sSuccess);
    }

    @Test
    public void test403ExceptionError() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(403).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(mException.getLocalizedMessage().contains("Code 403"));
        Assert.assertEquals(false, sSuccess);
    }

    @Ignore
    @Test
    public void test404ExceptionError() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(404).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("404", mException.getLocalizedMessage());
        Assert.assertEquals(false, sSuccess);
    }

    @Test
    public void test405ExceptionError() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(405).setBody("404 not found"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertTrue(mException.getLocalizedMessage().contains("Code 405"));
        Assert.assertEquals(false, sSuccess);
    }

    @Test
    public void testEmptyVast() throws IOException {
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("<VAST version=\"2.0\"></VAST>"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals("Invalid VAST Response: less than 100 characters.", mMsg);
        Assert.assertEquals(false, sSuccess);
    }

    @Test
    public void test301Redirect() throws IOException {
        mServer.enqueue(new MockResponse()
                            .setResponseCode(301)
                            .addHeader("Location: " + mServer.url("/new-path"))
                            .setBody("This page has moved!"));
        mServer.enqueue(new MockResponse().setBody("This is the new location!"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        mParams.name = REDIRECT_TASK;

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(true, sSuccess);
    }

    @Test
    public void test302Redirect() {
        mServer.enqueue(new MockResponse().setResponseCode(302)
                                          .addHeader("Location: " + mServer.url("/new-path"))
                                          .setBody("This page has moved"));
        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location"));

        BaseNetworkTask baseNetworkTask = new BaseNetworkTask(baseResponseHandler);

        mParams.name = REDIRECT_TASK;

        try {
            baseNetworkTask.execute(mParams);
        }
        catch (Exception e) {
            e.printStackTrace();
        }

        Assert.assertEquals(true, sSuccess);
    }
}
