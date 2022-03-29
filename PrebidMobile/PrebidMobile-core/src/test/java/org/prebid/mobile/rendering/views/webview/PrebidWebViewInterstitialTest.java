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

import android.app.Activity;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PrebidWebViewInterstitialTest {

    private PrebidWebViewInterstitial prebidWebViewInterstitial;
    private Context context;
    private String adHTML;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(context);

        adHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");

        prebidWebViewInterstitial = new PrebidWebViewInterstitial(context, mock(InterstitialManager.class));
    }

    @Test
    public void loadHTMLTest() throws IOException {
        prebidWebViewInterstitial.creative = mock(HTMLCreative.class);
        CreativeModel mockModel = mock(CreativeModel.class);
        when(prebidWebViewInterstitial.creative.getCreativeModel()).thenReturn(mockModel);
        when(mockModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(mockModel.getAdConfiguration()).thenReturn(new AdUnitConfiguration());
        prebidWebViewInterstitial.loadHTML(adHTML, 100, 200);

        assertNotNull(prebidWebViewInterstitial.webView);
        assertEquals("WebViewInterstitial", prebidWebViewInterstitial.webView.MRAIDBridgeName);
    }

    @Test
    public void preloadedTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        prebidWebViewInterstitial.webViewDelegate = mock(WebViewDelegate.class);

        prebidWebViewInterstitial.preloaded(null);
        verify(prebidWebViewInterstitial.webViewDelegate, never()).webViewReadyToDisplay();

        prebidWebViewInterstitial.preloaded(mockWebView);
        verify(prebidWebViewInterstitial.webViewDelegate).webViewReadyToDisplay();
    }
}