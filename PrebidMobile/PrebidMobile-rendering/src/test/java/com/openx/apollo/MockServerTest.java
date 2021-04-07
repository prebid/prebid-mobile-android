package com.openx.apollo;

import com.openx.apollo.networking.BaseNetworkTask;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

import static org.junit.Assert.assertEquals;

public class MockServerTest {

    private MockWebServer mServer;

    @Before
    public void setUp() throws Exception {
        mServer = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
    }

    @Test
    public void redirectResponseCodeTest() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(302));
        mServer.enqueue(new MockResponse().setResponseCode(307));
        mServer.start();

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.name = "foo";
        params.requestType = "GET";
        params.userAgent = "user-agent";

        HttpUrl baseUrl = mServer.url("/test");
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
