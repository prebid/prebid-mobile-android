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
import android.view.View;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.views.browser.AdBrowserActivity;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.rendering.views.webview.mraid.ScreenMetricsWaiter;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidCloseTest {

    private MraidClose mraidClose;
    private Activity testActivity;
    private BaseJSInterface spyBaseJSInterface;
    private WebViewBase mockWebViewBase;
    private JsExecutor mockJsExecutor;

    @Before
    public void setUp() throws Exception {
        testActivity = Robolectric.buildActivity(Activity.class).create().get();
        mockJsExecutor = mock(JsExecutor.class);

        mockWebViewBase = mock(WebViewBase.class);
        when(mockWebViewBase.isMRAID()).thenReturn(true);
        when(mockWebViewBase.getContext()).thenReturn(testActivity);

        spyBaseJSInterface = Mockito.spy(new BaseJSInterface(testActivity, mockWebViewBase, mockJsExecutor));
        WhiteBox.setInternalState(spyBaseJSInterface, "screenMetricsWaiter", mock(ScreenMetricsWaiter.class));
        doNothing().when(spyBaseJSInterface).onStateChange(anyString());

        mraidClose = new MraidClose(testActivity, spyBaseJSInterface, mockWebViewBase);
    }

    @Test
    public void closeThroughJSTest() throws Exception {
        ViewGroup mockViewGroup = mock(ViewGroup.class);
        when(spyBaseJSInterface.getRootView()).thenReturn(mockViewGroup);
        final MraidVariableContainer mraidVariableContainer = spyBaseJSInterface.getMraidVariableContainer();

        mraidClose = new MraidClose(null, spyBaseJSInterface, mockWebViewBase);
        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);
        mraidClose.closeThroughJS();
        verify(spyBaseJSInterface, times(0)).onStateChange(anyString());

        mraidClose = new MraidClose(testActivity, spyBaseJSInterface, mockWebViewBase);
        mraidVariableContainer.setCurrentState(JSInterface.STATE_LOADING);
        mraidClose.closeThroughJS();
        verify(spyBaseJSInterface, times(0)).onStateChange(anyString());

        mraidVariableContainer.setCurrentState(JSInterface.STATE_DEFAULT);
        mraidClose.closeThroughJS();
        verify(spyBaseJSInterface).onStateChange(eq(JSInterface.STATE_HIDDEN));

        mraidVariableContainer.setCurrentState(JSInterface.STATE_EXPANDED);
        mraidClose.closeThroughJS();
        verify(spyBaseJSInterface).onStateChange(eq(JSInterface.STATE_DEFAULT));
        verify(mockViewGroup).removeView(any());

        reset(spyBaseJSInterface);
        AdBrowserActivity mockActivity = new AdBrowserActivity();
        mraidClose = new MraidClose(mockActivity, spyBaseJSInterface, mockWebViewBase);
        mraidVariableContainer.setCurrentState(JSInterface.STATE_EXPANDED);
        mraidClose.closeThroughJS();
        verify(spyBaseJSInterface).onStateChange(eq(JSInterface.STATE_DEFAULT));
    }

    @Test
    public void makeViewVisibleTest() throws InvocationTargetException, IllegalAccessException {
        Method method = WhiteBox.method(MraidClose.class, "makeViewInvisible");

        method.invoke(mraidClose);
        verify(mockWebViewBase, timeout(100)).setVisibility(eq(View.INVISIBLE));
    }
}