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

package org.prebid.mobile.rendering.views.webview;

import android.webkit.WebView;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class AdWebViewClientTest {

    public static final String TEST_URL = "test";

    private AdWebViewClient adWebViewClient;
    private AdWebViewClient.AdAssetsLoadedListener adAssetLoadListener;

    @Before
    public void setup() {
        adAssetLoadListener = mock(AdWebViewClient.AdAssetsLoadedListener.class);

        adWebViewClient = new AdWebViewClient(adAssetLoadListener);
    }

    @Test
    public void onPageStartedLoadingTest() {
        WebView mockWebView = mock(WebView.class);

        adWebViewClient.onPageStarted(mockWebView, TEST_URL, null);

        verify(adAssetLoadListener).startLoadingAssets();
        assertEquals(false, adWebViewClient.isLoadingFinished());
    }

    @Test
    public void onPageFinishedTest() {
        WebView mockWebView = mock(WebView.class);

        adWebViewClient.onPageFinished(mockWebView, TEST_URL);

        verify(adAssetLoadListener).adAssetsLoaded();
        assertEquals(true, adWebViewClient.isLoadingFinished());
    }

    @Test
    public void onLoadResource_clickedIFrameAnchor_opensTheClickUrlExternally() {
        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        WebViewBase mockWebView = clickedIFrameWebView(WebView.HitTestResult.SRC_ANCHOR_TYPE, mockListener);

        adWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockWebView).stopLoading();
        verify(mockListener).openExternalLink(TEST_URL);
        // loadUrl() never reaches shouldOverrideUrlLoading, so it must not be the route to the click.
        verify(mockWebView, never()).loadUrl(anyString());
    }

    @Test
    public void onLoadResource_clickedIFrameImageAnchor_opensTheClickUrlExternally() {
        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        WebViewBase mockWebView = clickedIFrameWebView(WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE, mockListener);

        adWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockListener).openExternalLink(TEST_URL);
    }

    @Test
    public void onLoadResource_prefersTheTargetUrlOverTheIFrameNavigation() {
        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        WebViewBase mockWebView = clickedIFrameWebView(WebView.HitTestResult.SRC_ANCHOR_TYPE, mockListener);
        when(mockWebView.getTargetUrl()).thenReturn("https://landing.example");

        adWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockListener).openExternalLink("https://landing.example");
    }

    @Test
    public void onLoadResource_sameUrlLoadedAgain_opensOnlyOnce() {
        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        WebViewBase mockWebView = clickedIFrameWebView(WebView.HitTestResult.SRC_ANCHOR_TYPE, mockListener);

        adWebViewClient.onLoadResource(mockWebView, TEST_URL);
        adWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockListener, times(1)).openExternalLink(TEST_URL);
    }

    @Test
    public void onLoadResource_withoutATap_isNotAClick() {
        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        WebViewBase mockWebView = clickedIFrameWebView(WebView.HitTestResult.SRC_ANCHOR_TYPE, mockListener);
        when(mockWebView.isClicked()).thenReturn(false);

        adWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockWebView, never()).stopLoading();
        verify(mockListener, never()).openExternalLink(anyString());
    }

    @Test
    public void onLoadResource_tapThatIsNotOnALink_isNotAClick() {
        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        WebViewBase mockWebView = clickedIFrameWebView(WebView.HitTestResult.UNKNOWN_TYPE, mockListener);

        adWebViewClient.onLoadResource(mockWebView, TEST_URL);

        verify(mockWebView, never()).stopLoading();
        verify(mockListener, never()).openExternalLink(anyString());
    }

    private static WebViewBase clickedIFrameWebView(int hitTestType, MraidEventsManager.MraidListener listener) {
        WebViewBase mockWebView = mock(WebViewBase.class);
        WebView.HitTestResult mockHitTestResult = mock(WebView.HitTestResult.class);
        mockWebView.mraidListener = listener;

        when(mockWebView.isClicked()).thenReturn(true);
        when(mockWebView.containsIFrame()).thenReturn(true);
        when(mockWebView.getHitTestResult()).thenReturn(mockHitTestResult);
        when(mockHitTestResult.getType()).thenReturn(hitTestType);
        when(mockWebView.getMRAIDInterface()).thenReturn(mock(BaseJSInterface.class));
        when(mockWebView.getPreloadedListener()).thenReturn(mock(PreloadManager.PreloadedListener.class));
        when(mockWebView.canHandleClick()).thenReturn(true);
        return mockWebView;
    }

    @Test
    public void shouldOverrideUrlLoadingTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        when(mockWebView.isClicked()).thenReturn(false);

        boolean returnValue = adWebViewClient.shouldOverrideUrlLoading(mockWebView, TEST_URL);

        // Get the rendered HTML
        verify(mockWebView).loadUrl("javascript:window.HtmlViewer.showHTML" + "('<html>'+document.getElementsByTagName('html')[0].innerHTML+'</html>');");
        verify(mockWebView).stopLoading();
        assertEquals(true, returnValue);

        MraidEventsManager.MraidListener mockListener = mock(MraidEventsManager.MraidListener.class);
        mockWebView.mraidListener = mockListener;
        when(mockWebView.getMRAIDInterface()).thenReturn(mock(BaseJSInterface.class));
        when(mockWebView.getPreloadedListener()).thenReturn(mock(PreloadManager.PreloadedListener.class));
        when(mockWebView.canHandleClick()).thenReturn(true);
        when(mockWebView.isClicked()).thenReturn(true);
        returnValue = adWebViewClient.shouldOverrideUrlLoading(mockWebView, TEST_URL);
        // Get the rendered HTML
        verify(mockListener).openExternalLink(TEST_URL);
        assertEquals(true, returnValue);
    }
}
