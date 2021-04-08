package org.prebid.mobile.rendering.mraid.methods;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
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
