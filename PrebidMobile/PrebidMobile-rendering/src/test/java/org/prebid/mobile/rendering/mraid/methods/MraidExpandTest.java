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

import android.app.Activity;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.mraid.methods.network.RedirectUrlListener;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidExpandTest {

    private MraidExpand mMraidExpand;

    private Activity mTestActivity;
    private WebViewBase mMockWebViewBase;
    private BaseJSInterface mSpyBaseJsInterface;
    private JsExecutor mSpyJsExecutor;

    @Before
    public void setup() {
        mTestActivity = Robolectric.buildActivity(Activity.class)
                                   .setup()
                                   .create()
                                   .visible()
                                   .resume()
                                   .windowFocusChanged(true)
                                   .get();
        mSpyJsExecutor = spy(new JsExecutor(mMockWebViewBase, null, null));

        mMockWebViewBase = mock(WebViewBase.class);
        mSpyBaseJsInterface = Mockito.spy(new BaseJSInterface(mTestActivity, mMockWebViewBase, mSpyJsExecutor));

        when(mSpyBaseJsInterface.getJsExecutor()).thenReturn(mSpyJsExecutor);
        when(mMockWebViewBase.getMRAIDInterface()).thenReturn(mSpyBaseJsInterface);

        mMraidExpand = new MraidExpand(mTestActivity, mMockWebViewBase, mock(InterstitialManager.class));
    }

    @Test
    public void expandTest() {

        doAnswer(invocation -> {
            RedirectUrlListener listener = invocation.getArgument(1);
            listener.onSuccess("test", "html");
            return null;
        }).when(mSpyBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));

        PrebidWebViewBase mockPreloadedListener = mock(PrebidWebViewBase.class);
        HTMLCreative mockCreative = mock(HTMLCreative.class);

        when(mockPreloadedListener.getCreative()).thenReturn(mockCreative);
        when(mMockWebViewBase.getPreloadedListener()).thenReturn(mockPreloadedListener);

        CompletedCallBack callBack = mock(CompletedCallBack.class);
        final MraidVariableContainer mraidVariableContainer = mSpyBaseJsInterface.getMraidVariableContainer();
        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);

        MraidExpand spyExpand = spy(mMraidExpand);

        doAnswer(invocation -> {
            CompletedCallBack completedCallBack = invocation.getArgument(1);
            completedCallBack.expandDialogShown();
            return null;
        }).when(spyExpand).showExpandDialog(any(), any());

        spyExpand.expand("test", callBack);

        verify(mSpyBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));
        assertEquals(mraidVariableContainer.getUrlForLaunching(), "test");
        verify(callBack).expandDialogShown();
        verify(spyExpand).showExpandDialog(any(), any());
    }

    @Test
    public void nullifyDialogTest() throws IllegalAccessException {
        AdBaseDialog mockDialog = mock(AdBaseDialog.class);
        WhiteBox.field(MraidExpand.class, "mExpandedDialog").set(mMraidExpand, mockDialog);

        mMraidExpand.nullifyDialog();

        verify(mockDialog).cleanup();
        verify(mockDialog).cancel();
    }

    @Test
    public void setMraidExpandedTest() {
        mMraidExpand.setMraidExpanded(true);
        assertTrue(mMraidExpand.isMraidExpanded());
    }
}
