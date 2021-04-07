package com.openx.apollo.views.interstitial;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.bidding.display.InterstitialView;
import com.openx.apollo.bidding.display.VideoView;
import com.openx.apollo.interstitial.AdBaseDialog;
import com.openx.apollo.interstitial.AdInterstitialDialog;
import com.openx.apollo.interstitial.InterstitialManagerDisplayDelegate;
import com.openx.apollo.interstitial.InterstitialManagerVideoDelegate;
import com.openx.apollo.models.HTMLCreative;
import com.openx.apollo.models.InterstitialDisplayPropertiesInternal;
import com.openx.apollo.models.internal.MraidEvent;
import com.openx.apollo.mraid.methods.InterstitialManagerMraidDelegate;
import com.openx.apollo.video.VideoCreative;
import com.openx.apollo.views.AdViewManager;
import com.openx.apollo.views.webview.OpenXWebViewInterstitial;
import com.openx.apollo.views.webview.WebViewBanner;
import com.openx.apollo.views.webview.WebViewBase;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JSInterface;
import com.openx.apollo.views.webview.mraid.JsExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
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

        OpenXWebViewInterstitial mockOpenXWebViewInterstitial = mock(OpenXWebViewInterstitial.class);
        when(mockOpenXWebViewInterstitial.getWebView()).thenReturn(mockWebViewBase);

        InterstitialView mockInterstitialView = mock(InterstitialView.class);
        when(mockInterstitialView.getCreativeView()).thenReturn(mockOpenXWebViewInterstitial);

        mSpyInterstitialManager.displayAdViewInInterstitial(mContext, mockInterstitialView);

        verify(mMockAdViewDelegate).showInterstitial();
    }

    @Test
    public void displayOpenXWebViewForMraid_CallDelegateDisplayOpenXWebViewForMRAID()
    throws Exception {
        WebViewBanner mockWebView = mock(WebViewBanner.class);
        MraidEvent mockEvent = mock(MraidEvent.class);

        mockEvent.mraidAction = JSInterface.ACTION_EXPAND;
        mockEvent.mraidActionHelper = "test";
        when(mockWebView.getMraidEvent()).thenReturn(mockEvent);

        mSpyInterstitialManager.displayOpenXWebViewForMRAID(mockWebView, true);

        verify(mMockMraidDelegate).displayOpenXWebViewForMRAID(mockWebView, true, mockEvent);
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
