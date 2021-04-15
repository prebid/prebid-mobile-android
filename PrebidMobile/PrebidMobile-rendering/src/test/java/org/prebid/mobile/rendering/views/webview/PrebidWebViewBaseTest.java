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
public class PrebidWebViewBaseTest {

    private PrebidWebViewBase mPrebidWebViewBase;
    private WebViewBanner mMockWebViewBanner;
    private BaseJSInterface mMockBaseJSInterface;
    private JsExecutor mMockJsExecutor;

    @Before
    public void setUp() throws Exception {
        Context context = Robolectric.buildActivity(Activity.class).create().get();
        mPrebidWebViewBase = new PrebidWebViewBase(context, mock(InterstitialManager.class));
        mMockWebViewBanner = mock(WebViewBanner.class);
        mMockBaseJSInterface = mock(BaseJSInterface.class);
        mMockJsExecutor = mock(JsExecutor.class);

        when(mMockWebViewBanner.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        when(mMockBaseJSInterface.getJsExecutor()).thenReturn(mMockJsExecutor);

        mPrebidWebViewBase.mMraidWebView = mMockWebViewBanner;
    }

    @Test
    public void initMRAIDExpandedTest(){

        mPrebidWebViewBase.initMraidExpanded();
        verify(mMockBaseJSInterface, timeout(100)).onReadyExpanded();
    }

    @Test
    public void handleOpenTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        when(mockWebView.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        mPrebidWebViewBase.mCurrentWebViewBase = mockWebView;

        mPrebidWebViewBase.handleOpen("test");
        verify(mMockBaseJSInterface).open(eq("test"));
    }

    @Test
    public void openExternalLinkTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        mPrebidWebViewBase.mWebViewDelegate = mockDelegate;

        mPrebidWebViewBase.openExternalLink("test");
        verify(mockDelegate).webViewShouldOpenExternalLink(eq("test"));
    }

    @Test
    public void openMRAIDExternalLinkTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        mPrebidWebViewBase.mWebViewDelegate = mockDelegate;

        mPrebidWebViewBase.openMraidExternalLink("test");
        verify(mockDelegate).webViewShouldOpenMRAIDLink(eq("test"));
    }

    @Test
    public void renderAdViewTest() {
        when(mMockWebViewBanner.isMRAID()).thenReturn(true);

        mPrebidWebViewBase.renderAdView(null);
        verify(mMockWebViewBanner, times(0)).setVisibility(eq(View.VISIBLE));

        mPrebidWebViewBase.renderAdView(mMockWebViewBanner);
        verify(mMockWebViewBanner).setVisibility(eq(View.VISIBLE));
    }

    @Test
    public void displayAdViewPlacement() {
        when(mMockWebViewBanner.getAdHeight()).thenReturn(1);
        when(mMockWebViewBanner.getAdWidth()).thenReturn(1);

        mPrebidWebViewBase.setLayoutParams(mock(FrameLayout.LayoutParams.class));
        mPrebidWebViewBase.displayAdViewPlacement(mMockWebViewBanner);
        verify(mMockWebViewBanner, times(2)).getAdHeight();
        verify(mMockWebViewBanner, times(2)).getAdWidth();
    }

    @Test
    public void destroyTest() throws IllegalAccessException {
        Handler mockHandler = mock(Handler.class);
        WhiteBox.field(PrebidWebViewBase.class, "mHandler").set(mPrebidWebViewBase, mockHandler);
        when(mockHandler.postDelayed(any(Runnable.class), anyLong())).thenAnswer(new Answer() {
            @Override
            public Object answer(InvocationOnMock invocation) {
                Runnable runnable = invocation.getArgument(0);
                runnable.run();
                return null;
            }
        });

        mPrebidWebViewBase.destroy();
        verify(mMockWebViewBanner).destroy();
    }

    @Test
    public void onWindowFocusChangedTest() throws IllegalAccessException {
        mPrebidWebViewBase.mCurrentWebViewBase = mMockWebViewBanner;
        WhiteBox.field(PrebidWebViewBase.class, "mScreenVisibility").set(mPrebidWebViewBase, -1);

        mPrebidWebViewBase.onWindowFocusChanged(true);
        verify(mMockBaseJSInterface).handleScreenViewabilityChange(eq(true));
    }
}