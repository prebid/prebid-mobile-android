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
