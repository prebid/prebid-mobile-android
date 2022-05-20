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

package org.prebid.mobile.rendering.networking.tracking;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class ImpressionUrlTaskTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    //@Ignore
    @Test
    public void redirectTest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location: " + server.url("/new-path"))
                                         .setBody("This page has moved!"));

        server.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location!"));

        ImpressionUrlTask impressionUrlTask = new ImpressionUrlTask(null);

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.requestType = "GET";
        params.userAgent = "user-agent";
        //get server url
        HttpUrl baseUrl = server.url("/first");
        //give server url to SDK network class
        params.url = baseUrl.url().toString();
        params.name = BaseNetworkTask.REDIRECT_TASK;

        //send SDK request
        BaseNetworkTask.GetUrlResult result = impressionUrlTask.sendRequest(params);
        //validate
        assertEquals("This is the new location!", result.responseString);

        RecordedRequest first = server.takeRequest();
        assertEquals("GET /first HTTP/1.1", first.getRequestLine());

        RecordedRequest redirect = server.takeRequest();
        assertEquals("GET /new-path HTTP/1.1", redirect.getRequestLine());
    }

    @Test
    public void redirectWithThrowExceptionTest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302).addHeader("Location: " + server.url("/new-path"))
                                         .setBody("This page has moved!"));

        server.enqueue(new MockResponse().setResponseCode(400).setBody("Bad server response. Should throw exception"));

        ImpressionUrlTask impressionUrlTask = new ImpressionUrlTask(null);

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.requestType = "GET";
        params.userAgent = "user-agent";
        //get server url
        HttpUrl baseUrl = server.url("/first");
        //give server url to SDK network class
        params.url = baseUrl.url().toString();
        params.name = BaseNetworkTask.REDIRECT_TASK;

        //send SDK request
        BaseNetworkTask.GetUrlResult result = impressionUrlTask.sendRequest(params);
        //validate. No response as an exception should have been thrown for code 400
        assertNull(result.responseString);
        //There should be no exception set. We only log an error msg for an exception case
        assertNull(result.getException());

        RecordedRequest first = server.takeRequest();
        assertEquals("GET /first HTTP/1.1", first.getRequestLine());

        RecordedRequest redirect = server.takeRequest();
        assertEquals("GET /new-path HTTP/1.1", redirect.getRequestLine());
    }
}