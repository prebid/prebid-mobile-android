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

package org.prebid.mobile.rendering.models;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.prebid.mobile.api.data.AdFormat;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.listeners.CreativeResolutionListener;
import org.prebid.mobile.rendering.listeners.CreativeViewListener;
import org.prebid.mobile.rendering.models.internal.InternalFriendlyObstruction;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.models.internal.VisibilityTrackerResult;
import org.prebid.mobile.rendering.models.ntv.NativeEventTracker;
import org.prebid.mobile.rendering.mraid.methods.MraidController;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.session.manager.OmAdSessionManager;
import org.prebid.mobile.rendering.utils.exposure.ViewExposure;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBanner;
import org.prebid.mobile.rendering.views.webview.PrebidWebViewBase;
import org.prebid.mobile.rendering.views.webview.WebViewBase;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.lang.ref.WeakReference;
import java.util.EnumSet;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class HTMLCreativeTest {

    private Context context;
    @Mock
    AdUnitConfiguration mockConfig;
    @Mock
    CreativeModel mockModel;
    @Mock
    OmAdSessionManager mockOmAdSessionManager;
    @Mock
    InterstitialManager mockInterstitialManager;
    @Mock
    MraidController mockMraidController;
    @Mock
    CreativeVisibilityTracker mockCreativeVisibilityTracker;
    @Mock
    PrebidWebViewBase mockPrebidWebView;

    private HTMLCreative htmlCreative;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        context = Robolectric.buildActivity(Activity.class).create().get();

        ManagersResolver.getInstance().prepare(context);

        when(mockModel.getAdConfiguration()).thenReturn(mockConfig);
        htmlCreative = new HTMLCreative(context, mockModel, mockOmAdSessionManager, mockInterstitialManager);
        htmlCreative.setCreativeView(mockPrebidWebView);
        WhiteBox.setInternalState(htmlCreative, "mraidController", mockMraidController);
    }

    @After
    public void cleanup() throws IllegalAccessException {
        WhiteBox.field(ViewPool.class, "sInstance").set(null, null);
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void constructorTest() {
        try {
            new HTMLCreative(null, mockModel, mockOmAdSessionManager, mockInterstitialManager);
            fail("AdException was NOT thrown");
        }
        catch (AdException e) {
        }
    }

    @Test
    public void loadTest() throws Exception {
        PrebidWebViewBanner mockPrebidWebViewBanner = mock(PrebidWebViewBanner.class);

        ViewPool mockViewPool = mock(ViewPool.class);
        when(mockViewPool.getUnoccupiedView(any(Context.class),
                any(),
                any(AdFormat.class),
                any(InterstitialManager.class)))
            .thenReturn(mockPrebidWebViewBanner);
        WhiteBox.field(ViewPool.class, "sInstance").set(null, mockViewPool);

        // Test null context
        try {
            WhiteBox.field(HTMLCreative.class, "contextReference").set(htmlCreative, null);
            htmlCreative.load();
            fail("AdException was NOT thrown");
        }
        catch (AdException e) {
        }
        htmlCreative = new HTMLCreative(context, mockModel, mockOmAdSessionManager, mockInterstitialManager);

        EnumSet<AdFormat> result = EnumSet.noneOf(AdFormat.class);
        result.add(AdFormat.BANNER);
        // Test empty html
        try {
            when(mockConfig.getAdFormats()).thenReturn(result);
            htmlCreative = new HTMLCreative(context, mockModel, mockOmAdSessionManager, mockInterstitialManager);
            htmlCreative.load();
            fail("AdException was NOT thrown");
        } catch (AdException e) {
        }

        // Test non-empty html
        when(mockConfig.getAdFormats()).thenReturn(result);
        when(mockModel.getHtml()).thenReturn("foo");

        htmlCreative = new HTMLCreative(context, mockModel, mockOmAdSessionManager, mockInterstitialManager);
        htmlCreative.load();
        verify(mockPrebidWebViewBanner).loadHTML(any(), anyInt(), anyInt());
        assertEquals(mockPrebidWebViewBanner, htmlCreative.getCreativeView());
    }

    @Test
    public void displayTest() throws Exception {
        // Null view
        PrebidWebViewBase prebidWebViewBase = new PrebidWebViewBase(context, mockInterstitialManager);
        WhiteBox.setInternalState(prebidWebViewBase, "webView", mock(WebViewBase.class));
        when(mockPrebidWebView.getWebView()).thenReturn(mock(WebViewBase.class));

        EnumSet<AdFormat> result = EnumSet.noneOf(AdFormat.class);
        result.add(AdFormat.BANNER);
        when(mockConfig.getAdFormats()).thenReturn(result);

        htmlCreative.display();
        verify(mockModel, never()).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
    }

    @Test
    public void adSessionSuccessInitializationTest() throws Exception {
        PrebidWebViewBase prebidWebView = new PrebidWebViewBase(context, mockInterstitialManager);
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        BaseJSInterface baseJSInterface = mock(BaseJSInterface.class);
        when(mockWebViewBase.getMRAIDInterface()).thenReturn(baseJSInterface);
        WhiteBox.setInternalState(prebidWebView, "webView", mockWebViewBase);

        htmlCreative.setCreativeView(prebidWebView);
        htmlCreative.setCreativeView(prebidWebView);
        HTMLCreative spyHtmlCreative = spy(htmlCreative);

        spyHtmlCreative.display();
    }

    @Test
    public void adSessionFailureInitializationTest() throws Exception {
        PrebidWebViewBase prebidWebViewBase = new PrebidWebViewBase(context, mockInterstitialManager);
        WhiteBox.setInternalState(prebidWebViewBase, "webView", mock(WebViewBase.class));
        htmlCreative.setCreativeView(prebidWebViewBase);
        HTMLCreative spyHtmlCreative = spy(htmlCreative);

        spyHtmlCreative.display();

        verify(spyHtmlCreative, never()).addOmFriendlyObstruction(any(InternalFriendlyObstruction.class));
    }

    @Test
    public void creativeViewListenerEventsTest() throws AdException {
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        htmlCreative.setCreativeViewListener(mockCreativeViewListener);

        htmlCreative.interstitialAdClosed();
        verify(mockCreativeViewListener).creativeInterstitialDidClose(htmlCreative);

        htmlCreative.mraidAdExpanded();
        verify(mockCreativeViewListener).creativeDidExpand(htmlCreative);

        htmlCreative.mraidAdCollapsed();
        verify(mockCreativeViewListener).creativeDidCollapse(htmlCreative);
    }

    @Test
    public void changeVisibilityTrackerStateAdWebViewNoFocusVisibilityTrackerNotNull_StopVisibilityCheck()
    throws IllegalAccessException {
        WhiteBox.setInternalState(htmlCreative, "creativeVisibilityTracker", mockCreativeVisibilityTracker);

        htmlCreative.changeVisibilityTrackerState(false);

        verify(mockCreativeVisibilityTracker, times(1)).stopVisibilityCheck();
        verifyNoMoreInteractions(mockCreativeVisibilityTracker);
    }

    @Test
    public void changeVisibilityTrackerStateAdWebViewHasFocusVisibilityTrackerNotNull_StartVisibilityCheck()
    throws IllegalAccessException {
        WhiteBox.setInternalState(htmlCreative, "creativeVisibilityTracker", mockCreativeVisibilityTracker);

        htmlCreative.changeVisibilityTrackerState(true);

        verify(mockCreativeVisibilityTracker, times(1)).stopVisibilityCheck();
        verify(mockCreativeVisibilityTracker, times(1)).startVisibilityCheck(context);
        verifyNoMoreInteractions(mockCreativeVisibilityTracker);
    }

    @Test
    public void viewabilityTrackListenerExecution_TrackOnWindowFocusChangeAndOnViewExposureChange()
    throws Exception {
        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        when(mockPrebidWebViewBase.getWebView()).thenReturn(mock(WebViewBase.class));
        htmlCreative.setCreativeView(mockPrebidWebViewBase);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     null, false, false);

        doAnswer(invocation -> {
            CreativeVisibilityTracker.VisibilityTrackerListener listener =
                invocation.getArgument(0);

            listener.onVisibilityChanged(result);
            return null;
        }).when(mockCreativeVisibilityTracker)
          .setVisibilityTrackerListener(any(CreativeVisibilityTracker.VisibilityTrackerListener.class));

        htmlCreative.onVisibilityEvent(result);

        verify(mockModel, never()).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockPrebidWebViewBase, times(1)).onWindowFocusChanged(false);
        verify(mockPrebidWebViewBase, times(1)).onViewExposureChange(null);
    }

    @Test
    public void viewabilityTrackListenerExecutionIsViewable_trackImpression() {
        ViewExposure viewExposure = new ViewExposure();

        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        when(mockPrebidWebViewBase.getWebView()).thenReturn(mock(WebViewBase.class));
        htmlCreative.setCreativeView(mockPrebidWebViewBase);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     viewExposure, true, true);
        htmlCreative.onVisibilityEvent(result);

        verify(mockModel, times(1)).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockPrebidWebViewBase, times(1)).onWindowFocusChanged(true);
        verify(mockPrebidWebViewBase, times(1)).onViewExposureChange(viewExposure);
    }

    @Test
    public void webViewReadyToDisplayTest() throws Exception {
        CreativeResolutionListener mockResolutionListener = mock(CreativeResolutionListener.class);
        htmlCreative.setResolutionListener(mockResolutionListener);

        // Resolved
        WhiteBox.field(HTMLCreative.class, "resolved").set(htmlCreative, true);
        htmlCreative.webViewReadyToDisplay();
        verify(mockResolutionListener, never()).creativeReady(any(AbstractCreative.class));

        // Not resolved
        WhiteBox.field(HTMLCreative.class, "resolved").set(htmlCreative, false);
        htmlCreative.webViewReadyToDisplay();
        verify(mockResolutionListener).creativeReady(any(AbstractCreative.class));
    }

    @Test
    public void webViewFailedToLoadTest() throws Exception {
        CreativeResolutionListener mockResolutionListener = mock(CreativeResolutionListener.class);
        htmlCreative.setResolutionListener(mockResolutionListener);

        // Resolved
        WhiteBox.field(HTMLCreative.class, "resolved").set(htmlCreative, true);
        htmlCreative.webViewFailedToLoad(new AdException("foo", "bar"));
        verify(mockResolutionListener, never()).creativeFailed(any(AdException.class));

        // Not resolved
        WhiteBox.field(HTMLCreative.class, "resolved").set(htmlCreative, false);
        htmlCreative.webViewFailedToLoad(new AdException("foo", "bar"));
        verify(mockResolutionListener).creativeFailed(any(AdException.class));
    }

    @Test
    public void prebidWebViewDelegateEventTest() throws Exception {
        // External link
        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        Mockito.doNothing().when(mockPrebidWebViewBase).handleOpen(anyString());
        when(mockPrebidWebViewBase.post(any(Runnable.class))).thenReturn(anyBoolean());

        htmlCreative.setCreativeView(mockPrebidWebViewBase);
        htmlCreative.webViewShouldOpenExternalLink("foo");
        verify(mockPrebidWebViewBase).handleOpen(anyString());

        // MRAID link
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        htmlCreative.setCreativeViewListener(mockCreativeViewListener);
        htmlCreative.webViewShouldOpenMRAIDLink("foo");
        verify(mockCreativeViewListener).creativeWasClicked(any(HTMLCreative.class), anyString());
        verify(mockPrebidWebViewBase).post(any(Runnable.class));
    }

    @Test
    public void handleMRAIDEventsInCreativeTest() throws Exception {
        htmlCreative.handleMRAIDEventsInCreative(mock(MraidEvent.class), mock(WebViewBase.class));
        verify(mockMraidController).handleMraidEvent(any(MraidEvent.class),
                                                      eq(htmlCreative),
                                                      any(WebViewBase.class),
                                                      any());
    }

    @Test
    public void destroyTest() {
        MraidController mockController = mock(MraidController.class);
        WhiteBox.setInternalState(htmlCreative, "mraidController", mockController);

        htmlCreative.destroy();
        verify(mockPrebidWebView).destroy();
        verify(mockController).destroy();
    }

    @Test
    public void createOmAdSessionTest() throws IllegalAccessException {
        PrebidWebViewBase mockWebView = mock(PrebidWebViewBase.class);
        when(mockWebView.getWebView()).thenReturn(mock(WebViewBase.class));

        htmlCreative.setCreativeView(mockWebView);

        htmlCreative.createOmAdSession();
        verify(mockOmAdSessionManager).initWebAdSessionManager(any(WebViewBase.class), any());
        verify(mockOmAdSessionManager).registerAdView(any(View.class));
        verify(mockOmAdSessionManager).startAdSession();

        reset(mockOmAdSessionManager);
        htmlCreative.weakOmAdSessionManager = new WeakReference<>(null);
        htmlCreative.createOmAdSession();
        verify(mockOmAdSessionManager, never()).startAdSession();

        htmlCreative.setCreativeView(null);
        htmlCreative.createOmAdSession();
        verify(mockOmAdSessionManager, never()).startAdSession();
    }

    @Test
    public void isResolvedTest() {
        assertFalse(htmlCreative.isResolved());
    }

    @Test
    public void htmlAdLoaded_TrackEvent() {
        htmlCreative.trackAdLoaded();

        verify(mockModel).trackDisplayAdEvent(TrackingEvent.Events.LOADED);
    }
}
