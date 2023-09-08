package org.prebid.mobile.rendering.sdk;

import static android.os.Looper.getMainLooper;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister.PREBID_MOBILE_RENDERER_NAME;
import static org.robolectric.Shadows.shadowOf;
import static java.lang.Thread.sleep;

import android.app.Activity;
import android.content.Context;

import org.hamcrest.MatcherAssert;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.prebid.mobile.Host;
import org.prebid.mobile.PrebidMobile;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.api.rendering.pluginrenderer.PrebidMobilePluginRegister;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.reflection.sdk.PrebidMobileReflection;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.HttpUrl;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;

@RunWith(RobolectricTestRunner.class)
public class SdkInitializerTest {

    private static final int TERMINATION_TIMEOUT = 10;

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
        Reflection.setStaticVariableTo(InitializationNotifier.class, "initializationInProgress", false);
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

        advanceBackgroundTasks();

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertNull(error);
    }

    @Test
    public void init_longStatusResponse_initializationIsFailed() throws IOException, InterruptedException {
        setStatusResponse(200, "Good");

        SdkInitializer.init(context, createListener());

        Thread.sleep(12_000);
        shadowOf(getMainLooper()).idle();

        assertFalse(isSuccessful);
        assertFalse(PrebidMobile.isSdkInitialized());
        assertEquals("Terminated by timeout.", error);
    }

    @Test
    public void init_statusResponseIsEmpty_initializationIsSuccessful() throws IOException, InterruptedException {
        setStatusResponse(204, "");

        SdkInitializer.init(context, createListener());

        advanceBackgroundTasks();

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
    }

    @Test
    public void init_statusResponseIsBad_statusWarning() throws InterruptedException {
        setStatusResponse(404, "");

        SdkInitializer.init(context, createListener());

        advanceBackgroundTasks();

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertEquals("Server status is not ok!", error);
        assertTrue(serverWarning);
    }


    @Test
    public void init_customStatusResponseIsOk_initializationIsSuccessful() throws IOException, InterruptedException {
        setCustomStatusResponse(200, "Good");

        SdkInitializer.init(context, createListener());

        advanceBackgroundTasks();

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertTrue(PrebidMobilePluginRegister.getInstance().containsPlugin(PREBID_MOBILE_RENDERER_NAME));
        assertNull(error);
    }

    @Test
    public void init_customStatusResponseIsEmpty_initializationIsSuccessful() throws IOException, InterruptedException {
        setCustomStatusResponse(204, "");

        SdkInitializer.init(context, createListener());

        advanceBackgroundTasks();

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
    }

    @Test
    public void init_customStatusResponseIsBad_statusWarning() throws InterruptedException {
        setCustomStatusResponse(404, "");

        SdkInitializer.init(context, createListener());

        advanceBackgroundTasks();

        assertTrue(isSuccessful);
        assertTrue(PrebidMobile.isSdkInitialized());
        assertEquals("Server status is not ok!", error);
        assertTrue(serverWarning);
    }


    @Test
    public void runBackgroundTasks_checkStartedTasks() throws InterruptedException {
        ExecutorService executorMock = mock(ExecutorService.class);
        SdkInitializer.runBackgroundTasks(mock(InitializationNotifier.class), executorMock);

        ArgumentCaptor<Callable> requesterCaptor = ArgumentCaptor.forClass(Callable.class);
        verify(executorMock, times(1)).submit(requesterCaptor.capture());
        String requesterClassName = requesterCaptor.getValue().getClass().toString();
        MatcherAssert.assertThat(requesterClassName, is(startsWith("class org.prebid.mobile.rendering.sdk.StatusRequester")));

        ArgumentCaptor<Runnable> tasksCaptor = ArgumentCaptor.forClass(Runnable.class);
        verify(executorMock, times(2)).execute(tasksCaptor.capture());

        List<Runnable> allTasks = tasksCaptor.getAllValues();
        String firstTaskName = allTasks.get(0).getClass().toString();
        MatcherAssert.assertThat(firstTaskName, is(startsWith("class org.prebid.mobile.rendering.sdk.SdkInitializer$UserConsentFetcherTask")));

        String secondTaskName = allTasks.get(1).getClass().toString();
        MatcherAssert.assertThat(secondTaskName, is(startsWith("class org.prebid.mobile.rendering.sdk.UserAgentFetcherTask")));

        verify(executorMock, times(1)).shutdown();
        verify(executorMock, times(1)).awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS);
    }

    @Test
    public void runBackgroundTasks_testTermination() throws InterruptedException {
        InitializationNotifier listenerMock = mock(InitializationNotifier.class);
        ExecutorService executorMock = mock(ExecutorService.class);

        when(executorMock.awaitTermination(10, TimeUnit.SECONDS)).thenReturn(false);

        SdkInitializer.runBackgroundTasks(listenerMock, executorMock);

        verify(listenerMock, times(1)).initializationFailed("Terminated by timeout.");
    }

    @Test
    public void runBackgroundTasks_testSuccess_successStatusRequest() throws InterruptedException {
        InitializationNotifier listenerMock = mock(InitializationNotifier.class);
        ExecutorService executorMock = mock(ExecutorService.class);
        Future statusRequesterMock = mock(Future.class);

        when(executorMock.submit(any(Callable.class))).thenReturn(statusRequesterMock);
        when(executorMock.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS)).thenReturn(true);

        SdkInitializer.runBackgroundTasks(listenerMock, executorMock);

        verify(listenerMock, times(1)).initializationCompleted(null);
    }

    @Test
    public void runBackgroundTasks_testSuccess_failedStatusRequest() throws InterruptedException, ExecutionException {
        InitializationNotifier listenerMock = mock(InitializationNotifier.class);
        ExecutorService executorMock = mock(ExecutorService.class);
        Future statusRequesterMock = mock(Future.class);
        when(statusRequesterMock.get()).thenReturn("Error");

        when(executorMock.submit(any(Callable.class))).thenReturn(statusRequesterMock);
        when(executorMock.awaitTermination(TERMINATION_TIMEOUT, TimeUnit.SECONDS)).thenReturn(true);

        SdkInitializer.runBackgroundTasks(listenerMock, executorMock);

        verify(listenerMock, times(1)).initializationCompleted("Error");
    }


    private void advanceBackgroundTasks() throws InterruptedException {
        shadowOf(getMainLooper()).idle();
        sleep(3_000);
        shadowOf(getMainLooper()).idle();
        sleep(1_000);
        shadowOf(getMainLooper()).idle();
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
        mockResponse.setBodyDelay(500, TimeUnit.MILLISECONDS);
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