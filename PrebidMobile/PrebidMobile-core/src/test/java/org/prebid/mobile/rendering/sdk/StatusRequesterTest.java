package org.prebid.mobile.rendering.sdk;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;
import static java.lang.Thread.sleep;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.reflection.Reflection;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
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
    public void statusRequest_success() throws InterruptedException {
        setStatusResponse(200, "Good");

        InitializationManager listener = mock(InitializationManager.class);
        StatusRequester.makeRequest(listener);

        sleep(300);
        shadowOf(getMainLooper()).idle();

        verify(listener, times(1)).statusRequesterTaskCompleted(null);
    }

    @Test
    public void statusRequest_failed() throws InterruptedException {
        setStatusResponse(404, "");

        InitializationManager listener = mock(InitializationManager.class);
        StatusRequester.makeRequest(listener);

        sleep(300);
        shadowOf(getMainLooper()).idle();

        verify(listener, times(1)).statusRequesterTaskCompleted("Server status is not ok!");
    }

    @Test
    public void statusRequest_withoutAuctionPartOfUrl() throws InterruptedException {
        PrebidMobile.setPrebidServerHost(Host.createCustomHost("qwerty123456.qwerty"));

        InitializationManager listener = mock(InitializationManager.class);
        StatusRequester.makeRequest(listener);

        sleep(300);
        shadowOf(getMainLooper()).idle();

        verify(listener, times(1)).statusRequesterTaskCompleted(null);
    }

    @Test
    public void statusRequest_longServerAnswer() throws InterruptedException {
        PrebidMobile.setCustomStatusEndpoint("qwerty123456.qwerty");

        InitializationManager listener = mock(InitializationManager.class);
        StatusRequester.makeRequest(listener);

        sleep(300);
        shadowOf(getMainLooper()).idle();

        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(listener, times(1)).statusRequesterTaskCompleted(argument.capture());

        assertTrue(argument.getValue().startsWith("Prebid Server is not responding"));
    }


    private void setStatusResponse(int code, String body) {
        String host = createStatusResponse(code, body).replace("/status", "/openrtb2/auction");
        PrebidMobile.setPrebidServerHost(Host.createCustomHost(host));
    }

    private String createStatusResponse(int code, String body) {
        MockResponse mockResponse = new MockResponse();
        mockResponse.setResponseCode(code);
        mockResponse.setBody(body);
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