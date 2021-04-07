package com.openx.apollo.views.webview;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.widget.FrameLayout;

import com.apollo.test.utils.WhiteBox;
import com.openx.apollo.listeners.WebViewDelegate;
import com.openx.apollo.views.interstitial.InterstitialManager;
import com.openx.apollo.views.webview.mraid.BaseJSInterface;
import com.openx.apollo.views.webview.mraid.JsExecutor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class OpenXWebViewBaseTest {

    private OpenXWebViewBase mOpenXWebViewBase;
    private WebViewBanner mMockWebViewBanner;
    private BaseJSInterface mMockBaseJSInterface;
    private JsExecutor mMockJsExecutor;

    @Before
    public void setUp() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mOpenXWebViewBase = new OpenXWebViewBase(context, mock(InterstitialManager.class));
        mMockWebViewBanner = mock(WebViewBanner.class);
        mMockBaseJSInterface = mock(BaseJSInterface.class);
        mMockJsExecutor = mock(JsExecutor.class);

        when(mMockWebViewBanner.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        when(mMockBaseJSInterface.getJsExecutor()).thenReturn(mMockJsExecutor);

        mOpenXWebViewBase.mMraidWebView = mMockWebViewBanner;
    }

    @Test
    public void initMRAIDExpandedTest(){

        mOpenXWebViewBase.initMraidExpanded();
        verify(mMockBaseJSInterface, timeout(100)).onReadyExpanded();
    }

    @Test
    public void handleOpenTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        when(mockWebView.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        mOpenXWebViewBase.mCurrentWebViewBase = mockWebView;

        mOpenXWebViewBase.handleOpen("test");
        verify(mMockBaseJSInterface).open(eq("test"));
    }

    @Test
    public void openExternalLinkTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        mOpenXWebViewBase.mWebViewDelegate = mockDelegate;

        mOpenXWebViewBase.openExternalLink("test");
        verify(mockDelegate).webViewShouldOpenExternalLink(eq("test"));
    }

    @Test
    public void openMRAIDExternalLinkTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        mOpenXWebViewBase.mWebViewDelegate = mockDelegate;

        mOpenXWebViewBase.openMraidExternalLink("test");
        verify(mockDelegate).webViewShouldOpenMRAIDLink(eq("test"));
    }

    @Test
    public void renderAdViewTest() {
        when(mMockWebViewBanner.isMRAID()).thenReturn(true);

        mOpenXWebViewBase.renderAdView(null);
        verify(mMockWebViewBanner, times(0)).setVisibility(eq(View.VISIBLE));

        mOpenXWebViewBase.renderAdView(mMockWebViewBanner);
        verify(mMockWebViewBanner).setVisibility(eq(View.VISIBLE));
    }

    @Test
    public void displayAdViewPlacement() {
        when(mMockWebViewBanner.getAdHeight()).thenReturn(1);
        when(mMockWebViewBanner.getAdWidth()).thenReturn(1);

        mOpenXWebViewBase.setLayoutParams(mock(FrameLayout.LayoutParams.class));
        mOpenXWebViewBase.displayAdViewPlacement(mMockWebViewBanner);
        verify(mMockWebViewBanner, times(2)).getAdHeight();
        verify(mMockWebViewBanner, times(2)).getAdWidth();
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        Handler mockHandler = mock(Handler.class);
        WhiteBox.field(OpenXWebViewBase.class, "mHandler").set(mOpenXWebViewBase, mockHandler);
        when(mockHandler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                invocation.getArgumentAt(0, Runnable.class).run();
                return null;
            }
        });

        mOpenXWebViewBase.destroy();
        verify(mMockWebViewBanner).destroy();
    }

    @Test
    public void onWindowFocusChangedTest() throws IllegalAccessException {
        mOpenXWebViewBase.mCurrentWebViewBase = mMockWebViewBanner;
        WhiteBox.field(OpenXWebViewBase.class, "mScreenVisibility").set(mOpenXWebViewBase, -1);

        mOpenXWebViewBase.onWindowFocusChanged(true);
        verify(mMockBaseJSInterface).handleScreenViewabilityChange(eq(true));
    }
}