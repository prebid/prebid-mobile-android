package com.openx.apollo.models;

import android.app.Activity;
import android.content.Context;
import android.view.View;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.errors.AdException;
import com.openx.apollo.listeners.CreativeResolutionListener;
import com.openx.apollo.listeners.CreativeViewListener;
import com.openx.apollo.listeners.VideoCreativeViewListener;
import com.openx.apollo.models.internal.InternalFriendlyObstruction;
import com.openx.apollo.models.internal.MraidEvent;
import com.openx.apollo.models.internal.VisibilityTrackerResult;
import com.openx.apollo.models.ntv.NativeEventTracker;
import com.openx.apollo.mraid.methods.MraidController;
import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.session.manager.OmAdSessionManager;
import com.openx.apollo.utils.exposure.ViewExposure;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.OpenXWebViewBanner;
import com.openx.apollo.views.webview.OpenXWebViewBase;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
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
    OpenXWebViewBase mMockOpenXWebView;

    private HTMLCreative mHtmlCreative;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();

        ManagersResolver.getInstance().prepare(mContext);

        when(mMockModel.getAdConfiguration()).thenReturn(mMockConfig);
        mHtmlCreative = new HTMLCreative(mContext, mMockModel, mMockOmAdSessionManager, mMockInterstitialManager);
        mHtmlCreative.setCreativeView(mMockOpenXWebView);
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
        OpenXWebViewBanner mockOpenXWebViewBanner = mock(OpenXWebViewBanner.class);

        ViewPool mockViewPool = mock(ViewPool.class);
        when(mockViewPool.getUnoccupiedView(any(Context.class),
                                            any(VideoCreativeViewListener.class),
                                            any(AdConfiguration.AdUnitIdentifierType.class),
                                            any(InterstitialManager.class)))
            .thenReturn(mockOpenXWebViewBanner);
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
        verify(mockOpenXWebViewBanner).loadHTML(anyString(), anyInt(), anyInt());
        assertEquals(mockOpenXWebViewBanner, mHtmlCreative.getCreativeView());
    }

    @Test
    public void displayTest() throws Exception {
        // Null view
        OpenXWebViewBase openXWebViewBase = new OpenXWebViewBase(mContext, mMockInterstitialManager);
        WhiteBox.setInternalState(openXWebViewBase, "mWebView", mock(WebViewBase.class));
        when(mMockOpenXWebView.getWebView()).thenReturn(mock(WebViewBase.class));

        when(mMockConfig.getAdUnitIdentifierType()).thenReturn(AdConfiguration.AdUnitIdentifierType.BANNER);

        mHtmlCreative.display();
        verify(mMockModel, never()).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
    }

    @Test
    public void adSessionSuccessInitializationTest() throws Exception {
        OpenXWebViewBase openXWebView = new OpenXWebViewBase(mContext, mMockInterstitialManager);
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        BaseJSInterface baseJSInterface = mock(BaseJSInterface.class);
        when(mockWebViewBase.getMRAIDInterface()).thenReturn(baseJSInterface);
        WhiteBox.setInternalState(openXWebView, "mWebView", mockWebViewBase);

        mHtmlCreative.setCreativeView(openXWebView);
        mHtmlCreative.setCreativeView(openXWebView);
        HTMLCreative spyHtmlCreative = spy(mHtmlCreative);

        spyHtmlCreative.display();
    }

    @Test
    public void adSessionFailureInitializationTest() throws Exception {
        OpenXWebViewBase openXWebViewBase = new OpenXWebViewBase(mContext, mMockInterstitialManager);
        WhiteBox.setInternalState(openXWebViewBase, "mWebView", mock(WebViewBase.class));
        mHtmlCreative.setCreativeView(openXWebViewBase);
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
        OpenXWebViewBase mockOpenXWebViewBase = mock(OpenXWebViewBase.class);
        when(mockOpenXWebViewBase.getWebView()).thenReturn(mock(WebViewBase.class));
        mHtmlCreative.setCreativeView(mockOpenXWebViewBase);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     null, false, false);

        doAnswer(invocation -> {
            CreativeVisibilityTracker.VisibilityTrackerListener listener =
                invocation.getArgumentAt(0, CreativeVisibilityTracker.VisibilityTrackerListener.class);

            listener.onVisibilityChanged(result);
            return null;
        }).when(mMockCreativeVisibilityTracker)
          .setVisibilityTrackerListener(any(CreativeVisibilityTracker.VisibilityTrackerListener.class));

        mHtmlCreative.onVisibilityEvent(result);

        verify(mMockModel, never()).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockOpenXWebViewBase, times(1)).onWindowFocusChanged(false);
        verify(mockOpenXWebViewBase, times(1)).onViewExposureChange(null);
    }

    @Test
    public void viewabilityTrackListenerExecutionIsViewable_trackImpression() {
        ViewExposure viewExposure = new ViewExposure();

        OpenXWebViewBase mockOpenXWebViewBase = mock(OpenXWebViewBase.class);
        when(mockOpenXWebViewBase.getWebView()).thenReturn(mock(WebViewBase.class));
        mHtmlCreative.setCreativeView(mockOpenXWebViewBase);

        VisibilityTrackerResult result = new VisibilityTrackerResult(NativeEventTracker.EventType.IMPRESSION,
                                                                     viewExposure, true, true);
        mHtmlCreative.onVisibilityEvent(result);

        verify(mMockModel, times(1)).trackDisplayAdEvent(TrackingEvent.Events.IMPRESSION);
        verify(mockOpenXWebViewBase, times(1)).onWindowFocusChanged(true);
        verify(mockOpenXWebViewBase, times(1)).onViewExposureChange(viewExposure);
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
    public void openXWebViewDelegateEventTest() throws Exception {
        // External link
        OpenXWebViewBase mockOpenXWebViewBase = mock(OpenXWebViewBase.class);
        Mockito.doNothing().when(mockOpenXWebViewBase).handleOpen(anyString());
        when(mockOpenXWebViewBase.post(any(Runnable.class))).thenReturn(anyBoolean());

        mHtmlCreative.setCreativeView(mockOpenXWebViewBase);
        mHtmlCreative.webViewShouldOpenExternalLink("foo");
        verify(mockOpenXWebViewBase).handleOpen(anyString());

        // MRAID link
        CreativeViewListener mockCreativeViewListener = mock(CreativeViewListener.class);
        mHtmlCreative.setCreativeViewListener(mockCreativeViewListener);
        mHtmlCreative.webViewShouldOpenMRAIDLink("foo");
        verify(mockCreativeViewListener).creativeWasClicked(any(HTMLCreative.class), anyString());
        verify(mockOpenXWebViewBase).post(any(Runnable.class));
    }

    @Test
    public void handleMRAIDEventsInCreativeTest() throws Exception {
        mHtmlCreative.handleMRAIDEventsInCreative(mock(MraidEvent.class), mock(WebViewBase.class));
        verify(mMockMraidController).handleMraidEvent(any(MraidEvent.class),
                                                      eq(mHtmlCreative),
                                                      any(WebViewBase.class),
                                                      any(OpenXWebViewBase.class));
    }

    @Test
    public void destroyTest() {
        MraidController mockController = mock(MraidController.class);
        WhiteBox.setInternalState(mHtmlCreative, "mMraidController", mockController);

        mHtmlCreative.destroy();
        verify(mMockOpenXWebView).destroy();
        verify(mockController).destroy();
    }

    @Test
    public void createOmAdSessionTest() throws IllegalAccessException {
        OpenXWebViewBase mockOXWebView = mock(OpenXWebViewBase.class);
        when(mockOXWebView.getWebView()).thenReturn(mock(WebViewBase.class));

        mHtmlCreative.setCreativeView(mockOXWebView);

        mHtmlCreative.createOmAdSession();
        verify(mMockOmAdSessionManager).initWebAdSessionManager(any(WebViewBase.class), anyString());
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
