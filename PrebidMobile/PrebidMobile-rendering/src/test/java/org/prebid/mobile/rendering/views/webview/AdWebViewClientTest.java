package org.prebid.mobile.rendering.views.webview;

import android.webkit.WebView;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static junit.framework.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdWebViewClientTest {

    public static final String TEST_URL = "test";

    private AdWebViewClient mAdWebViewClient;
    private AdWebViewClient.AdAssetsLoadedListener mAdAssetLoadListener;

    @Before
    public void setup() {
        mAdAssetLoadListener = mock(AdWebViewClient.AdAssetsLoadedListener.class);

        mAdWebViewClient = new AdWebViewClient(mAdAssetLoadListener);
    }

    @Test
    public void onPageStartedLoadingTest() {
        WebView mockWebView = mock(WebView.class);

        mAdWebViewClient.onPageStarted(mockWebView, TEST_URL, null);

        verify(mAdAssetLoadListener).startLoadingAssets();
        assertEquals(false, mAdWebViewClient.isLoadingFinished());
    }

    @Test
    public void onPageFinishedTest() {
        WebView mockWebView = mock(WebView.class);

        mAdWebViewClient.onPageFinished(mockWebView, TEST_URL);

        verify(mAdAssetLoadListener).adAssetsLoaded();
        assertEquals(true, mAdWebViewClient.isLoadingFinished());
    }

    @Test
    public void onLoadResourceTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        WebView.HitTestResult mockHitTestResult = mock(WebView.HitTestResult.class);

        when(mockWebView.isClicked()).thenReturn(true);
        when(mockWebView.containsIFrame()).thenReturn(true);
        when(mockWebView.getHitTestResult()).thenReturn(mockHitTestResult);
        when(mockHitTestResult.getType()).thenReturn(WebView.HitTestResult.SRC_ANCHOR_TYPE);

        mAdWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockWebView).stopLoading();
        verify(mockWebView).loadUrl(TEST_URL);
    }

    @Test
    public void shouldOverrideUrlLoadingTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        when(mockWebView.isClicked()).thenReturn(false);

        boolean returnValue = mAdWebViewClient.shouldOverrideUrlLoading(mockWebView, TEST_URL);

        // Get the rendered HTML
        verify(mockWebView).loadUrl("javascript:window.HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        verify(mockWebView).stopLoading();
        assertEquals(true, returnValue);

        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        mockWebView.mMraidListener = mockListener;
        when(mockWebView.getMRAIDInterface()).thenReturn(mock(BaseJSInterface.class));
        when(mockWebView.getPreloadedListener()).thenReturn(mock(PreloadManager.PreloadedListener.class));
        when(mockWebView.canHandleClick()).thenReturn(true);
        when(mockWebView.isClicked()).thenReturn(true);
        returnValue = mAdWebViewClient.shouldOverrideUrlLoading(mockWebView, TEST_URL);
        // Get the rendered HTML
        verify(mockListener).openExternalLink(TEST_URL);
        assertEquals(true, returnValue);
    }
}
