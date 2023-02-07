package org.prebid.mobile.rendering.sdk;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.robolectric.Shadows.shadowOf;
import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Context;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class SdkInitializerTest {

    private boolean calledAlready = false;
    private Boolean isSuccessful;
    private Boolean serverWarning;
    private String error;

    private MockWebServer server;
    private Context context;

    @Before
    public void setUp() throws Exception {
        server = new MockWebServer();
        context = Robolectric.buildActivity(Activity.class).create().get();
        reset();
    }

    @After
    public void tearDown() throws IOException {
        server.shutdown();
        reset();
    }

    private void reset() {
        calledAlready = false;
        isSuccessful = null;
        error = null;
        serverWarning = null;
        PrebidMobile.setPrebidServerHost(Host.createCustomHost(""));
        Reflection.setStaticVariableTo(PrebidMobile.class, "customStatusEndpoint", null);
        PrebidContextHolder.clearContext();
    }


    @Test
    public void init_putNullContextAndNullListener_initializationFail() {
        SdkInitializer.init(null, null);

        assertFalse(PrebidMobile.isSdkInitialized());
    }

    @Test
    public void init_putNullContext_initializationFail() {
        SdkInitializer.init(null, createListener());

        assertFalse(isSuccessful);
        assertFalse(PrebidMobile.isSdkInitialized());
        assertEquals(error, "Context must be not null!");
    }

    @Test
    public void init_statusResponseIsOk_initializationIsSuccessful() throws IOException, InterruptedException {
        setStatusResponse(200, "Good");

        SdkInitializer.init(context, createListener());

        sleep(300);
        shadowOf(getMainLooper()).idle();
        sleep(200);

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertNull(error);
    }

    @Test
    public void init_statusResponseIsEmpty_initializationIsSuccessful() throws IOException, InterruptedException {
        setStatusResponse(204, "");

        SdkInitializer.init(context, createListener());

        sleep(300);
        shadowOf(getMainLooper()).idle();
        sleep(200);

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
    }

    @Test
    public void init_statusResponseIsBad_statusWarning() throws InterruptedException {
        setStatusResponse(404, "");

        SdkInitializer.init(context, createListener());

        sleep(300);
        shadowOf(getMainLooper()).idle();
        sleep(200);

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertEquals("Server status is not ok!", error);
        assertTrue(serverWarning);
    }


    @Test
    public void init_customStatusResponseIsOk_initializationIsSuccessful() throws IOException, InterruptedException {
        setCustomStatusResponse(200, "Good");

        SdkInitializer.init(context, createListener());

        sleep(300);
        shadowOf(getMainLooper()).idle();
        sleep(200);

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertNull(error);
    }

    @Test
    public void init_customStatusResponseIsEmpty_initializationIsSuccessful() throws IOException, InterruptedException {
        setCustomStatusResponse(204, "");

        SdkInitializer.init(context, createListener());

        sleep(300);
        shadowOf(getMainLooper()).idle();
        sleep(200);

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
    }

    @Test
    public void init_customStatusResponseIsBad_statusWarning() throws InterruptedException {
        setCustomStatusResponse(404, "");

        SdkInitializer.init(context, createListener());

        sleep(300);
        shadowOf(getMainLooper()).idle();
        sleep(200);

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertEquals("Server status is not ok!", error);
        assertTrue(serverWarning);
    }


    private SdkInitializationListener createListener() {
        return status -> {
            if (calledAlready) fail();

            if (status == InitializationStatus.SUCCEEDED) {
                isSuccessful = true;
            } else if (status == InitializationStatus.SERVER_STATUS_WARNING) {
                isSuccessful = true;
                serverWarning = true;
                error = status.getDescription();
            } else {
                isSuccessful = false;
                error = status.getDescription();
            }

            calledAlready = true;
        };
    }

    private void setStatusResponse(int code, String body) {
        String host = createStatusResponse(code, body).replace("/status", "/openrtb2/auction");
        PrebidMobile.setPrebidServerHost(Host.createCustomHost(host));
    }

    private void setCustomStatusResponse(int code, String body) {
        String url = createStatusResponse(code, body);
        PrebidMobileReflection.setCustomStatusEndpoint(url);
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