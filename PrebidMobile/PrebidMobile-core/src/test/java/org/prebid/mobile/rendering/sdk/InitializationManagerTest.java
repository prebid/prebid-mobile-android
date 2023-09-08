package org.prebid.mobile.rendering.sdk;

import static android.os.Looper.getMainLooper;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.robolectric.Shadows.shadowOf;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.data.InitializationStatus;
import org.prebid.mobile.reflection.Reflection;
import org.prebid.mobile.rendering.listeners.SdkInitializationListener;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class InitializationManagerTest {

    @Mock
    private SdkInitializationListener listener;

    private InitializationNotifier subject;

    private AutoCloseable mocks;

    @Before
    public void setUp() throws Exception {
        mocks = MockitoAnnotations.openMocks(this);
        reset();
    }

    @After
    public void tearDown() throws Exception {
        reset();
        mocks.close();
    }

    private void reset() {
        Reflection.setStaticVariableTo(InitializationNotifier.class, "tasksCompletedSuccessfully", false);
        Reflection.setStaticVariableTo(InitializationNotifier.class, "initializationInProgress", false);
    }


    @Test
    public void defaultParams() {
        assertFalse(InitializationNotifier.isInitializationInProgress());
        assertFalse(InitializationNotifier.wereTasksCompletedSuccessfully());

        subject = new InitializationNotifier(listener);

        assertTrue(InitializationNotifier.isInitializationInProgress());
        verify(listener, never()).onInitializationComplete(any());
    }

    @Test
    public void checkAllTasksCompleted() {
        subject = new InitializationNotifier(listener);

        subject.initializationCompleted(null);
        verify(listener, times(1)).onInitializationComplete(any());

        assertFalse(InitializationNotifier.isInitializationInProgress());
        assertTrue(InitializationNotifier.wereTasksCompletedSuccessfully());
    }

    @Test
    public void checkInitializationFailed() {
        subject = new InitializationNotifier(listener);

        subject.initializationFailed("Error 101");
        shadowOf(getMainLooper()).idle();

        InitializationStatus status = InitializationStatus.FAILED;
        verify(listener, times(1)).onInitializationComplete(status);
        assertEquals("Error 101", status.getDescription());

        assertFalse(InitializationNotifier.isInitializationInProgress());
        assertFalse(InitializationNotifier.wereTasksCompletedSuccessfully());
    }

    @Test
    public void testDoubleCall() {
        subject = new InitializationNotifier(listener);

        subject.initializationFailed("error");
        subject.initializationFailed("error");
        shadowOf(getMainLooper()).idle();

        verify(listener, times(1)).onInitializationComplete(any());
    }

    @Test
    public void statusRequest_success() {
        subject = new InitializationNotifier(listener);

        subject.initializationCompleted(null);
        shadowOf(getMainLooper()).idle();

        verify(listener, times(1)).onInitializationComplete(InitializationStatus.SUCCEEDED);

        assertFalse(InitializationNotifier.isInitializationInProgress());
        assertTrue(InitializationNotifier.wereTasksCompletedSuccessfully());
    }

    @Test
    public void statusRequest_failed() {
        subject = new InitializationNotifier(listener);

        subject.initializationCompleted("Error");
        shadowOf(getMainLooper()).idle();

        verify(listener, times(1)).onInitializationComplete(InitializationStatus.SERVER_STATUS_WARNING);

        assertFalse(InitializationNotifier.isInitializationInProgress());
        assertTrue(InitializationNotifier.wereTasksCompletedSuccessfully());
    }

}