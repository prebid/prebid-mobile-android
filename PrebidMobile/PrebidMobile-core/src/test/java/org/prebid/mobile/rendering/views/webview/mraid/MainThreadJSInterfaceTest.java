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

package org.prebid.mobile.rendering.views.webview.mraid;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.robolectric.Shadows.shadowOf;

import android.os.Looper;
import android.webkit.JavascriptInterface;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
@LooperMode(LooperMode.Mode.PAUSED)
public class MainThreadJSInterfaceTest {

    private static final long LONG_TIMEOUT_MS = 10_000;

    private BaseJSInterface mockDelegate;
    private JsExecutor mockJsExecutor;

    @Before
    public void setUp() {
        mockDelegate = mock(BaseJSInterface.class);
        mockJsExecutor = mock(JsExecutor.class);
        when(mockDelegate.getJsExecutor()).thenReturn(mockJsExecutor);
    }

    @Test
    public void everyBaseJSInterfaceMethod_IsExposedToJavaScript() throws Exception {
        int exposedMethods = 0;
        for (Method method : BaseJSInterface.class.getMethods()) {
            if (method.isAnnotationPresent(JavascriptInterface.class)) {
                Method bridgeMethod = MainThreadJSInterface.class.getMethod(method.getName(), method.getParameterTypes());
                assertTrue(method.getName(), bridgeMethod.isAnnotationPresent(JavascriptInterface.class));
                exposedMethods++;
            }
        }
        assertTrue(exposedMethods > 0);
    }

    @Test
    public void getterOnMainThread_CallsDelegateDirectly() {
        when(mockDelegate.getCurrentPosition()).thenReturn("{\"x\":1}");
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate);

        // The main looper is paused, so waiting for a posted call would time out and return "{}"
        assertEquals("{\"x\":1}", jsInterface.getCurrentPosition());
    }

    @Test
    public void getterFromBackgroundThread_RunsDelegateOnMainThread() throws Exception {
        AtomicReference<Looper> delegateLooper = new AtomicReference<>();
        when(mockDelegate.getCurrentPosition()).thenAnswer(invocation -> {
            delegateLooper.set(Looper.myLooper());
            return "{\"x\":1}";
        });
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate, LONG_TIMEOUT_MS);

        FutureTask<String> jsCall = runOnBackgroundThread(jsInterface::getCurrentPosition);

        assertEquals("{\"x\":1}", awaitWhileIdlingMainLooper(jsCall));
        assertEquals(Looper.getMainLooper(), delegateLooper.get());
    }

    @Test
    public void getterFromBackgroundThread_WhenMainThreadDoesNotRespond_ReturnsFallback() throws Exception {
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate, 50);

        FutureTask<String> maxSizeCall = runOnBackgroundThread(jsInterface::getMaxSize);
        FutureTask<String> locationCall = runOnBackgroundThread(jsInterface::getLocation);

        assertEquals("{}", maxSizeCall.get(LONG_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        assertEquals(JSInterface.LOCATION_ERROR, locationCall.get(LONG_TIMEOUT_MS, TimeUnit.MILLISECONDS));

        shadowOf(Looper.getMainLooper()).idle();

        verify(mockDelegate, never()).getMaxSize();
        verify(mockDelegate, never()).getLocation();
    }

    @Test
    public void placementTypeAndSupports_AreAnsweredOnCallingThread() throws Exception {
        when(mockDelegate.getPlacementType()).thenReturn("inline");
        when(mockDelegate.supports("sms")).thenReturn(true);
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate);

        assertEquals("inline", runOnBackgroundThread(jsInterface::getPlacementType).get(LONG_TIMEOUT_MS, TimeUnit.MILLISECONDS));
        assertTrue(runOnBackgroundThread(() -> jsInterface.supports("sms")).get(LONG_TIMEOUT_MS, TimeUnit.MILLISECONDS));
    }

    @Test
    public void commandsFromBackgroundThread_RunOnMainThreadInOrder() throws Exception {
        AtomicReference<Looper> delegateLooper = new AtomicReference<>();
        doAnswer(invocation -> {
            delegateLooper.set(Looper.myLooper());
            return null;
        }).when(mockDelegate).open("url");
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate);

        runOnBackgroundThread(() -> {
            jsInterface.open("url");
            jsInterface.close();
            return null;
        }).get(LONG_TIMEOUT_MS, TimeUnit.MILLISECONDS);

        verify(mockDelegate, never()).open("url");

        shadowOf(Looper.getMainLooper()).idle();

        InOrder inOrder = inOrder(mockDelegate);
        inOrder.verify(mockDelegate).open("url");
        inOrder.verify(mockDelegate).close();
        assertEquals(Looper.getMainLooper(), delegateLooper.get());
    }

    @Test
    public void commandThatThrows_CompletesNativeCallInsteadOfCrashing() {
        doThrow(new IllegalStateException("test")).when(mockDelegate).open("url");
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate);

        jsInterface.open("url");
        shadowOf(Looper.getMainLooper()).idle();

        verify(mockJsExecutor).executeNativeCallComplete();
    }

    @Test
    public void javaScriptCallbackThatThrows_DoesNotCompleteNativeCall() {
        doThrow(new IllegalStateException("test")).when(mockDelegate).javaScriptCallback("hash", "method", "value");
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate);

        jsInterface.javaScriptCallback("hash", "method", "value");
        shadowOf(Looper.getMainLooper()).idle();

        verify(mockJsExecutor, never()).executeNativeCallComplete();
    }

    @Test
    public void commandQueuedBeforeDestroy_IsIgnoredAfterDestroy() {
        MainThreadJSInterface jsInterface = new MainThreadJSInterface(mockDelegate);

        jsInterface.close();
        when(mockDelegate.isDestroyed()).thenReturn(true);
        shadowOf(Looper.getMainLooper()).idle();

        verify(mockDelegate, never()).close();
        assertEquals("{}", jsInterface.getCurrentPosition());
        verify(mockDelegate, never()).getCurrentPosition();
    }

    private static <T> FutureTask<T> runOnBackgroundThread(Callable<T> call) {
        FutureTask<T> task = new FutureTask<>(call);
        new Thread(task, "JavaBridge").start();
        return task;
    }

    private static <T> T awaitWhileIdlingMainLooper(FutureTask<T> task) throws Exception {
        for (int attempt = 0; attempt < 2_000 && !task.isDone(); attempt++) {
            shadowOf(Looper.getMainLooper()).idle();
            Thread.sleep(5);
        }
        return task.get(0, TimeUnit.MILLISECONDS);
    }
}
