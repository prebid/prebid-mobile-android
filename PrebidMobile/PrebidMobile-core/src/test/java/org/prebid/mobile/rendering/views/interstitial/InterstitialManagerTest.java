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

package org.prebid.mobile.rendering.views.interstitial;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.rendering.InterstitialView;
import org.prebid.mobile.api.rendering.VideoView;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.interstitial.AdInterstitialDialog;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerDisplayDelegate;
import org.prebid.mobile.rendering.interstitial.InterstitialManagerVideoDelegate;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.InterstitialDisplayPropertiesInternal;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.mraid.methods.InterstitialManagerMraidDelegate;
import org.prebid.mobile.rendering.video.VideoCreative;
import org.prebid.mobile.rendering.views.AdViewManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewInterstitial;
import org.prebid.mobile.rendering.views.webview.WebViewBanner;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import java.util.Stack;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialManagerTest {

    private Context context;
    private InterstitialManager spyInterstitialManager;

    @Mock
    InterstitialManagerDisplayDelegate mockInterstitialManagerDisplayDelegate;
    @Mock
    private AdViewManager.AdViewManagerInterstitialDelegate mockAdViewDelegate;
    @Mock
    private InterstitialManagerMraidDelegate mockMraidDelegate;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();
        spyInterstitialManager = spy(InterstitialManager.class);
        spyInterstitialManager.setInterstitialDisplayDelegate(mockInterstitialManagerDisplayDelegate);
        spyInterstitialManager.setAdViewManagerInterstitialDelegate(mockAdViewDelegate);
        spyInterstitialManager.setMraidDelegate(mockMraidDelegate);
    }

    @Test
    public void interstitialClosedWithView_NotifyInterstitialDelegate() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        BaseJSInterface mockInterface = mock(BaseJSInterface.class);

        when(mockWebView.getMRAIDInterface()).thenReturn(mockInterface);

        spyInterstitialManager.interstitialClosed(mockWebView);

        verify(mockInterstitialManagerDisplayDelegate, times(1)).interstitialAdClosed();
    }

    @Test
    public void interstitialClosedAndMraidCollapsed_DoNotNotifyMraidDelegate()
    throws IllegalAccessException {
        HTMLCreative mockHtmlCreative = mock(HTMLCreative.class);
        AdInterstitialDialog mockAdInterstitialDialog = mock(AdInterstitialDialog.class);

        WhiteBox.field(InterstitialManager.class, "interstitialDialog")
                .set(spyInterstitialManager, mockAdInterstitialDialog);

        spyInterstitialManager.setInterstitialDisplayDelegate(mockHtmlCreative);

        when(mockMraidDelegate.collapseMraid()).thenReturn(true);

        spyInterstitialManager.interstitialClosed(null);

        verify(mockAdInterstitialDialog, never()).nullifyDialog();
    }

    @Test
    public void interstitialClosedNullMraidExpand_NullifyDialog() throws IllegalAccessException {
        AdInterstitialDialog mockAdInterstitialDialog = mock(AdInterstitialDialog.class);

        WhiteBox.field(InterstitialManager.class, "interstitialDialog")
                .set(spyInterstitialManager, mockAdInterstitialDialog);
        when(mockMraidDelegate.collapseMraid()).thenReturn(false);
        spyInterstitialManager.interstitialClosed(null);

        verify(mockAdInterstitialDialog, times(1)).nullifyDialog();
    }

    @Test
    public void whenDestroy_ResetInterstitialPropertiesAndDestroyExpand() {
        spyInterstitialManager.destroy();

        InterstitialDisplayPropertiesInternal interstitialDisplayProperties = spyInterstitialManager.getInterstitialDisplayProperties();

        assertEquals(0, interstitialDisplayProperties.expandHeight);
        assertEquals(0, interstitialDisplayProperties.expandWidth);

        verify(mockMraidDelegate).destroyMraidExpand();
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void displayViewAsInterstitialSuccess_ShowInterstitialAdView() {
        VideoCreative mockVideoCreative = mock(VideoCreative.class);
        when(mockVideoCreative.isResolved()).thenReturn(true);

        BaseJSInterface mockJsInterface = mock(BaseJSInterface.class);
        when(mockJsInterface.getJsExecutor()).thenReturn(mock(JsExecutor.class));

        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        when(mockWebViewBase.getMRAIDInterface()).thenReturn(mockJsInterface);

        PrebidWebViewInterstitial mockPrebidWebViewInterstitial = mock(PrebidWebViewInterstitial.class);
        when(mockPrebidWebViewInterstitial.getWebView()).thenReturn(mockWebViewBase);

        InterstitialView mockInterstitialView = mock(InterstitialView.class);
        when(mockInterstitialView.getCreativeView()).thenReturn(mockPrebidWebViewInterstitial);

        spyInterstitialManager.displayAdViewInInterstitial(context, mockInterstitialView);

        verify(mockAdViewDelegate).showInterstitial();
    }

    @Test
    public void displayPrebidWebViewForMraid_CallDelegateDisplayPrebidWebViewForMRAID()
    throws Exception {
        WebViewBanner mockWebView = mock(WebViewBanner.class);
        MraidEvent mockEvent = mock(MraidEvent.class);

        mockEvent.mraidAction = JSInterface.ACTION_EXPAND;
        mockEvent.mraidActionHelper = "test";
        when(mockWebView.getMraidEvent()).thenReturn(mockEvent);

        spyInterstitialManager.displayPrebidWebViewForMraid(mockWebView, true);

        verify(mockMraidDelegate).displayPrebidWebViewForMraid(mockWebView, true, mockEvent);
    }

    @Test
    public void addOldViewToBackStackIntControllerNull_ZeroViewStackInteractions() throws IllegalAccessException {
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        Stack<View> mockViewStack = spy(new Stack<>());
        WhiteBox.field(InterstitialManager.class, "viewStack")
                .set(spyInterstitialManager, mockViewStack);

        spyInterstitialManager.addOldViewToBackStack(mockWebViewBase, null, null);

        verifyNoInteractions(mockViewStack);
    }

    @Test
    public void addOldViewToBackStackValidIntController_PushDisplayViewToStack() throws IllegalAccessException {
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        Stack<View> mockViewStack = spy(new Stack<>());
        AdBaseDialog mockInterstitialViewController = mock(AdBaseDialog.class);
        View mockDisplayView = mock(View.class);
        WhiteBox.field(InterstitialManager.class, "viewStack")
                .set(spyInterstitialManager, mockViewStack);

        when(mockInterstitialViewController.getDisplayView()).thenReturn(mockDisplayView);

        spyInterstitialManager.addOldViewToBackStack(mockWebViewBase, "test", mockInterstitialViewController);

        verify(mockViewStack).push(mockDisplayView);
    }

    @Test
    public void displayVideoAdViewInInterstitial_ExecuteVideoAdViewShow() {
        VideoView mockVideoAdView = mock(VideoView.class);

        spyInterstitialManager.displayVideoAdViewInInterstitial(context, mockVideoAdView);

        verify(mockAdViewDelegate).showInterstitial();
    }

    @Test
    public void interstitialAdClosed_NotifyInterstitialDelegate() {
        spyInterstitialManager.interstitialAdClosed();

        verify(mockInterstitialManagerDisplayDelegate).interstitialAdClosed();
    }

    @Test
    public void interstitialAdClosed_NotifyVideoDelegate() {
        InterstitialManagerVideoDelegate mockDelegate = mock(InterstitialManagerVideoDelegate.class);
        spyInterstitialManager.setInterstitialVideoDelegate(mockDelegate);
        spyInterstitialManager.interstitialAdClosed();

        verify(mockDelegate).onVideoInterstitialClosed();
    }

    @Test
    public void interstitialDialogShown_NotifyInterstitialDelegate() {
        spyInterstitialManager.interstitialDialogShown(mock(ViewGroup.class));

        verify(mockInterstitialManagerDisplayDelegate).interstitialDialogShown(any(ViewGroup.class));
    }
}
