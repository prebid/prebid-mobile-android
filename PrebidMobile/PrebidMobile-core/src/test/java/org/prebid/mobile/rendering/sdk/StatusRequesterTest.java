package org.prebid.mobile.rendering.sdk;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.reflection.Reflection;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

public class StatusRequesterTest {

    private MockWebServer server;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
        Reflection.setStaticVariableTo(PrebidMobile.class, "customStatusEndpoint", null);
    }


    @Test
    public void statusRequest_success() {
        setStatusResponse(200, "Good");

        String result = StatusRequester.makeRequest();

        assertNull(result);
    }

    @Test
    public void statusRequest_failed() {
        setStatusResponse(404, "");

        String result = StatusRequester.makeRequest();

        assertEquals("Server status is not ok!", result);
    }

    @Test
    public void statusRequest_withoutAuctionPartOfUrl() {
        PrebidMobile.setPrebidServerHost(Host.createCustomHost("qwerty123456.qwerty"));

        String result = StatusRequester.makeRequest();

        assertNull(result);
    }


    private void setStatusResponse(int code, String body) {
        String host = createStatusResponse(code, body).replace("/status", "/openrtb2/auction");
        PrebidMobile.setPrebidServerHost(Host.createCustomHost(host));
    }

    private String createStatusResponse(int code, String body) {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(code);
        mockResponse.setBody(body);
        mockResponse.setBodyDelay(1, TimeUnit.SECONDS);
        server.enqueue(mockResponse);

        try {
            server.start();
        } catch (IOException exception) {
            throw new NullPointerException(exception.getMessage());
        }


        HttpUrl url = server.url("/status");
        server.setProtocolNegotiationEnabled(false);
        return url.toString();
    }

}