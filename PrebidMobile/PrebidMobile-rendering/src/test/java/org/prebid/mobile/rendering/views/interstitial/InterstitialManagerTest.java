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
import org.prebid.mobile.rendering.bidding.display.InterstitialView;
import org.prebid.mobile.rendering.bidding.display.VideoView;
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

import static junit.framework.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class InterstitialManagerTest {

    private Context mContext;
    private InterstitialManager mSpyInterstitialManager;

    @Mock
    InterstitialManagerDisplayDelegate mMockInterstitialManagerDisplayDelegate;
    @Mock
    private AdViewManager.AdViewManagerInterstitialDelegate mMockAdViewDelegate;
    @Mock
    private InterstitialManagerMraidDelegate mMockMraidDelegate;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);

        mContext = Robolectric.buildActivity(Activity.class).create().get();
        mSpyInterstitialManager = spy(InterstitialManager.class);
        mSpyInterstitialManager.setInterstitialDisplayDelegate(mMockInterstitialManagerDisplayDelegate);
        mSpyInterstitialManager.setAdViewManagerInterstitialDelegate(mMockAdViewDelegate);
        mSpyInterstitialManager.setMraidDelegate(mMockMraidDelegate);
    }

    @Test
    public void interstitialClosedWithView_NotifyInterstitialDelegate() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        BaseJSInterface mockInterface = mock(BaseJSInterface.class);

        when(mockWebView.getMRAIDInterface()).thenReturn(mockInterface);

        mSpyInterstitialManager.interstitialClosed(mockWebView);

        verify(mMockInterstitialManagerDisplayDelegate, times(1)).interstitialAdClosed();
    }

    @Test
    public void interstitialClosedAndMraidCollapsed_DoNotNotifyMraidDelegate()
    throws IllegalAccessException {
        HTMLCreative mockHtmlCreative = mock(HTMLCreative.class);
        AdInterstitialDialog mockAdInterstitialDialog = mock(AdInterstitialDialog.class);

        WhiteBox.field(InterstitialManager.class, "mInterstitialDialog")
                .set(mSpyInterstitialManager, mockAdInterstitialDialog);

        mSpyInterstitialManager.setInterstitialDisplayDelegate(mockHtmlCreative);

        when(mMockMraidDelegate.collapseMraid()).thenReturn(true);

        mSpyInterstitialManager.interstitialClosed(null);

        verify(mockAdInterstitialDialog, never()).nullifyDialog();
    }

    @Test
    public void interstitialClosedNullMraidExpand_NullifyDialog() throws IllegalAccessException {
        AdInterstitialDialog mockAdInterstitialDialog = mock(AdInterstitialDialog.class);

        WhiteBox.field(InterstitialManager.class, "mInterstitialDialog")
                .set(mSpyInterstitialManager, mockAdInterstitialDialog);
        when(mMockMraidDelegate.collapseMraid()).thenReturn(false);
        mSpyInterstitialManager.interstitialClosed(null);

        verify(mockAdInterstitialDialog, times(1)).nullifyDialog();
    }

    @Test
    public void whenDestroy_ResetInterstitialPropertiesAndDestroyExpand() {
        mSpyInterstitialManager.destroy();

        InterstitialDisplayPropertiesInternal interstitialDisplayProperties = mSpyInterstitialManager.getInterstitialDisplayProperties();

        assertEquals(0, interstitialDisplayProperties.expandHeight);
        assertEquals(0, interstitialDisplayProperties.expandWidth);

        verify(mMockMraidDelegate).destroyMraidExpand();
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

        mSpyInterstitialManager.displayAdViewInInterstitial(mContext, mockInterstitialView);

        verify(mMockAdViewDelegate).showInterstitial();
    }

    @Test
    public void displayPrebidWebViewForMraid_CallDelegateDisplayPrebidWebViewForMRAID()
    throws Exception {
        WebViewBanner mockWebView = mock(WebViewBanner.class);
        MraidEvent mockEvent = mock(MraidEvent.class);

        mockEvent.mraidAction = JSInterface.ACTION_EXPAND;
        mockEvent.mraidActionHelper = "test";
        when(mockWebView.getMraidEvent()).thenReturn(mockEvent);

        mSpyInterstitialManager.displayPrebidWebViewForMraid(mockWebView, true);

        verify(mMockMraidDelegate).displayPrebidWebViewForMraid(mockWebView, true, mockEvent);
    }

    @Test
    public void addOldViewToBackStackIntControllerNull_ZeroViewStackInteractions() throws IllegalAccessException {
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        Stack<View> mockViewStack = spy(new Stack<>());
        WhiteBox.field(InterstitialManager.class, "mViewStack")
                .set(mSpyInterstitialManager, mockViewStack);

        mSpyInterstitialManager.addOldViewToBackStack(mockWebViewBase, null, null);

        verifyZeroInteractions(mockViewStack);
    }

    @Test
    public void addOldViewToBackStackValidIntController_PushDisplayViewToStack() throws IllegalAccessException {
        WebViewBase mockWebViewBase = mock(WebViewBase.class);
        Stack<View> mockViewStack = spy(new Stack<>());
        AdBaseDialog mockInterstitialViewController = mock(AdBaseDialog.class);
        View mockDisplayView = mock(View.class);
        WhiteBox.field(InterstitialManager.class, "mViewStack")
                .set(mSpyInterstitialManager, mockViewStack);

        when(mockInterstitialViewController.getDisplayView()).thenReturn(mockDisplayView);

        mSpyInterstitialManager.addOldViewToBackStack(mockWebViewBase, "test", mockInterstitialViewController);

        verify(mockViewStack).push(mockDisplayView);
    }

    @Test
    public void displayVideoAdViewInInterstitial_ExecuteVideoAdViewShow() {
        VideoView mockVideoAdView = mock(VideoView.class);

        mSpyInterstitialManager.displayVideoAdViewInInterstitial(mContext, mockVideoAdView);

        verify(mMockAdViewDelegate).showInterstitial();
    }

    @Test
    public void interstitialAdClosed_NotifyInterstitialDelegate() {
        mSpyInterstitialManager.interstitialAdClosed();

        verify(mMockInterstitialManagerDisplayDelegate).interstitialAdClosed();
    }

    @Test
    public void interstitialAdClosed_NotifyVideoDelegate() {
        InterstitialManagerVideoDelegate mockDelegate = mock(InterstitialManagerVideoDelegate.class);
        mSpyInterstitialManager.setInterstitialVideoDelegate(mockDelegate);
        mSpyInterstitialManager.interstitialAdClosed();

        verify(mockDelegate).onVideoInterstitialClosed();
    }

    @Test
    public void interstitialDialogShown_NotifyInterstitialDelegate() {
        mSpyInterstitialManager.interstitialDialogShown(any(ViewGroup.class));

        verify(mMockInterstitialManagerDisplayDelegate).interstitialDialogShown(any(ViewGroup.class));
    }
}
