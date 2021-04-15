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
import org.prebid.mobile.rendering.errors.AdException;
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

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyInt;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class HTMLCreativeTest {

    private Context mContext;
    @Mock
    AdConfiguration mMockConfig;
    @Mock
    CreativeModel mMockModel;
    @Mock
    OmAdSessionManager mMockOmAdSessionManager;
    @Mock
    InterstitialManager mMockInterstitialManager;
    @Mock
    MraidController mMockMraidController;
    @Mock
    CreativeVisibilityTracker mMockCreativeVisibilityTracker;
    @Mock
    PrebidWebViewBase mMockPrebidWebView;

    private HTMLCreative mHtmlCreative;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        ManagersResolver.getInstance().prepare(mContext);

        when(mMockModel.getAdConfiguration()).thenReturn(mMockConfig);
        mHtmlCreative = new HTMLCreative(mContext, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);
        mHtmlCreative.setCreativeView(mMockPrebidWebView);
        WhiteBox.setInternalState(mHtmlCreative, "mMraidController", mMockMraidController);
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
            new HTMLCreative(null, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);
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
                                            any(AdConfiguration.AdUnitIdentifierType.class),
                                            any(InterstitialManager.class)))
            .thenReturn(mockPrebidWebViewBanner);
        WhiteBox.field(ViewPool.class, "sInstance").set(null, mockViewPool);

        // Test null context
        try {
            WhiteBox.field(HTMLCreative.class, "mContextReference").set(mHtmlCreative, null);
            mHtmlCreative.load();
            fail("AdException was NOT thrown");
        }
        catch (AdException e) {
        }
        mHtmlCreative = new HTMLCreative(mContext, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);

        // Test null adType
        try {
            mHtmlCreative.load();
            fail("AdException was NOT thrown");
        }
        catch (AdException e) {
        }

        // Test empty html
        try {
            when(mMockConfig.getAdUnitIdentifierType()).thenReturn(AdConfiguration.AdUnitIdentifierType.BANNER);
            mHtmlCreative = new HTMLCreative(mContext, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);
            mHtmlCreative.load();
            fail("AdException was NOT thrown");
        }
        catch (AdException e) {
        }

        // Test non-empty html
        when(mMockConfig.getAdUnitIdentifierType()).thenReturn(AdConfiguration.AdUnitIdentifierType.BANNER);
        when(mMockModel.getHtml()).thenReturn("foo");

        mHtmlCreative = new HTMLCreative(mContext, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);
        mHtmlCreative.load();
        verify(mockPrebidWebViewBanner).loadHTML(any(), anyInt(), anyInt());
        assertEquals(mockPrebidWebViewBanner, mHtmlCreative.getCreativeView());
    }

    @Test
    public void displayTest() throws Exception {
        // Null view
        PrebidWebViewBase prebidWebViewBase = new PrebidWebViewBase(mContext, mMockInterstitialManager);
        WhiteBox.setInternalState(prebidWebViewBase, "mWebView", mock(WebViewBase.class));
        when(mMockPrebidWebView.getWebView()).thenReturn(mock(WebViewBase.class));

        when(mMockConfig.getAdUnitIdentifierType()).thenReturn(AdConfiguration.AdUnitIdentifierType.BANNER);

        mHtmlCreative.display();
        verify(mMockModel, never()).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
    }

    @Test
    public void adSessionSuccessInitializationTest() throws Exception {
        PrebidWebViewBase prebidWebView = new PrebidWebViewBase(mContext, mMockInterstitialManager);
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        BaseJSInterface baseJSInterface = mock(BaseJSInterface.class);
        when(mockWebViewBase.getMRAIDInterface()).thenReturn(baseJSInterface);
        WhiteBox.setInternalState(prebidWebView, "mWebView", mockWebViewBase);

        mHtmlCreative.setCreativeView(prebidWebView);
        mHtmlCreative.setCreativeView(prebidWebView);
        HTMLCreative spyHtmlCreative = spy(mHtmlCreative);

        spyHtmlCreative.display();
    }

    @Test
    public void adSessionFailureInitializationTest() throws Exception {
        PrebidWebViewBase prebidWebViewBase = new PrebidWebViewBase(mContext, mMockInterstitialManager);
        WhiteBox.setInternalState(prebidWebViewBase, "mWebView", mock(WebViewBase.class));
        mHtmlCreative.setCreativeView(prebidWebViewBase);
        HTMLCreative spyHtmlCreative = spy(mHtmlCreative);

        spyHtmlCreative.display();

        verify(spyHtmlCreative, never()).addOmFriendlyObstruction(any(InternalFriendlyObstruction.class));
    }

    @Test
    public void creativeViewListenerEventsTest() throws AdException {
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        mHtmlCreative.setCreativeViewListener(mockCreativeViewListener);

        mHtmlCreative.interstitialAdClosed();
        verify(mockCreativeViewListener).creativeInterstitialDidClose(mHtmlCreative);

        mHtmlCreative.mraidAdExpanded();
        verify(mockCreativeViewListener).creativeDidExpand(mHtmlCreative);

        mHtmlCreative.mraidAdCollapsed();
        verify(mockCreativeViewListener).creativeDidCollapse(mHtmlCreative);
    }

    @Test
    public void changeVisibilityTrackerStateAdWebViewNoFocusVisibilityTrackerNotNull_StopVisibilityCheck()
    throws IllegalAccessException {
        WhiteBox.setInternalState(mHtmlCreative, "mCreativeVisibilityTracker", mMockCreativeVisibilityTracker);

        mHtmlCreative.changeVisibilityTrackerState(false);

        verify(mMockCreativeVisibilityTracker, times(1)).stopVisibilityCheck();
        verifyNoMoreInteractions(mMockCreativeVisibilityTracker);
    }

    @Test
    public void changeVisibilityTrackerStateAdWebViewHasFocusVisibilityTrackerNotNull_StartVisibilityCheck()
    throws IllegalAccessException {
        WhiteBox.setInternalState(mHtmlCreative, "mCreativeVisibilityTracker", mMockCreativeVisibilityTracker);

        mHtmlCreative.changeVisibilityTrackerState(true);

        verify(mMockCreativeVisibilityTracker, times(1)).stopVisibilityCheck();
        verify(mMockCreativeVisibilityTracker, times(1)).startVisibilityCheck(mContext);
        verifyNoMoreInteractions(mMockCreativeVisibilityTracker);
    }

    @Test
    public void viewabilityTrackListenerExecution_TrackOnWindowFocusChangeAndOnViewExposureChange()
    throws Exception {
        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        when(mockPrebidWebViewBase.getWebView()).thenReturn(mock(WebViewBase.class));
        mHtmlCreative.setCreativeView(mockPrebidWebViewBase);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     null, false, false);

        doAnswer(invocation -> {
            CreativeVisibilityTracker.VisibilityTrackerListener listener =
                invocation.getArgument(0);

            listener.onVisibilityChanged(result);
            return null;
        }).when(mMockCreativeVisibilityTracker)
          .setVisibilityTrackerListener(any(CreativeVisibilityTracker.VisibilityTrackerListener.class));

        mHtmlCreative.onVisibilityEvent(result);

        verify(mMockModel, never()).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockPrebidWebViewBase, times(1)).onWindowFocusChanged(false);
        verify(mockPrebidWebViewBase, times(1)).onViewExposureChange(null);
    }

    @Test
    public void viewabilityTrackListenerExecutionIsViewable_trackImpression() {
        ViewExposure viewExposure = new ViewExposure();

        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        when(mockPrebidWebViewBase.getWebView()).thenReturn(mock(WebViewBase.class));
        mHtmlCreative.setCreativeView(mockPrebidWebViewBase);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     viewExposure, true, true);
        mHtmlCreative.onVisibilityEvent(result);

        verify(mMockModel, times(1)).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockPrebidWebViewBase, times(1)).onWindowFocusChanged(true);
        verify(mockPrebidWebViewBase, times(1)).onViewExposureChange(viewExposure);
    }

    @Test
    public void webViewReadyToDisplayTest() throws Exception {
        CreativeResolutionListener mockResolutionListener = mock(CreativeResolutionListener.class);
        mHtmlCreative.setResolutionListener(mockResolutionListener);

        // Resolved
        WhiteBox.field(HTMLCreative.class, "mResolved").set(mHtmlCreative, true);
        mHtmlCreative.webViewReadyToDisplay();
        verify(mockResolutionListener, never()).creativeReady(any(AbstractCreative.class));

        // Not resolved
        WhiteBox.field(HTMLCreative.class, "mResolved").set(mHtmlCreative, false);
        mHtmlCreative.webViewReadyToDisplay();
        verify(mockResolutionListener).creativeReady(any(AbstractCreative.class));
    }

    @Test
    public void webViewFailedToLoadTest() throws Exception {
        CreativeResolutionListener mockResolutionListener = mock(CreativeResolutionListener.class);
        mHtmlCreative.setResolutionListener(mockResolutionListener);

        // Resolved
        WhiteBox.field(HTMLCreative.class, "mResolved").set(mHtmlCreative, true);
        mHtmlCreative.webViewFailedToLoad(new AdException("foo", "bar"));
        verify(mockResolutionListener, never()).creativeFailed(any(AdException.class));

        // Not resolved
        WhiteBox.field(HTMLCreative.class, "mResolved").set(mHtmlCreative, false);
        mHtmlCreative.webViewFailedToLoad(new AdException("foo", "bar"));
        verify(mockResolutionListener).creativeFailed(any(AdException.class));
    }

    @Test
    public void prebidWebViewDelegateEventTest() throws Exception {
        // External link
        PrebidWebViewBase mockPrebidWebViewBase = mock(PrebidWebViewBase.class);
        Mockito.doNothing().when(mockPrebidWebViewBase).handleOpen(anyString());
        when(mockPrebidWebViewBase.post(any(Runnable.class))).thenReturn(anyBoolean());

        mHtmlCreative.setCreativeView(mockPrebidWebViewBase);
        mHtmlCreative.webViewShouldOpenExternalLink("foo");
        verify(mockPrebidWebViewBase).handleOpen(anyString());

        // MRAID link
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        mHtmlCreative.setCreativeViewListener(mockCreativeViewListener);
        mHtmlCreative.webViewShouldOpenMRAIDLink("foo");
        verify(mockCreativeViewListener).creativeWasClicked(any(HTMLCreative.class), anyString());
        verify(mockPrebidWebViewBase).post(any(Runnable.class));
    }

    @Test
    public void handleMRAIDEventsInCreativeTest() throws Exception {
        mHtmlCreative.handleMRAIDEventsInCreative(mock(MraidEvent.class), mock(WebViewBase.class));
        verify(mMockMraidController).handleMraidEvent(any(MraidEvent.class),
                                                      eq(mHtmlCreative),
                                                      any(WebViewBase.class),
                                                      any());
    }

    @Test
    public void destroyTest() {
        MraidController mockController = mock(MraidController.class);
        WhiteBox.setInternalState(mHtmlCreative, "mMraidController", mockController);

        mHtmlCreative.destroy();
        verify(mMockPrebidWebView).destroy();
        verify(mockController).destroy();
    }

    @Test
    public void createOmAdSessionTest() throws IllegalAccessException {
        PrebidWebViewBase mockOXWebView = mock(PrebidWebViewBase.class);
        when(mockOXWebView.getWebView()).thenReturn(mock(WebViewBase.class));

        mHtmlCreative.setCreativeView(mockOXWebView);

        mHtmlCreative.createOmAdSession();
        verify(mMockOmAdSessionManager).initWebAdSessionManager(any(WebViewBase.class), any());
        verify(mMockOmAdSessionManager).registerAdView(any(View.class));
        verify(mMockOmAdSessionManager).startAdSession();

        reset(mMockOmAdSessionManager);
        mHtmlCreative.mWeakOmAdSessionManager = new WeakReference<>(null);
        mHtmlCreative.createOmAdSession();
        verify(mMockOmAdSessionManager, never()).startAdSession();

        mHtmlCreative.setCreativeView(null);
        mHtmlCreative.createOmAdSession();
        verify(mMockOmAdSessionManager, never()).startAdSession();
    }

    @Test
    public void isResolvedTest() {
        assertFalse(mHtmlCreative.isResolved());
    }

    @Test
    public void htmlAdLoaded_TrackEvent() {
        mHtmlCreative.trackAdLoaded();

        verify(mMockModel).trackDisplayAdEvent(TrackingEvent.Events.LOADED);
    }
}
