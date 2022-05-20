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
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.ViewGroup;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.prebid.mobile.api.exceptions.AdException;
import org.prebid.mobile.configuration.AdUnitConfiguration;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.CreativeModel;
import org.prebid.mobile.rendering.models.HTMLCreative;
import org.prebid.mobile.rendering.models.internal.MraidEvent;
import org.prebid.mobile.rendering.mraid.handler.FetchPropertiesHandler;
import org.prebid.mobile.rendering.sdk.ManagersResolver;
import org.prebid.mobile.rendering.views.interstitial.InterstitialManager;
import org.prebid.mobile.rendering.views.webview.mraid.BaseJSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JSInterface;
import org.prebid.mobile.rendering.views.webview.mraid.JsExecutor;
import org.prebid.mobile.test.utils.ResourceUtils;
import org.prebid.mobile.test.utils.WhiteBox;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.*;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PrebidWebViewBannerTest {

    private PrebidWebViewBanner banner;
    private WebViewBanner mockWebViewBanner;
    private BaseJSInterface mockBaseJSInterface;

    private Context context;
    private String adHTML;
    private JsExecutor mockJsExecutor;

    @Before
    public void setUp() throws Exception {
        context = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(context);

        adHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");
        mockWebViewBanner = mock(WebViewBanner.class);
        mockBaseJSInterface = mock(BaseJSInterface.class);
        mockJsExecutor = mock(JsExecutor.class);

        when(mockBaseJSInterface.getJsExecutor()).thenReturn(mockJsExecutor);

        when(mockWebViewBanner.getMRAIDInterface()).thenReturn(mockBaseJSInterface);
        banner = new PrebidWebViewBanner(context, mock(InterstitialManager.class));

        banner.mraidWebView = mockWebViewBanner;
    }

    @Test
    public void loadMraidExpandPropertiesWebViewNotNull_ExecuteGetExpandProperties()
    throws IllegalAccessException {
        final String jsonAnswer = "{\"width\":100,\"height\":200,\"useCustomClose\":false,\"isModal\":true}";
        doAnswer(invocation -> {
            Handler handler = invocation.getArgument(0);
            Message message = new Message();

            Bundle data = new Bundle();
            data.putString(JSInterface.JSON_VALUE, jsonAnswer);

            message.setData(data);
            handler.handleMessage(message);
            return null;
        }).when(mockJsExecutor).executeGetExpandProperties(any(Handler.class));

        FetchPropertiesHandler.FetchPropertyCallback mockCallback = mock(FetchPropertiesHandler.FetchPropertyCallback.class);
        WhiteBox.field(PrebidWebViewBanner.class, "expandPropertiesCallback").set(banner, mockCallback);

        banner.loadMraidExpandProperties();

        verify(mockCallback).onResult(eq(jsonAnswer));
    }

    @Test
    public void preloadedTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        InterstitialManager mockManager = mock(InterstitialManager.class);
        banner.webViewDelegate = mockDelegate;
        banner.interstitialManager = mockManager;
        MraidEvent mockEvent = mock(MraidEvent.class);
        when(mockWebViewBanner.getMraidEvent()).thenReturn(mockEvent);

        banner.preloaded(null);
        verify(mockDelegate).webViewFailedToLoad(any(AdException.class));

        mockWebViewBanner.MRAIDBridgeName = "twopart";
        banner.preloaded(mockWebViewBanner);
        verify(mockManager).displayPrebidWebViewForMraid(eq(mockWebViewBanner), eq(true));

        mockWebViewBanner.MRAIDBridgeName = "else";
        banner.preloaded(mockWebViewBanner);
        //verify render
        verify(mockWebViewBanner).setAdWidth(anyInt());

        when(mockWebViewBanner.getParent()).thenReturn(mock(ViewGroup.class));
        banner.preloaded(mockWebViewBanner);
        verify(mockWebViewBanner).bringToFront();

        verify(mockDelegate, times(3)).webViewReadyToDisplay();
    }

    @Test
    public void initTwoPartAndLoadTest() throws IOException {
        banner.creative = mock(HTMLCreative.class);
        CreativeModel mockModel = mock(CreativeModel.class);
        when(mockModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(banner.creative.getCreativeModel()).thenReturn(mockModel);

        banner.initTwoPartAndLoad(adHTML);

        assertNotNull(banner.mraidWebView);
        assertEquals("twopart", banner.mraidWebView.MRAIDBridgeName);
    }

    @Test
    public void loadHTMLTest() throws IOException {
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        CreativeModel mockCreativeModel = mock(CreativeModel.class);
        when(mockCreative.getCreativeModel()).thenReturn(mockCreativeModel);
        when(mockCreativeModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(mockCreativeModel.getAdConfiguration()).thenReturn(new AdUnitConfiguration());
        banner.creative = mockCreative;

        banner.loadHTML(adHTML, 100 ,200);

        assertNotNull(banner.webView);
        assertEquals("1part", banner.webView.MRAIDBridgeName);
    }

}