package org.prebid.mobile.rendering.networking.tracking;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.rendering.networking.BaseNetworkTask;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNull;

public class ImpressionUrlTaskTest {

    private MockWebServer mServer;

    @Before
    public void setUp() throws Exception {
        mServer = new MockWebServer();
    }

    @After
    public void tearDown() throws Exception {
        mServer.shutdown();
    }

    //@Ignore
    @Test
    public void redirectTest() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(302).addHeader("Location: " + mServer.url("/new-path"))
                                          .setBody("This page has moved!"));

        mServer.enqueue(new MockResponse().setResponseCode(200).setBody("This is the new location!"));

        ImpressionUrlTask impressionUrlTask = new ImpressionUrlTask(null);

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.requestType = "GET";
        params.userAgent = "user-agent";
        //get server url
        HttpUrl baseUrl = mServer.url("/first");
        //give server url to SDK network class
        params.url = baseUrl.url().toString();
        params.name = BaseNetworkTask.REDIRECT_TASK;

        //send SDK request
        BaseNetworkTask.GetUrlResult result = impressionUrlTask.sendRequest(params);
        //validate
        assertEquals("This is the new location!", result.responseString);

        RecordedRequest first = mServer.takeRequest();
        assertEquals("GET /first HTTP/1.1", first.getRequestLine());

        RecordedRequest redirect = mServer.takeRequest();
        assertEquals("GET /new-path HTTP/1.1", redirect.getRequestLine());
    }

    @Test
    public void redirectWithThrowExceptionTest() throws Exception {
        mServer.enqueue(new MockResponse().setResponseCode(302).addHeader("Location: " + mServer.url("/new-path"))
                                          .setBody("This page has moved!"));

        mServer.enqueue(new MockResponse().setResponseCode(400).setBody("Bad server response. Should throw exception"));

        ImpressionUrlTask impressionUrlTask = new ImpressionUrlTask(null);

        BaseNetworkTask.GetUrlParams params = new BaseNetworkTask.GetUrlParams();
        params.requestType = "GET";
        params.userAgent = "user-agent";
        //get server url
        HttpUrl baseUrl = mServer.url("/first");
        //give server url to SDK network class
        params.url = baseUrl.url().toString();
        params.name = BaseNetworkTask.REDIRECT_TASK;

        //send SDK request
        BaseNetworkTask.GetUrlResult result = impressionUrlTask.sendRequest(params);
        //validate. No response as an exception should have been thrown for code 400
        assertNull(result.responseString);
        //There should be no exception set. We only log an error msg for an exception case
        assertNull(result.getException());

        RecordedRequest first = mServer.takeRequest();
        assertEquals("GET /first HTTP/1.1", first.getRequestLine());

        RecordedRequest redirect = mServer.takeRequest();
        assertEquals("GET /new-path HTTP/1.1", redirect.getRequestLine());
    }
}