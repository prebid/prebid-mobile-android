package com.openx.apollo.mraid.methods;

import com.openx.apollo.models.HTMLCreative;
import com.openx.apollo.models.internal.MraidEvent;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.JsExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidEventHandlerNotifierRunnableTest {
    private MraidEventHandlerNotifierRunnable mMraidEventHandlerNotifierRunnable;

    @Mock
    HTMLCreative mMockHTMLCreative;
    @Mock
    WebViewBase mMockWebViewBase;
    @Mock
    JsExecutor mMockJsExecutor;
    @Mock
    MraidEvent mMockMraidEvent;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mMraidEventHandlerNotifierRunnable = new MraidEventHandlerNotifierRunnable(mMockHTMLCreative,
                                                                                   mMockWebViewBase,
                                                                                   mMockMraidEvent,
                                                                                   mMockJsExecutor);
    }

    @Test
    public void runWithValidHtmlCreativeAndWebViewBase_HandleMraidEventsInCreative() {
        mMraidEventHandlerNotifierRunnable.run();

        verify(mMockHTMLCreative).handleMRAIDEventsInCreative(mMockMraidEvent, mMockWebViewBase);
    }

    @Test
    public void runWithInValidHtmlCreativeOrWebViewBase_NoInteractions() {
        MraidEventHandlerNotifierRunnable mraidEventHandlerNotifierRunnable =
            new MraidEventHandlerNotifierRunnable(null, mMockWebViewBase,
                                                  mMockMraidEvent, mMockJsExecutor);
        mraidEventHandlerNotifierRunnable.run();

        verifyZeroInteractions(mMockHTMLCreative);
        verifyZeroInteractions(mMockJsExecutor);
    }

    @Test
    public void runWithValidHtmlCreativeAndWebViewBase_ExecuteNativeCallComplete() {
        mMraidEventHandlerNotifierRunnable.run();

        verify(mMockJsExecutor).executeNativeCallComplete();
    }
}
