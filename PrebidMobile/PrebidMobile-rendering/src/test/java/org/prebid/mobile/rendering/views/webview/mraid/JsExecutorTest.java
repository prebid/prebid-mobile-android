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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
public class JsExecutorTest {
    private static final String TEST_SCRIPT = "test";
    private JsExecutor mSpyJsExecutor;

    @Mock WebView mMockWebView;
    @Mock Handler mMockHandler;
    @Mock HandlerQueueManager mMockHandlerQueueManager;

    private final MraidVariableContainer mMraidVariableContainer = new MraidVariableContainer();

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
        final JsExecutor jsExecutor = new JsExecutor(mMockWebView, mMockHandler, mMockHandlerQueueManager);
        jsExecutor.setMraidVariableContainer(mMraidVariableContainer);

        mSpyJsExecutor = spy(jsExecutor);
    }

    @Test
    public void whenExecuteGetResizeProperties_EvaluateJavaScriptMethodWithResult() {
        Handler handler = mock(Handler.class);

        mSpyJsExecutor.executeGetResizeProperties(handler);

        verify(mSpyJsExecutor).evaluateJavaScriptMethodWithResult(eq("getResizeProperties"),
                                                                  eq(handler));
    }

    @Test
    public void whenExecuteGetExpandProperties_EvaluateJavaScriptMethodWithResult() {
        Handler handler = mock(Handler.class);

        mSpyJsExecutor.executeGetExpandProperties(handler);

        verify(mSpyJsExecutor).evaluateJavaScriptMethodWithResult(eq("getExpandProperties"),
                                                                  eq(handler));
    }

    @Test
    public void whenExecuteSetScreenSize_EvaluateJavaScript() {
        Rect mockRect = getMockRect(101, 102);

        mSpyJsExecutor.executeSetScreenSize(mockRect);

        verify(mSpyJsExecutor).evaluateJavaScript(eq("mraid.setScreenSize(101, 102);"));
    }

    @Test
    public void whenExecuteMaxSize_EvaluateJavaScript() {
        Rect mockRect = getMockRect(103, 106);

        mSpyJsExecutor.executeSetMaxSize(mockRect);

        verify(mSpyJsExecutor).evaluateJavaScript(eq("mraid.setMaxSize(103, 106);"));
    }

    @Test
    public void whenExecuteSetCurrentPosition_EvaluateJavaScript() {
        Rect mockRect = getMockRect(105, 101);

        mSpyJsExecutor.executeSetCurrentPosition(mockRect);

        verify(mSpyJsExecutor).evaluateJavaScript(eq("mraid.setCurrentPosition(0, 0, 105, 101);"));
    }

    @Test
    public void whenExecuteSetDefaultPosition_EvaluateJavaScript() {
        Rect mockRect = getMockRect(109, 101);

        mSpyJsExecutor.executeSetDefaultPosition(mockRect);

        verify(mSpyJsExecutor).evaluateJavaScript(eq("mraid.setDefaultPosition(0, 0, 109, 101);"));
    }

    @Test
    public void whenExecuteOnSizeChange_EvaluateJavaScript() {
        Rect mockRect = getMockRect(109, 102);

        mSpyJsExecutor.executeOnSizeChange(mockRect);

        mSpyJsExecutor.evaluateJavaScript(eq("mraid.onSizeChange(109, 102);"));
    }

    @Test
    public void whenExecuteOnViewableChange_EvaluateJavaScript() {
        mSpyJsExecutor.executeOnViewableChange(false);
        assertFalse(mMraidVariableContainer.getCurrentViewable());
        verify(mSpyJsExecutor).evaluateJavaScript("mraid.onViewableChange(false);");

        mSpyJsExecutor.executeOnViewableChange(true);
        assertTrue(mMraidVariableContainer.getCurrentViewable());
        verify(mSpyJsExecutor).evaluateJavaScript("mraid.onViewableChange(true);");
    }

    @Test
    public void whenExecuteAudioVolumeChange_EvaluateMraidScript() {
        mSpyJsExecutor.executeAudioVolumeChange(100f);
        verify(mSpyJsExecutor).evaluateMraidScript(eq("mraid.onAudioVolumeChange(100.0);"));

        mSpyJsExecutor.executeAudioVolumeChange(null);
        verify(mSpyJsExecutor).evaluateMraidScript(eq("mraid.onAudioVolumeChange(null);"));
    }

    @Test
    public void whenExecuteExposureChange_EvaluateMraidScript() {
        ViewExposure viewExposure = new ViewExposure();
        String expectedString = String.format("mraid.onExposureChange('%1$s');", viewExposure.toString());

        mSpyJsExecutor.executeExposureChange(viewExposure);

        assertEquals(viewExposure.toString(), mMraidVariableContainer.getCurrentExposure());
        verify(mSpyJsExecutor).evaluateMraidScript(expectedString);
    }

    @Test
    public void whenExecuteOnError_EvaluateJavaScript() {
        mSpyJsExecutor.executeOnError("message", "action");
        verify(mSpyJsExecutor).evaluateJavaScript(eq("mraid.onError('message', 'action');"));
    }

    @Test
    public void whenExecuteDisabledFlags_EvaluateMraidScript() {
        mSpyJsExecutor.executeDisabledFlags(TEST_SCRIPT);
        verify(mSpyJsExecutor).evaluateMraidScript(TEST_SCRIPT);
    }

    @Test
    public void whenExecuteOnReadyExpanded_EvaluateMraidScript() {
        mSpyJsExecutor.executeOnReadyExpanded();
        assertEquals(JSInterface.STATE_EXPANDED, mMraidVariableContainer.getCurrentState());
        verify(mSpyJsExecutor).evaluateMraidScript("mraid.onReadyExpanded();");
    }

    @Test
    public void whenExecuteOnReady_EvaluateMraidScript() {
        mSpyJsExecutor.executeOnReady();
        assertEquals(JSInterface.STATE_DEFAULT, mMraidVariableContainer.getCurrentState());
        verify(mSpyJsExecutor).evaluateMraidScript("mraid.onReady();");
    }

    @Test
    public void whenExecuteSingleOnStateChange_EvaluateJavaScriptOnce() {
        mSpyJsExecutor.executeStateChange(JSInterface.STATE_EXPANDED);

        assertEquals(JSInterface.STATE_EXPANDED, mMraidVariableContainer.getCurrentState());
        verify(mSpyJsExecutor, times(1))
            .evaluateMraidScript("mraid.onStateChange('" + JSInterface.STATE_EXPANDED + "');");
    }

    @Test
    public void whenExecuteMultipleSameStateChange_EvaluateJavaScriptOnce() {
        mSpyJsExecutor.executeStateChange(JSInterface.STATE_EXPANDED);
        mSpyJsExecutor.executeStateChange(JSInterface.STATE_EXPANDED);

        assertEquals(JSInterface.STATE_EXPANDED, mMraidVariableContainer.getCurrentState());
        verify(mSpyJsExecutor, times(1))
            .evaluateMraidScript("mraid.onStateChange('" + JSInterface.STATE_EXPANDED + "');");
    }

    @Test
    public void whenExcuteMultipleSameOnViewableStateChange_EvaluateJavaScriptOnce() {
        mSpyJsExecutor.executeOnViewableChange(true);
        mSpyJsExecutor.executeOnViewableChange(true);

        verify(mSpyJsExecutor, times(1)).evaluateJavaScript("mraid.onViewableChange(true);");
    }

    @Test
    public void whenExecuteNativeCallComplete_EvaluateMraidScript() {
        mSpyJsExecutor.executeNativeCallComplete();

        verify(mSpyJsExecutor).evaluateMraidScript("mraid.nativeCallComplete();");
    }

    @Test
    public void whenExecuteLoading_ChangeStateVariable() {
        mSpyJsExecutor.loading();
        assertEquals(mSpyJsExecutor.getCurrentState(), JSInterface.STATE_LOADING);
    }

    @Test
    public void whenEvaluateJavaScriptNullWebView_DoNothing() {
        JsExecutor jsExecutor = spy(new JsExecutor(null, mMockHandler, null));

        jsExecutor.evaluateJavaScript(TEST_SCRIPT);

        verifyNoMoreInteractions(mMockHandler);
    }

    @Test
    public void whenEvaluateJavaScriptValidWebView_PostScriptLoadingRunnableOnHandler() {
        mSpyJsExecutor.evaluateJavaScript(TEST_SCRIPT);

        verify(mMockHandler).post(any(JsExecutor.EvaluateScriptRunnable.class));
    }

    @Test
    public void whenSingleEvaluateJavaScriptMethodWithResult_DispatchHandlerMessage() {
        mSpyJsExecutor.evaluateJavaScriptMethodWithResult(TEST_SCRIPT, mMockHandler);

        verify(mMockHandler).dispatchMessage(any(Message.class));
    }

    @Test
    public void whenIsMraidEvaluateJavaScriptMethodWithResult_Evaluate() {
        mSpyJsExecutor.evaluateJavaScriptMethodWithResult(TEST_SCRIPT, mMockHandler);

        verify(mMockHandler).dispatchMessage(any(Message.class));
    }

    @Test
    public void whenEvaluateMraidScriptWithNullWebView_NoInteractions() {
        JsExecutor jsExecutor = new JsExecutor(null, mMockHandler, null);
        jsExecutor.evaluateMraidScript(TEST_SCRIPT);

        verify(mMockHandler, times(0)).post(any(JsExecutor.EvaluateScriptRunnable.class));
    }

    @Test
    public void whenEvaluateMraidScript_postScriptRunnable() {
        mSpyJsExecutor.evaluateMraidScript(TEST_SCRIPT);

        verify(mMockHandler, times(1)).post(any(JsExecutor.EvaluateScriptRunnable.class));
    }

    private Rect getMockRect(int width, int height) {
        Rect mockRect = mock(Rect.class);
        when(mockRect.width()).thenReturn(width);
        when(mockRect.height()).thenReturn(height);
        return mockRect;
    }
}
