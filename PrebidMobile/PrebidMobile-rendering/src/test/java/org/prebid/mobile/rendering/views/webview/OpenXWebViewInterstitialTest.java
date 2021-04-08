package org.prebid.mobile.rendering.views.webview;

import android.app.Activity;
import android.content.Context;

import com.apollo.test.utils.ResourceUtils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.AdConfiguration;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class OpenXWebViewInterstitialTest {

    private OpenXWebViewInterstitial mOpenXWebViewInterstitial;
    private Context mContext;
    private String mAdHTML;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(mContext);

        mAdHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");

        mOpenXWebViewInterstitial = new OpenXWebViewInterstitial(mContext, mock(InterstitialManager.class));
    }

    @Test
    public void loadHTMLTest() throws IOException {
        mOpenXWebViewInterstitial.mCreative = mock(HTMLCreative.class);
        CreativeModel mockModel = mock(CreativeModel.class);
        when(mOpenXWebViewInterstitial.mCreative.getCreativeModel()).thenReturn(mockModel);
        when(mockModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(mockModel.getAdConfiguration()).thenReturn(new AdConfiguration());
        mOpenXWebViewInterstitial.loadHTML(mAdHTML, 100 ,200);

        assertNotNull(mOpenXWebViewInterstitial.mWebView);
        assertEquals("WebViewInterstitial", mOpenXWebViewInterstitial.mWebView.mMRAIDBridgeName);
    }

    @Test
    public void preloadedTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        mOpenXWebViewInterstitial.mWebViewDelegate = mock(WebViewDelegate.class);

        mOpenXWebViewInterstitial.preloaded(null);
        verify(mOpenXWebViewInterstitial.mWebViewDelegate, never()).webViewReadyToDisplay();

        mOpenXWebViewInterstitial.preloaded(mockWebView);
        verify(mOpenXWebViewInterstitial.mWebViewDelegate).webViewReadyToDisplay();
    }
}