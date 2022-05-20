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

package org.prebid.mobile.rendering.views.webview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PrebidWebViewBaseTest {

    private PrebidWebViewBase prebidWebViewBase;
    private WebViewBanner mockWebViewBanner;
    private BaseJSInterface mockBaseJSInterface;
    private JsExecutor mockJsExecutor;

    @Before
    public void setUp() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        prebidWebViewBase = new PrebidWebViewBase(context, mock(InterstitialManager.class));
        mockWebViewBanner = mock(WebViewBanner.class);
        mockBaseJSInterface = mock(BaseJSInterface.class);
        mockJsExecutor = mock(JsExecutor.class);

        when(mockWebViewBanner.getMRAIDInterface()).thenReturn(mockBaseJSInterface);
        when(mockBaseJSInterface.getJsExecutor()).thenReturn(mockJsExecutor);

        prebidWebViewBase.mraidWebView = mockWebViewBanner;
    }

    @Test
    public void initMRAIDExpandedTest() {

        prebidWebViewBase.initMraidExpanded();
        verify(mockBaseJSInterface, timeout(100)).onReadyExpanded();
    }

    @Test
    public void handleOpenTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        when(mockWebView.getMRAIDInterface()).thenReturn(mockBaseJSInterface);
        prebidWebViewBase.currentWebViewBase = mockWebView;

        prebidWebViewBase.handleOpen("test");
        verify(mockBaseJSInterface).open(eq("test"));
    }

    @Test
    public void openExternalLinkTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        prebidWebViewBase.webViewDelegate = mockDelegate;

        prebidWebViewBase.openExternalLink("test");
        verify(mockDelegate).webViewShouldOpenExternalLink(eq("test"));
    }

    @Test
    public void openMRAIDExternalLinkTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        prebidWebViewBase.webViewDelegate = mockDelegate;

        prebidWebViewBase.openMraidExternalLink("test");
        verify(mockDelegate).webViewShouldOpenMRAIDLink(eq("test"));
    }

    @Test
    public void renderAdViewTest() {
        when(mockWebViewBanner.isMRAID()).thenReturn(true);

        prebidWebViewBase.renderAdView(null);
        verify(mockWebViewBanner, times(0)).setVisibility(eq(View.VISIBLE));

        prebidWebViewBase.renderAdView(mockWebViewBanner);
        verify(mockWebViewBanner).setVisibility(eq(View.VISIBLE));
    }

    @Test
    public void displayAdViewPlacement() {
        when(mockWebViewBanner.getAdHeight()).thenReturn(1);
        when(mockWebViewBanner.getAdWidth()).thenReturn(1);

        prebidWebViewBase.setLayoutParams(mock(FrameLayout.LayoutParams.class));
        prebidWebViewBase.displayAdViewPlacement(mockWebViewBanner);
        verify(mockWebViewBanner, times(2)).getAdHeight();
        verify(mockWebViewBanner, times(2)).getAdWidth();
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        Handler mockHandler = mock(Handler.class);
        WhiteBox.field(PrebidWebViewBase.class, "handler").set(prebidWebViewBase, mockHandler);
        when(mockHandler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }
        });

        prebidWebViewBase.destroy();
        verify(mockWebViewBanner).destroy();
    }

    @Test
    public void onWindowFocusChangedTest() throws IllegalAccessException {
        prebidWebViewBase.currentWebViewBase = mockWebViewBanner;
        WhiteBox.field(PrebidWebViewBase.class, "screenVisibility").set(prebidWebViewBase, -1);

        prebidWebViewBase.onWindowFocusChanged(true);
        verify(mockBaseJSInterface).handleScreenViewabilityChange(eq(true));
    }
}