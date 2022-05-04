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

package org.prebid.mobile.rendering;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;

import static org.junit.Assert.assertEquals;

public class MockServerTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        server.shutdown();
    }

    @Test
    public void redirectResponseCodeTest() throws Exception {
        server.enqueue(new MockResponse().setResponseCode(302));
        server.enqueue(new MockResponse().setResponseCode(307));
        server.start();

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.name = "foo";
        params.requestType = "GET";
        params.userAgent = "user-agent";

        HttpUrl baseUrl = server.url("/test");
        params.url = baseUrl.url().toString();

        // HTTP 302
        try {
            BaseNetworkTask baseNetworkTask = new BaseNetworkTask(null);
            baseNetworkTask.sendRequest(params);
        }
        catch (Exception e) {
            assertEquals("Bad server response - [HTTP Response code of 302]", e.getMessage());
        }

        // HTTP 307
        try {
            BaseNetworkTask baseNetworkTask = new BaseNetworkTask(null);
            baseNetworkTask.sendRequest(params);
        }
        catch (Exception e) {
            assertEquals("Bad server response - [HTTP Response code of 307]", e.getMessage());
        }
    }
}
