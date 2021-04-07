package com.openx.apollo.views.webview;

import android.app.Activity;
import android.content.Context;

import com.apollo.test.utils.ResourceUtils;
import com.openx.apollo.listeners.WebViewDelegate;
import com.openx.apollo.models.AdConfiguration;
import com.openx.apollo.models.CreativeModel;
import com.openx.apollo.models.HTMLCreative;
import com.openx.apollo.sdk.ManagersResolver;
import com.openx.apollo.views.interstitial.InterstitialManager;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
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