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
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.units.configuration.AdUnitConfiguration;
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

    private PrebidWebViewInterstitial mPrebidWebViewInterstitial;
    private Context mContext;
    private String mAdHTML;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(mContext);

        mAdHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");

        mPrebidWebViewInterstitial = new PrebidWebViewInterstitial(mContext, mock(InterstitialManager.class));
    }

    @Test
    public void loadHTMLTest() throws IOException {
        mPrebidWebViewInterstitial.mCreative = mock(HTMLCreative.class);
        CreativeModel mockModel = mock(CreativeModel.class);
        when(mPrebidWebViewInterstitial.mCreative.getCreativeModel()).thenReturn(mockModel);
        when(mockModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(mockModel.getAdConfiguration()).thenReturn(new AdUnitConfiguration());
        mPrebidWebViewInterstitial.loadHTML(mAdHTML, 100, 200);

        assertNotNull(mPrebidWebViewInterstitial.mWebView);
        assertEquals("WebViewInterstitial", mPrebidWebViewInterstitial.mWebView.mMRAIDBridgeName);
    }

    @Test
    public void preloadedTest() {
        WebViewBase mockWebView = mock(WebViewBase.class);
        mPrebidWebViewInterstitial.mWebViewDelegate = mock(WebViewDelegate.class);

        mPrebidWebViewInterstitial.preloaded(null);
        verify(mPrebidWebViewInterstitial.mWebViewDelegate, never()).webViewReadyToDisplay();

        mPrebidWebViewInterstitial.preloaded(mockWebView);
        verify(mPrebidWebViewInterstitial.mWebViewDelegate).webViewReadyToDisplay();
    }
}