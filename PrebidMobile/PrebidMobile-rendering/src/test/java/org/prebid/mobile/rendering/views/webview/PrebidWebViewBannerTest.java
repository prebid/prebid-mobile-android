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
import org.prebid.mobile.rendering.errors.AdException;
import org.prebid.mobile.rendering.listeners.WebViewDelegate;
import org.prebid.mobile.rendering.models.AdConfiguration;
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
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 19)
public class PrebidWebViewBannerTest {

    private PrebidWebViewBanner mBanner;
    private WebViewBanner mMockWebViewBanner;
    private BaseJSInterface mMockBaseJSInterface;

    private Context mContext;
    private String mAdHTML;
    private JsExecutor mMockJsExecutor;

    @Before
    public void setUp() throws Exception {
        mContext = Robolectric.buildActivity(Activity.class).create().get();
        ManagersResolver.getInstance().prepare(mContext);

        mAdHTML = ResourceUtils.convertResourceToString("ad_not_mraid_html.txt");
        mMockWebViewBanner = mock(WebViewBanner.class);
        mMockBaseJSInterface = mock(BaseJSInterface.class);
        mMockJsExecutor = mock(JsExecutor.class);

        when(mMockBaseJSInterface.getJsExecutor()).thenReturn(mMockJsExecutor);

        when(mMockWebViewBanner.getMRAIDInterface()).thenReturn(mMockBaseJSInterface);
        mBanner = new PrebidWebViewBanner(mContext, mock(InterstitialManager.class));

        mBanner.mMraidWebView = mMockWebViewBanner;
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
        }).when(mMockJsExecutor).executeGetExpandProperties(any(Handler.class));

        FetchPropertiesHandler.FetchPropertyCallback mockCallback = mock(FetchPropertiesHandler.FetchPropertyCallback.class);
        WhiteBox.field(PrebidWebViewBanner.class, "mExpandPropertiesCallback").set(mBanner, mockCallback);

        mBanner.loadMraidExpandProperties();

        verify(mockCallback).onResult(eq(jsonAnswer));
    }

    @Test
    public void preloadedTest() {
        WebViewDelegate mockDelegate = mock(WebViewDelegate.class);
        InterstitialManager mockManager = mock(InterstitialManager.class);
        mBanner.mWebViewDelegate = mockDelegate;
        mBanner.mInterstitialManager = mockManager;
        MraidEvent mockEvent = mock(MraidEvent.class);
        when(mMockWebViewBanner.getMraidEvent()).thenReturn(mockEvent);

        mBanner.preloaded(null);
        verify(mockDelegate).webViewFailedToLoad(any(AdException.class));

        mMockWebViewBanner.mMRAIDBridgeName = "twopart";
        mBanner.preloaded(mMockWebViewBanner);
        verify(mockManager).displayPrebidWebViewForMraid(eq(mMockWebViewBanner), eq(true));

        mMockWebViewBanner.mMRAIDBridgeName = "else";
        mBanner.preloaded(mMockWebViewBanner);
        //verify render
        verify(mMockWebViewBanner).setAdWidth(anyInt());

        when(mMockWebViewBanner.getParent()).thenReturn(mock(ViewGroup.class));
        mBanner.preloaded(mMockWebViewBanner);
        verify(mMockWebViewBanner).bringToFront();

        verify(mockDelegate, times(3)).webViewReadyToDisplay();
    }

    @Test
    public void initTwoPartAndLoadTest() throws IOException {
        mBanner.mCreative = mock(HTMLCreative.class);
        CreativeModel mockModel = mock(CreativeModel.class);
        when(mockModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(mBanner.mCreative.getCreativeModel()).thenReturn(mockModel);

        mBanner.initTwoPartAndLoad(mAdHTML);

        assertNotNull(mBanner.mMraidWebView);
        assertEquals("twopart", mBanner.mMraidWebView.mMRAIDBridgeName);
    }

    @Test
    public void loadHTMLTest() throws IOException {
        HTMLCreative mockCreative = mock(HTMLCreative.class);
        CreativeModel mockCreativeModel = mock(CreativeModel.class);
        when(mockCreative.getCreativeModel()).thenReturn(mockCreativeModel);
        when(mockCreativeModel.getHtml()).thenReturn(ResourceUtils.convertResourceToString("ad_contains_iframe"));
        when(mockCreativeModel.getAdConfiguration()).thenReturn(new AdConfiguration());
        mBanner.mCreative = mockCreative;

        mBanner.loadHTML(mAdHTML, 100 ,200);

        assertNotNull(mBanner.mWebView);
        assertEquals("1part", mBanner.mWebView.mMRAIDBridgeName);
    }

}