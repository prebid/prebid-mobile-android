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
import android.content.Context;
import android.view.View;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.interstitial.AdBaseDialog;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.models.internal.MraidVariableContainer;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.MraidEventsManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBanner;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

import java.lang.reflect.Field;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class MraidControllerTest {

    @Mock
    private InterstitialManager mMockInterstitialManager;

    private MraidController mMraidController;
    private Context mContext;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);
        mMraidController = spy(new MraidController(mMockInterstitialManager));
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(mContext);
    }

    @After
    public void cleanup() {
        ManagersResolver.getInstance().dispose();
    }

    @Test
    public void handleMraidEventWhenCloseEvent_CallClose() {
        callControllerHandler(createMraidEvent(JSInterface.ACTION_CLOSE, null));
        verify(mMockInterstitialManager).interstitialClosed(any(View.class));
    }

    @Test
    public void handleMraidEventWhenUnloadEvent_CallCreativeCompleteAndClose() {
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        CreativeViewListener mockListener = mock(CreativeViewListener.class);
        when(mockCreative.getCreativeViewListener()).thenReturn(mockListener);

        mMraidController.handleMraidEvent(createMraidEvent(JSInterface.ACTION_UNLOAD, null),
                                          mockCreative,
                                          mock(WebViewBase.class),
                                          mock(PrebidWebViewBase.class));

        verify(mMockInterstitialManager).interstitialClosed(any(View.class));
        verify(mockListener).creativeDidComplete(mockCreative);
    }

    @Test
    public void handleMraidEventWhenCalendarEvent_CallCreateCalendarEvent() {
        String actionHelper = "{\"description\":\"Mayan Apocalypse/End of World\",\"location\":\"everywhere\",\"start\":\"2013-12-21T00:00-05:00\",\"end\":\"2013-12-22T00:00-05:00\"}";
        callControllerHandler(createMraidEvent(JSInterface.ACTION_CREATE_CALENDAR_EVENT, actionHelper));
        assertNotNull(WhiteBox.getInternalState(mMraidController, "mMraidCalendarEvent"));
    }

    @Test
    public void handleMraidEventWhenExpandEventAndHelperEmpty_InitExpand() {
        MraidEvent event = createMraidEvent(JSInterface.ACTION_EXPAND, null);
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        WebViewBase mockOldWebView = mock(WebViewBase.class);

        when(mockOldWebView.getMraidListener()).thenReturn(mock(MraidEventsManager.MraidListener.class));
        when(mockOldWebView.getContext()).thenReturn(mContext);

        mMraidController.handleMraidEvent(event, mockCreative, mockOldWebView, mock(PrebidWebViewBase.class));
        verify(mMraidController).initMraidExpand(any(View.class),
                                                 any(MraidController.DisplayCompletionListener.class),
                                                 any(MraidEvent.class));
    }

    @Test
    public void handleMraidEventWhenExpandEventAndHelperEmpty_RunTwoPartRunnable() {
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        MraidEvent event = createMraidEvent(JSInterface.ACTION_EXPAND, "twoPart");
        WebViewBase mockOldWebView = mock(WebViewBase.class);

        when(mockOldWebView.getMraidListener()).thenReturn(mock(MraidEventsManager.MraidListener.class));
        when(mockOldWebView.getContext()).thenReturn(mContext);

        mMraidController.handleMraidEvent(event, mockCreative, mockOldWebView, mock(PrebidWebViewBase.class));
        ShadowLooper.runUiThreadTasks();
        verify(mMraidController, times(1)).expand(any(WebViewBase.class), any(PrebidWebViewBase.class), eq(event));
    }

    @Test
    public void expandWhenHelperNotEmpty_NewWebViewSetEvent() {
        WebViewBase mockOldWebView = mock(WebViewBase.class);
        PrebidWebViewBase mockNewWebView = mock(PrebidWebViewBase.class);
        MraidEvent event = createMraidEvent(JSInterface.ACTION_EXPAND, "twoPart");
        WebViewBanner mockBanner = mock(WebViewBanner.class);

        when(mockNewWebView.getMraidWebView()).thenReturn(mockBanner);
        when(mockOldWebView.getMraidListener()).thenReturn(mock(MraidEventsManager.MraidListener.class));

        mMraidController.expand(mockOldWebView, mockNewWebView, event);
        verify(mockNewWebView, times(1)).getMraidWebView();
        verify(mockBanner).setMraidEvent(eq(event));
    }

    @Test
    public void handleMraidEventWhenOrientationChangeEvent_CallOnSetOrientationProperties()
    throws IllegalAccessException, AdException {
        MraidExpand mockMraidExpand = mock(MraidExpand.class);
        AdBaseDialog mockDialog = mock(AdBaseDialog.class);
        when(mockMraidExpand.getInterstitialViewController()).thenReturn(mockDialog);
        WhiteBox.field(MraidController.class, "mMraidExpand").set(mMraidController, mockMraidExpand);

        callControllerHandler(createMraidEvent(JSInterface.ACTION_ORIENTATION_CHANGE, null));
        verify(mockDialog).handleSetOrientationProperties();
    }

    @Test
    public void handleMraidEventWhenOpenEvent_CallOpen() {
        callControllerHandler(createMraidEvent(JSInterface.ACTION_OPEN, "test"));
        assertNotNull(WhiteBox.getInternalState(mMraidController, "mMraidUrlHandler"));
    }

    @Test
    public void handleMraidEventWhenPlayVideoEvent_DisplayMraidInInterstitial() {
        MraidEvent event = createMraidEvent(JSInterface.ACTION_PLAY_VIDEO, "test");
        callControllerHandler(event);

        // called when displaying interstitial
        verify(mMraidController).initMraidExpand(any(View.class),
                                                 any(MraidController.DisplayCompletionListener.class),
                                                 any(MraidEvent.class));
    }

    @Test
    public void handleMraidEventWhenResizeEvent_CallResize() throws Exception {
        BaseJSInterface mockJsInterface = mock(BaseJSInterface.class);
        MraidVariableContainer container = new MraidVariableContainer();
        container.setCurrentState(JSInterface.STATE_LOADING);
        when(mockJsInterface.getMraidVariableContainer()).thenReturn(container);

        WebViewBase mockOldWebView = mock(WebViewBase.class);
        when(mockOldWebView.getContext()).thenReturn(mContext);
        when(mockOldWebView.getMRAIDInterface()).thenReturn(mockJsInterface);
        mMraidController.handleMraidEvent(createMraidEvent(JSInterface.ACTION_RESIZE, null), mock(HTMLCreative.class), mockOldWebView, mock(PrebidWebViewBase.class));
        assertNotNull(WhiteBox.getInternalState(mMraidController, "mMraidResize"));
    }

    @Test
    public void handleMraidEventWhenStorePictureEvent_CallStorePicture() throws Exception {
        callControllerHandler(createMraidEvent(JSInterface.ACTION_STORE_PICTURE, "test"));
        assertNotNull(WhiteBox.getInternalState(mMraidController, "mMraidStorePicture"));
    }

    @Test
    public void destroy_MraidMethodsDestroyed() throws IllegalAccessException {
        MraidResize mockMraidResize = mock(MraidResize.class);
        MraidUrlHandler mockMraidUrlHandler = mock(MraidUrlHandler.class);

        WhiteBox.field(MraidController.class, "mMraidResize").set(mMraidController, mockMraidResize);
        WhiteBox.field(MraidController.class, "mMraidUrlHandler").set(mMraidController, mockMraidUrlHandler);

        mMraidController.destroy();
        verify(mockMraidResize).destroy();
        verify(mockMraidUrlHandler).destroy();
    }

    @Test
    public void delegateDisplayViewInInterstitialAndExpandNull_InitExpand()
    throws Exception {
        mMraidController = new MraidController(mMockInterstitialManager);
        MraidEvent mockEvent = mock(MraidEvent.class);

        mockEvent.mraidAction = JSInterface.ACTION_EXPAND;
        mockEvent.mraidActionHelper = "test";

        InterstitialManagerMraidDelegate delegate = getMraidDelegate();
        delegate.displayViewInInterstitial(mock(WebViewBase.class),
                                           true,
                                           mockEvent,
                                           mock(MraidController.DisplayCompletionListener.class));
        assertNotNull(WhiteBox.getInternalState(mMraidController, "mMraidExpand"));
    }

    @Test
    public void delegateDisplayViewInInterstitialAndExpandNotNull_SetExpandDisplayViewAndInitMraidExpanded()
    throws IllegalAccessException {
        mMraidController = new MraidController(mMockInterstitialManager);
        WebViewBase mockWebView = mock(WebViewBase.class);
        MraidEvent mockEvent = mock(MraidEvent.class);
        MraidExpand mockMraidExpand = mock(MraidExpand.class);
        PrebidWebViewBase mockOxBase = mock(PrebidWebViewBase.class);

        mockEvent.mraidAction = JSInterface.ACTION_EXPAND;
        mockEvent.mraidActionHelper = "test";

        when(mockMraidExpand.getInterstitialViewController()).thenReturn(mock(AdBaseDialog.class));
        when(mockWebView.getPreloadedListener()).thenReturn(mockOxBase);

        WhiteBox.field(MraidController.class, "mMraidExpand").set(mMraidController, mockMraidExpand);
        getMraidDelegate().displayPrebidWebViewForMraid(mockWebView, true, mockEvent);

        verify(mockMraidExpand, times(1)).setDisplayView(mockWebView);
        verify(mockOxBase).initMraidExpanded();
    }

    @Test
    public void delegateOnInterstitialClosed_NullifyExpand() throws IllegalAccessException {
        mMraidController = new MraidController(mMockInterstitialManager);
        MraidExpand mockMraidExpand = mock(MraidExpand.class);
        HTMLCreative mockCreative = mock(HTMLCreative.class);

        when(mockMraidExpand.isMraidExpanded()).thenReturn(true);
        when(mMockInterstitialManager.getHtmlCreative()).thenReturn(mockCreative);

        Field expandField = WhiteBox.field(MraidController.class, "mMraidExpand");
        expandField.set(mMraidController, mockMraidExpand);

        getMraidDelegate().collapseMraid();
        verify(mockCreative).mraidAdCollapsed();
        verify(mockMraidExpand).nullifyDialog();
        assertNull(expandField.get(mMraidController));
    }

    @Test
    public void delegateOnDestroy_DestroyMraidExpand() throws IllegalAccessException {
        mMraidController = new MraidController(mMockInterstitialManager);
        MraidExpand mockMraidExpand = mock(MraidExpand.class);

        when(mockMraidExpand.isMraidExpanded()).thenReturn(true);

        Field expandField = WhiteBox.field(MraidController.class, "mMraidExpand");
        expandField.set(mMraidController, mockMraidExpand);

        getMraidDelegate().destroyMraidExpand();
        verify(mockMraidExpand).destroy();
        assertNull(expandField.get(mMraidController));
    }

    private void callControllerHandler(MraidEvent event) {
        final HTMLCreative mockCreative = mock(HTMLCreative.class);
        final CreativeModel mockCreativeModel = mock(CreativeModel.class);
        final WebViewBase mockOldWebView = mock(WebViewBase.class);

        when(mockCreative.getCreativeModel()).thenReturn(mockCreativeModel);
        when(mockCreativeModel.getAdConfiguration()).thenReturn(mock(AdConfiguration.class));
        when(mockOldWebView.getContext()).thenReturn(mContext);

        mMraidController.handleMraidEvent(event, mockCreative, mockOldWebView, mock(PrebidWebViewBase.class));
    }

    private MraidEvent createMraidEvent(String eventType, String helper) {
        MraidEvent event = mock(MraidEvent.class);
        event.mraidAction = eventType;
        event.mraidActionHelper = helper;
        return event;
    }

    private InterstitialManagerMraidDelegate getMraidDelegate() throws IllegalAccessException {
        return (InterstitialManagerMraidDelegate) WhiteBox.field(MraidController.class, "mInterstitialManagerMraidDelegate").get(mMraidController);
    }
}