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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidExpandTest {

    private MraidExpand mraidExpand;

    private Activity testActivity;
    private WebViewBase mockWebViewBase;
    private BaseJSInterface spyBaseJsInterface;
    private JsExecutor spyJsExecutor;

    @Before
    public void setup() {
        testActivity = Robolectric.buildActivity(Activity.class)
                                  .setup()
                                  .create()
                                  .visible()
                                  .resume()
                                  .windowFocusChanged(true)
                                  .get();
        spyJsExecutor = spy(new JsExecutor(mockWebViewBase, null, null));

        mockWebViewBase = mock(WebViewBase.class);
        spyBaseJsInterface = Mockito.spy(new BaseJSInterface(testActivity, mockWebViewBase, spyJsExecutor));

        when(spyBaseJsInterface.getJsExecutor()).thenReturn(spyJsExecutor);
        when(mockWebViewBase.getMRAIDInterface()).thenReturn(spyBaseJsInterface);

        mraidExpand = new MraidExpand(testActivity, mockWebViewBase, mock(InterstitialManager.class));
    }

    @Test
    public void expandTest() {

        doAnswer(invocation -> {
            RedirectUrlListener listener = invocation.getArgument(1);
            listener.onSuccess("test", "html");
            return null;
        }).when(spyBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));

        PrebidWebViewBase mockPreloadedListener = mock(PrebidWebViewBase.class);
        HTMLCreative mockCreative = mock(HTMLCreative.class);

        when(mockPreloadedListener.getCreative()).thenReturn(mockCreative);
        when(mockWebViewBase.getPreloadedListener()).thenReturn(mockPreloadedListener);

        CompletedCallBack callBack = mock(CompletedCallBack.class);
        final MraidVariableContainer mraidVariableContainer = spyBaseJsInterface.getMraidVariableContainer();
        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);

        MraidExpand spyExpand = spy(mraidExpand);

        doAnswer(invocation -> {
            CompletedCallBack completedCallBack = invocation.getArgument(1);
            completedCallBack.expandDialogShown();
            return null;
        }).when(spyExpand).showExpandDialog(any(), any());

        spyExpand.expand("test", callBack);

        verify(spyBaseJsInterface).followToOriginalUrl(anyString(), any(RedirectUrlListener.class));
        assertEquals(mraidVariableContainer.getUrlForLaunching(), "test");
        verify(callBack).expandDialogShown();
        verify(spyExpand).showExpandDialog(any(), any());
    }

    @Test
    public void nullifyDialogTest() throws IllegalAccessException {
        AdBaseDialog mockDialog = mock(AdBaseDialog.class);
        WhiteBox.field(MraidExpand.class, "expandedDialog").set(mraidExpand, mockDialog);

        mraidExpand.nullifyDialog();

        verify(mockDialog).cleanup();
        verify(mockDialog).cancel();
    }

    @Test
    public void setMraidExpandedTest() {
        mraidExpand.setMraidExpanded(true);
        assertTrue(mraidExpand.isMraidExpanded());
    }
}
