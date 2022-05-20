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

import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.webkit.WebView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.utils.helpers.HandlerQueueManager;
import org.robolectric.RobolectricTestRunner;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
public class JsExecutorTest {
    private static final String TEST_SCRIPT = "test";
    private JsExecutor spyJsExecutor;

    @Mock WebView mockWebView;
    @Mock Handler mockHandler;
    @Mock HandlerQueueManager mockHandlerQueueManager;

    private final MraidVariableContainer mraidVariableContainer = new MraidVariableContainer();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final JsExecutor jsExecutor = new JsExecutor(mockWebView, mockHandler, mockHandlerQueueManager);
        jsExecutor.setMraidVariableContainer(mraidVariableContainer);

        spyJsExecutor = spy(jsExecutor);
    }

    @Test
    public void whenExecuteGetResizeProperties_EvaluateJavaScriptMethodWithResult() {
        Handler handler = mock(Handler.class);

        spyJsExecutor.executeGetResizeProperties(handler);

        verify(spyJsExecutor).evaluateJavaScriptMethodWithResult(eq("getResizeProperties"),
                                                                  eq(handler));
    }

    @Test
    public void whenExecuteGetExpandProperties_EvaluateJavaScriptMethodWithResult() {
        Handler handler = mock(Handler.class);

        spyJsExecutor.executeGetExpandProperties(handler);

        verify(spyJsExecutor).evaluateJavaScriptMethodWithResult(eq("getExpandProperties"),
                                                                  eq(handler));
    }

    @Test
    public void whenExecuteSetScreenSize_EvaluateJavaScript() {
        Rect mockRect = getMockRect(101, 102);

        spyJsExecutor.executeSetScreenSize(mockRect);

        verify(spyJsExecutor).evaluateJavaScript(eq("mraid.setScreenSize(101, 102);"));
    }

    @Test
    public void whenExecuteMaxSize_EvaluateJavaScript() {
        Rect mockRect = getMockRect(103, 106);

        spyJsExecutor.executeSetMaxSize(mockRect);

        verify(spyJsExecutor).evaluateJavaScript(eq("mraid.setMaxSize(103, 106);"));
    }

    @Test
    public void whenExecuteSetCurrentPosition_EvaluateJavaScript() {
        Rect mockRect = getMockRect(105, 101);

        spyJsExecutor.executeSetCurrentPosition(mockRect);

        verify(spyJsExecutor).evaluateJavaScript(eq("mraid.setCurrentPosition(0, 0, 105, 101);"));
    }

    @Test
    public void whenExecuteSetDefaultPosition_EvaluateJavaScript() {
        Rect mockRect = getMockRect(109, 101);

        spyJsExecutor.executeSetDefaultPosition(mockRect);

        verify(spyJsExecutor).evaluateJavaScript(eq("mraid.setDefaultPosition(0, 0, 109, 101);"));
    }

    @Test
    public void whenExecuteOnSizeChange_EvaluateJavaScript() {
        Rect mockRect = getMockRect(109, 102);

        spyJsExecutor.executeOnSizeChange(mockRect);

        spyJsExecutor.evaluateJavaScript(eq("mraid.onSizeChange(109, 102);"));
    }

    @Test
    public void whenExecuteOnViewableChange_EvaluateJavaScript() {
        spyJsExecutor.executeOnViewableChange(false);
        assertFalse(mraidVariableContainer.getCurrentViewable());
        verify(spyJsExecutor).evaluateJavaScript("mraid.onViewableChange(false);");

        spyJsExecutor.executeOnViewableChange(true);
        assertTrue(mraidVariableContainer.getCurrentViewable());
        verify(spyJsExecutor).evaluateJavaScript("mraid.onViewableChange(true);");
    }

    @Test
    public void whenExecuteAudioVolumeChange_EvaluateMraidScript() {
        spyJsExecutor.executeAudioVolumeChange(100f);
        verify(spyJsExecutor).evaluateMraidScript(eq("mraid.onAudioVolumeChange(100.0);"));

        spyJsExecutor.executeAudioVolumeChange(null);
        verify(spyJsExecutor).evaluateMraidScript(eq("mraid.onAudioVolumeChange(null);"));
    }

    @Test
    public void whenExecuteExposureChange_EvaluateMraidScript() {
        ViewExposure viewExposure = new ViewExposure();
        String expectedString = String.format("mraid.onExposureChange('%1$s');", viewExposure.toString());

        spyJsExecutor.executeExposureChange(viewExposure);

        assertEquals(viewExposure.toString(), mraidVariableContainer.getCurrentExposure());
        verify(spyJsExecutor).evaluateMraidScript(expectedString);
    }

    @Test
    public void whenExecuteOnError_EvaluateJavaScript() {
        spyJsExecutor.executeOnError("message", "action");
        verify(spyJsExecutor).evaluateJavaScript(eq("mraid.onError('message', 'action');"));
    }

    @Test
    public void whenExecuteDisabledFlags_EvaluateMraidScript() {
        spyJsExecutor.executeDisabledFlags(TEST_SCRIPT);
        verify(spyJsExecutor).evaluateMraidScript(TEST_SCRIPT);
    }

    @Test
    public void whenExecuteOnReadyExpanded_EvaluateMraidScript() {
        spyJsExecutor.executeOnReadyExpanded();
        assertEquals(JSInterface.STATE_EXPANDED, mraidVariableContainer.getCurrentState());
        verify(spyJsExecutor).evaluateMraidScript("mraid.onReadyExpanded();");
    }

    @Test
    public void whenExecuteOnReady_EvaluateMraidScript() {
        spyJsExecutor.executeOnReady();
        assertEquals(JSInterface.STATE_DEFAULT, mraidVariableContainer.getCurrentState());
        verify(spyJsExecutor).evaluateMraidScript("mraid.onReady();");
    }

    @Test
    public void whenExecuteSingleOnStateChange_EvaluateJavaScriptOnce() {
        spyJsExecutor.executeStateChange(JSInterface.STATE_EXPANDED);

        assertEquals(JSInterface.STATE_EXPANDED, mraidVariableContainer.getCurrentState());
        verify(spyJsExecutor, times(1))
            .evaluateMraidScript("mraid.onStateChange('" + JSInterface.STATE_EXPANDED + "');");
    }

    @Test
    public void whenExecuteMultipleSameStateChange_EvaluateJavaScriptOnce() {
        spyJsExecutor.executeStateChange(JSInterface.STATE_EXPANDED);
        spyJsExecutor.executeStateChange(JSInterface.STATE_EXPANDED);

        assertEquals(JSInterface.STATE_EXPANDED, mraidVariableContainer.getCurrentState());
        verify(spyJsExecutor, times(1))
            .evaluateMraidScript("mraid.onStateChange('" + JSInterface.STATE_EXPANDED + "');");
    }

    @Test
    public void whenExcuteMultipleSameOnViewableStateChange_EvaluateJavaScriptOnce() {
        spyJsExecutor.executeOnViewableChange(true);
        spyJsExecutor.executeOnViewableChange(true);

        verify(spyJsExecutor, times(1)).evaluateJavaScript("mraid.onViewableChange(true);");
    }

    @Test
    public void whenExecuteNativeCallComplete_EvaluateMraidScript() {
        spyJsExecutor.executeNativeCallComplete();

        verify(spyJsExecutor).evaluateMraidScript("mraid.nativeCallComplete();");
    }

    @Test
    public void whenExecuteLoading_ChangeStateVariable() {
        spyJsExecutor.loading();
        assertEquals(spyJsExecutor.getCurrentState(), JSInterface.STATE_LOADING);
    }

    @Test
    public void whenEvaluateJavaScriptNullWebView_DoNothing() {
        JsExecutor jsExecutor = spy(new JsExecutor(null, mockHandler, null));

        jsExecutor.evaluateJavaScript(TEST_SCRIPT);

        verifyNoMoreInteractions(mockHandler);
    }

    @Test
    public void whenEvaluateJavaScriptValidWebView_PostScriptLoadingRunnableOnHandler() {
        spyJsExecutor.evaluateJavaScript(TEST_SCRIPT);

        verify(mockHandler).post(any(JsExecutor.EvaluateScriptRunnable.class));
    }

    @Test
    public void whenSingleEvaluateJavaScriptMethodWithResult_DispatchHandlerMessage() {
        spyJsExecutor.evaluateJavaScriptMethodWithResult(TEST_SCRIPT, mockHandler);

        verify(mockHandler).dispatchMessage(any(Message.class));
    }

    @Test
    public void whenIsMraidEvaluateJavaScriptMethodWithResult_Evaluate() {
        spyJsExecutor.evaluateJavaScriptMethodWithResult(TEST_SCRIPT, mockHandler);

        verify(mockHandler).dispatchMessage(any(Message.class));
    }

    @Test
    public void whenEvaluateMraidScriptWithNullWebView_NoInteractions() {
        JsExecutor jsExecutor = new JsExecutor(null, mockHandler, null);
        jsExecutor.evaluateMraidScript(TEST_SCRIPT);

        verify(mockHandler, times(0)).post(any(JsExecutor.EvaluateScriptRunnable.class));
    }

    @Test
    public void whenEvaluateMraidScript_postScriptRunnable() {
        spyJsExecutor.evaluateMraidScript(TEST_SCRIPT);

        verify(mockHandler, times(1)).post(any(JsExecutor.EvaluateScriptRunnable.class));
    }

    private Rect getMockRect(int width, int height) {
        Rect mockRect = mock(Rect.class);
        when(mockRect.width()).thenReturn(width);
        when(mockRect.height()).thenReturn(height);
        return mockRect;
    }
}
